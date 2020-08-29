package io.github.armcha.autolink

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.Typeface.BOLD
import android.text.DynamicLayout
import android.text.SpannableString
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE
import android.text.StaticLayout
import android.text.style.CharacterStyle
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import java.lang.reflect.Field

class AutoLinkTextView(context: Context, attrs: AttributeSet? = null) : TextView(context, attrs) {

    companion object {
        internal val TAG = AutoLinkTextView::class.java.simpleName
        private const val MIN_PHONE_NUMBER_LENGTH = 7
        private const val MAX_PHONE_NUMBER_LENGTH = 15
        private const val DEFAULT_COLOR = Color.RED
    }

    private val spanMap = mutableMapOf<Mode, HashSet<CharacterStyle>>()
    private val transformations = mutableMapOf<String, String>()
    private val modes = mutableSetOf<Mode>()
    private var onAutoLinkClick: ((AutoLinkItem) -> Unit)? = null
    private var urlProcessor: ((String) -> String)? = null

    var pressedTextColor = Color.LTGRAY
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

    /**
     * Mention color by offset
     * */
    private val spanOffset = ArrayList<Pair<Int,Int>>()
    private var mentionBackgroundColor: Int = 0
    private var mentionTextColor: Int = 0
    private var mentionCornerRadius: Float = 10f
    private var mentionPaddingStart: Float = 20f
    private var mentionPaddingEnd: Float = 20f
    private var mentionMarginStart: Float = 20f
    private var mentionStyle: StyleSpan = StyleSpan(BOLD)
    fun setMentionsByOffset(mentions:ArrayList<Pair<Int,Int>>,backgroundColor: Int,textColor: Int,cornerRadius: Float = 10f, paddingStart: Float= 20f,paddingEnd: Float= 20f,marginStart: Float= 20f
    , mentionStyle:StyleSpan
    ){
        this.spanOffset.addAll(mentions)
        this.mentionBackgroundColor = backgroundColor
        mentionTextColor = textColor

        mentionCornerRadius = cornerRadius
        mentionPaddingStart = paddingStart
        mentionPaddingEnd = paddingEnd
        mentionMarginStart = marginStart
        this.mentionStyle = mentionStyle
    }


    override fun setText(text: CharSequence, type: BufferType) {
        if (text.isEmpty() || modes.isNullOrEmpty()) {
            super.setText(text, type)
            return
        }
        val spannableString = makeSpannableString(text)
        super.setText(spannableString, type)
    }

    fun addAutoLinkMode(vararg modes: Mode) {
        this.modes.addAll(modes)
    }

    fun addSpan(mode: Mode, vararg spans: CharacterStyle) {
        spanMap[mode] = spans.toHashSet()
    }

    fun onAutoLinkClick(body: (AutoLinkItem) -> Unit) {
        onAutoLinkClick = body
    }

    fun addUrlTransformations(vararg pairs: Pair<String, String>) {
        transformations.putAll(pairs.toMap())
    }

    fun attachUrlProcessor(processor: (String) -> String) {
        urlProcessor = processor
    }

    private fun makeSpannableString(text: CharSequence): SpannableString {

        val autoLinkItems = matchedRanges(text)
        val transformedText = transformLinks(text, autoLinkItems)
        val spannableString = SpannableString(transformedText)

        for (autoLinkItem in autoLinkItems) {
            val mode = autoLinkItem.mode
            val currentColor = getColorByMode(mode)

            val clickableSpan = object : TouchableSpan(currentColor, pressedTextColor) {
                override fun onClick(widget: View) {
                    onAutoLinkClick?.invoke(autoLinkItem)
                }
            }

            spannableString.addSpan(clickableSpan, autoLinkItem)
            spanMap[mode]?.forEach {
                spannableString.addSpan(CharacterStyle.wrap(it), autoLinkItem)
            }
        }

        spanOffset.forEach {
            if(it.first>=0 && it.second <= text.length) {
                val tagSpan = RoundedBackgroundSpan(mentionBackgroundColor, mentionTextColor, mentionCornerRadius, mentionPaddingStart, mentionPaddingEnd, mentionMarginStart)
                spannableString.setSpan(tagSpan, it.first, it.second, SPAN_EXCLUSIVE_EXCLUSIVE)
                spannableString.setSpan(mentionStyle,it.first, it.second, SPAN_INCLUSIVE_INCLUSIVE)
            }
        }
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
        val autoLinkItems = mutableSetOf<AutoLinkItem>()
        modes.forEach {
            val matcher = it.toPattern().matcher(text)
            while (matcher.find()) {
                var group = matcher.group()
                var startPoint = matcher.start()
                val endPoint = matcher.end()
                when (it) {
                    is MODE_PHONE -> if (group.length in MIN_PHONE_NUMBER_LENGTH..MAX_PHONE_NUMBER_LENGTH) {
                        val item = AutoLinkItem(startPoint, endPoint, group, group, it)
                        autoLinkItems.add(item)
                    }
                    else -> {
                        val isUrl = it is MODE_URL
                        if (isUrl) {
                            if(startPoint > 0) {
                                startPoint += 1
                            }
                            group = group.trimStart()
                            if (urlProcessor != null) {
                                val transformedUrl = urlProcessor?.invoke(group) ?: group
                                if (transformedUrl != group)
                                    transformations[group] = transformedUrl
                            }
                        }
                        val matchedText = if (isUrl && transformations.containsKey(group)) {
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
