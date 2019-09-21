package com.luseen.autolinklibrary

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.DynamicLayout
import android.text.SpannableString
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.StaticLayout
import android.text.TextUtils
import android.text.style.CharacterStyle
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import java.lang.reflect.Field
import java.util.regex.Matcher

class AutoLinkTextView(context: Context, attrs: AttributeSet? = null) : TextView(context, attrs) {

    companion object {
        internal val TAG = AutoLinkTextView::class.java.simpleName
        private const val MIN_PHONE_NUMBER_LENGTH = 8
        private const val DEFAULT_COLOR = Color.RED
    }

    private var onAutoLinkClick: ((Mode, String) -> Unit)? = null
    private var modes: MutableList<Mode> = mutableListOf()
    private var defaultSelectedColor = Color.LTGRAY
    private var spanMap = mutableMapOf<Mode, List<CharacterStyle>>()

    var mentionModeColor = DEFAULT_COLOR
    var hashTagModeColor = DEFAULT_COLOR
    var urlModeColor = DEFAULT_COLOR
    var phoneModeColor = DEFAULT_COLOR
    var emailModeColor = DEFAULT_COLOR
    var customModeColor = DEFAULT_COLOR

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

        val spannableString = SpannableString(text)
        val autoLinkItems = matchedRanges(text)

        fun SpannableString.addSpan(span: Any, autoLinkItem: AutoLinkItem) {
            setSpan(span, autoLinkItem.startPoint, autoLinkItem.endPoint, SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        for (autoLinkItem in autoLinkItems) {
            val mode = autoLinkItem.mode
            val currentColor = getColorByMode(mode)

            val clickableSpan = object : TouchableSpan(currentColor, defaultSelectedColor) {
                override fun onClick(widget: View) {
                    onAutoLinkClick?.invoke(mode, autoLinkItem.matchedText)
                }
            }

            spannableString.addSpan(clickableSpan, autoLinkItem)

            val spans = spanMap[mode]
            if (spans != null && spans.isNotEmpty()) {
                spans.forEach {
                    spannableString.addSpan(CharacterStyle.wrap(it), autoLinkItem)
                }
            }
        }
        return spannableString
    }

    private fun matchedRanges(text: CharSequence): Set<AutoLinkItem> {

        val autoLinkItems = mutableSetOf<AutoLinkItem>()

        if (modes.isEmpty()) {
            throw NullPointerException("Please add at least one mode")
        }

        modes.sortedBy { it.modeName }
                .forEach {
                    val matcher = it.toPattern().matcher(text)
                    if (it is MODE_PHONE) {
                        while (matcher.find())
                            if (matcher.group().length > MIN_PHONE_NUMBER_LENGTH)
                                addAutoLinkItem(matcher, autoLinkItems, it)
                    } else {
                        while (matcher.find()) {
                            addAutoLinkItem(matcher, autoLinkItems, it)
                        }
                    }
                }
        return autoLinkItems
    }

    private fun addAutoLinkItem(matcher: Matcher, autoLinkItems: MutableSet<AutoLinkItem>, mode: Mode) {
        autoLinkItems.add(AutoLinkItem(
                matcher.start(),
                matcher.end(),
                matcher.group(),
                mode))
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
