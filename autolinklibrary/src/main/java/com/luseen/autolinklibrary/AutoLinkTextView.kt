package com.luseen.autolinklibrary

import android.content.Context
import android.graphics.Color
import android.text.DynamicLayout
import android.text.SpannableString
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.StaticLayout
import android.text.TextUtils
import android.text.style.CharacterStyle
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import java.lang.reflect.Field

class AutoLinkTextView(context: Context, attrs: AttributeSet? = null) : TextView(context, attrs) {

    companion object {
        internal val TAG = AutoLinkTextView::class.java.simpleName
        private const val MIN_PHONE_NUMBER_LENGTH = 8
        private const val DEFAULT_COLOR = Color.RED
    }

    private var onAutoLinkClick: ((Mode, String) -> Unit)? = null
    private val modes = mutableListOf<Mode>()
    private val defaultSelectedColor = Color.LTGRAY
    private val spanMap = mutableMapOf<Mode, List<CharacterStyle>>()
    private val transformations = mutableMapOf<String, String>()

    var mentionModeColor = DEFAULT_COLOR
    var hashTagModeColor = DEFAULT_COLOR
    var urlModeColor = DEFAULT_COLOR
    var phoneModeColor = DEFAULT_COLOR
    var emailModeColor = DEFAULT_COLOR
    var customModeColor = DEFAULT_COLOR

    init {
        transformations["https://google.com"] = "GOOGLE"
        transformations["https://allo.google.com"] = "ARMAN"
        transformations["https://www.youtube.com/watch?v=pwfKLfqoMeM"] = "YOUTUBE"
    }

    override fun setHighlightColor(color: Int) {
        super.setHighlightColor(Color.TRANSPARENT)
    }

    override fun setText(text: CharSequence, type: BufferType) {
        if (TextUtils.isEmpty(text)) {
            super.setText(text, type)
            return
        }
        val spannableString = makeSpannableString(text)
        movementMethod = LinkTouchMovementMethod()
        super.setText(spannableString, type)
    }

    fun addAutoLinkMode(vararg modes: Mode) {
        this.modes.addAll(modes)
    }

    fun addSpan(mode: Mode, vararg spans: CharacterStyle) {
        spanMap[mode] = spanMap[mode]?.plus(spans) ?: spans.toList()
    }

    fun onAutoLinkClick(body: (Mode, String) -> Unit) {
        onAutoLinkClick = body
    }

    private fun makeSpannableString(text: CharSequence): SpannableString {

        val autoLinkItems = matchedRanges(text)
        val transformedText = transformLinks(text, autoLinkItems)
        val spannableString = SpannableString(transformedText)

        for (autoLinkItem in autoLinkItems) {
            val mode = autoLinkItem.mode
            val currentColor = getColorByMode(mode)

            val clickableSpan = object : TouchableSpan(currentColor, defaultSelectedColor) {
                override fun onClick(widget: View) {
                    onAutoLinkClick?.invoke(mode, autoLinkItem.originalText)
                }
            }

            spannableString.addSpan(clickableSpan, autoLinkItem)

            spanMap[mode]?.forEach {
                spannableString.addSpan(CharacterStyle.wrap(it), autoLinkItem)
            }
        }
        return spannableString
    }

    private fun transformLinks(text: CharSequence, autoLinkItems: Set<AutoLinkItem>): String {
        var changedText = text.toString()
        var shift = 0
        autoLinkItems
                .sortedBy { it.startPoint }
                .forEach {
                    if (it.mode is MODE_URL && it.originalText != it.transformedText) {
                        val diff = it.originalText.length - it.transformedText.length
                        shift += diff
                        changedText = changedText.replace(it.originalText, it.transformedText)
                        it.startPoint = it.startPoint - shift + diff
                        it.endPoint = it.startPoint + it.transformedText.length
                    } else {
                        it.startPoint = it.startPoint - shift
                        it.endPoint = it.startPoint + it.originalText.length
                    }
                }
        return changedText
    }

    private fun matchedRanges(text: CharSequence): Set<AutoLinkItem> {
        if (modes.isEmpty()) {
            throw NullPointerException("Please add at least one mode")
        }

        val autoLinkItems = mutableSetOf<AutoLinkItem>()
        modes.sortedBy { it.modeName }
                .forEach {
                    val matcher = it.toPattern().matcher(text)
                    if (it is MODE_PHONE) {
                        while (matcher.find()) {
                            val group = matcher.group()
                            if (group.length > MIN_PHONE_NUMBER_LENGTH) {
                                val item = AutoLinkItem(matcher.start(), matcher.end(),
                                        matcher.group(), group, it)
                                autoLinkItems.add(item)
                            }
                        }
                    } else {
                        while (matcher.find()) {
                            val group = matcher.group()
                            val matchedText = if (it is MODE_URL) {
                                transformations[group] ?: group
                            } else {
                                group
                            }
                            val item = AutoLinkItem(matcher.start(), matcher.end(),
                                    group, transformedText = matchedText, mode = it)
                            autoLinkItems.add(item)
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
