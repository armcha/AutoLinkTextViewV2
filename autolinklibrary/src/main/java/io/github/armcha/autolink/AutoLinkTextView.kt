package io.github.armcha.autolink

import android.content.Context
import android.graphics.Color
import android.text.DynamicLayout
import android.text.SpannableString
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.StaticLayout
import android.text.TextUtils
import android.text.style.CharacterStyle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.TextView
import java.lang.reflect.Field
import kotlin.system.measureTimeMillis

class AutoLinkTextView(context: Context, attrs: AttributeSet? = null) : TextView(context, attrs) {

    companion object {
        internal val TAG = AutoLinkTextView::class.java.simpleName
        private const val MIN_PHONE_NUMBER_LENGTH = 8
        private const val DEFAULT_COLOR = Color.RED
    }

    private val spanMap = mutableMapOf<Mode, HashSet<CharacterStyle>>()
    private var onAutoLinkClick: ((Mode, String) -> Unit)? = null
    private val transformations = mutableMapOf<String, String>()
    private val defaultSelectedColor = Color.LTGRAY
    private val modes = mutableSetOf<Mode>()

    var mentionModeColor = DEFAULT_COLOR
    var hashTagModeColor = DEFAULT_COLOR
    var customModeColor = DEFAULT_COLOR
    var phoneModeColor = DEFAULT_COLOR
    var emailModeColor = DEFAULT_COLOR
    var urlModeColor = DEFAULT_COLOR

    init {
        movementMethod = LinkTouchMovementMethod()
        highlightColor = Color.TRANSPARENT
    }

    override fun setText(text: CharSequence, type: BufferType) {
        if (TextUtils.isEmpty(text)) {
            super.setText(text, type)
            return
        }
        val time = measureTimeMillis {
            val spannableString = makeSpannableString(text)
            super.setText(spannableString, type)
        }
        Log.e("measureTimeMillis", "TIME $time")
        Log.e("modes", "modes ${modes.size}")
        Log.e("transformations", "transformations ${transformations.size}")
    }

    fun addAutoLinkMode(vararg modes: Mode) {
        this.modes.addAll(modes)
    }

    fun addSpan(mode: Mode, vararg spans: CharacterStyle) {
        spanMap[mode] = spans.toHashSet()
        Log.e("spanMap", "spanMap ${spanMap[mode]?.size}")
    }

    fun onAutoLinkClick(body: (Mode, String) -> Unit) {
        onAutoLinkClick = body
    }

    fun addUrlTransformations(vararg pairs: Pair<String, String>) {
        transformations.putAll(pairs.toMap())
    }

    private fun makeSpannableString(text: CharSequence): SpannableString {

        var autoLinkItems: Set<AutoLinkItem> = HashSet<AutoLinkItem>()
        val autoLinkItemsTime = measureTimeMillis {
            autoLinkItems = matchedRanges(text)
        }
        var transformedText = ""

        val transformTIme = measureTimeMillis {
            transformedText = transformLinks(text, autoLinkItems)
        }
        val spannableString = SpannableString(transformedText)

        Log.e("makeSpannableString", "autoLinkItems ${autoLinkItems.size}")
        Log.e("span", "spanMap ${spanMap.size}")
        Log.e("matchedRanges", "time ${autoLinkItemsTime}")
        Log.e("transformLinks", "time ${transformTIme}")

        val addSpanTime = measureTimeMillis {
            for (autoLinkItem in autoLinkItems) {
                val mode = autoLinkItem.mode
                val currentColor = getColorByMode(mode)

                val clickableSpan = object : TouchableSpan(currentColor, defaultSelectedColor) {
                    override fun onClick(widget: View) {
                        onAutoLinkClick?.invoke(mode, autoLinkItem.originalText)
                    }
                }

                spannableString.addSpan(clickableSpan, autoLinkItem)

                val addSpanTimeForEach = measureTimeMillis {
                    spanMap[mode]?.forEach {
                        spannableString.addSpan(CharacterStyle.wrap(it), autoLinkItem)
                    }
                }
                Log.e("addSpanTimeForEach", "time ${addSpanTimeForEach}")
            }
        }
        Log.e("addSpanTime", "time ${addSpanTime}")
        return spannableString
    }

    private fun transformLinks(text: CharSequence, autoLinkItems: Set<AutoLinkItem>): String {
        if (transformations.isEmpty())
            return text.toString()

        val stringBuilder = StringBuilder(text)
        var shift = 0

        autoLinkItems
                .sortedBy { it.startPoint }
                .forEach {
                    if (it.mode is MODE_URL && it.originalText != it.transformedText) {
                        val originalTextLength = it.originalText.length
                        val transformedTextLength = it.transformedText.length
                        val diff = originalTextLength - transformedTextLength
                        shift += diff
                        it.startPoint = it.startPoint - shift + diff
                        it.endPoint = it.startPoint + transformedTextLength
                        stringBuilder.replace(it.startPoint, it.startPoint + originalTextLength, it.transformedText)
                    } else if (shift > 0) {
                        it.startPoint = it.startPoint - shift
                        it.endPoint = it.startPoint + it.originalText.length
                    }
                }
        return stringBuilder.toString()
    }

    private fun matchedRanges(text: CharSequence): Set<AutoLinkItem> {
        if (modes.isEmpty()) {
            throw NullPointerException("Please add at least one mode")
        }

        val autoLinkItems = mutableSetOf<AutoLinkItem>()
        modes.sortedBy { it.modeName }
                .forEach {
                    val matcher = it.toPattern().matcher(text)
                    while (matcher.find()) {
                        val group = matcher.group()
                        val startPoint = matcher.start()
                        val endPoint = matcher.end()
                        when (it) {
                            is MODE_PHONE -> if (group.length > MIN_PHONE_NUMBER_LENGTH) {
                                val item = AutoLinkItem(startPoint, endPoint, group, group, it)
                                autoLinkItems.add(item)
                            }
                            else -> {
                                val matchedText = if (it is MODE_URL && transformations.containsKey(group)) {
                                    transformations[group] ?: group
                                } else {
                                    group
                                }
                                val item = AutoLinkItem(startPoint, endPoint, group,
                                        transformedText = matchedText, mode = it)
                                autoLinkItems.add(item)
                            }
                        }
                    }
                }
        return autoLinkItems
    }

    private fun SpannableString.addSpan(span: Any, autoLinkItem: AutoLinkItem) {
        setSpan(span, autoLinkItem.startPoint, autoLinkItem.endPoint, SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun getColorByMode(mode: Mode): Int {
        return when (mode) {
            is MODE_HASHTAG -> hashTagModeColor
            is MODE_MENTION -> mentionModeColor
            is MODE_URL -> urlModeColor
            is MODE_PHONE -> phoneModeColor
            is MODE_EMAIL -> emailModeColor
            is MODE_CUSTOM -> customModeColor
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var field: Field? = null
        val staticField = DynamicLayout::class.java.getDeclaredField("sStaticLayout")
        staticField.isAccessible = true
        val layout: StaticLayout? = staticField.get(DynamicLayout::class.java) as? StaticLayout?
        if (layout != null) {
            field = StaticLayout::class.java.getDeclaredField("mMaximumVisibleLineCount")
            field.isAccessible = true
            field.setInt(layout, maxLines)
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (layout != null && field != null) {
            field.setInt(layout, Integer.MAX_VALUE)
        }
    }
}
