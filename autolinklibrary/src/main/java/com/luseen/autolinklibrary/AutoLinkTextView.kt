package com.luseen.autolinklibrary

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.*
import android.text.style.CharacterStyle
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
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
    private var boldModes: MutableList<Mode> = mutableListOf()
    private var underLineModes: MutableList<Mode> = mutableListOf()
    private var mentionModeColor = DEFAULT_COLOR
    private var hashTagModeColor = DEFAULT_COLOR
    private var urlModeColor = DEFAULT_COLOR
    private var phoneModeColor = DEFAULT_COLOR
    private var emailModeColor = DEFAULT_COLOR
    private var customModeColor = DEFAULT_COLOR
    private var defaultSelectedColor = Color.LTGRAY
    private var spanMap = mutableMapOf<Mode, CharacterStyle>()

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

    private fun makeSpannableString(text: CharSequence): SpannableString {

        val spannableString = SpannableString(text)
        val autoLinkItems = matchedRanges(text)

        fun SpannableString.addSpan(span: Any, autoLinkItem: AutoLinkItem) {
            setSpan(span, autoLinkItem.startPoint, autoLinkItem.endPoint,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        for (autoLinkItem in autoLinkItems) {
            val currentColor = getColorByMode(autoLinkItem.mode)
            val isUnderLineEnabled = underLineModes.contains(autoLinkItem.mode)

            val clickableSpan = object : TouchableSpan(currentColor, defaultSelectedColor, isUnderLineEnabled) {
                override fun onClick(widget: View) {
                    onAutoLinkClick?.invoke(autoLinkItem.mode, autoLinkItem.matchedText)
                }
            }

            spannableString.addSpan(clickableSpan, autoLinkItem)

            if (boldModes.contains(autoLinkItem.mode)) {
                spannableString.addSpan(StyleSpan(Typeface.BOLD), autoLinkItem)
            }
            spannableString.addSpan(TypefaceSpan("monospace"), autoLinkItem)
        }
        return spannableString
    }

    private fun matchedRanges(text: CharSequence): List<AutoLinkItem> {

        val autoLinkItems = mutableListOf<AutoLinkItem>()

        if (modes.isEmpty()) {
            throw NullPointerException("Please add at least one mode")
        }

        modes.forEach {
            val matcher = it.toPattern().matcher(text)
            if (it.autoLinkMode == AutoLinkMode.PHONE) {
                while (matcher.find())
                    if (matcher.group().length > MIN_PHONE_NUMBER_LENGTH)
                        addAutoLinkItem(matcher, autoLinkItems, it)
            } else {
                while (matcher.find())
                    addAutoLinkItem(matcher, autoLinkItems, it)
            }
        }
        return autoLinkItems
    }

    private fun addAutoLinkItem(matcher: Matcher, autoLinkItems: MutableList<AutoLinkItem>, mode: Mode) {
        autoLinkItems.add(AutoLinkItem(
                matcher.start(),
                matcher.end(),
                matcher.group(),
                mode))
    }

    private fun getColorByMode(mode: Mode): Int {
        return when (mode.autoLinkMode) {
            AutoLinkMode.HASH_TAG -> hashTagModeColor
            AutoLinkMode.MENTION -> mentionModeColor
            AutoLinkMode.URL -> urlModeColor
            AutoLinkMode.PHONE -> phoneModeColor
            AutoLinkMode.EMAIL -> emailModeColor
            AutoLinkMode.CUSTOM -> customModeColor
        }
    }

    fun setMentionModeColor(@ColorInt mentionModeColor: Int) {
        this.mentionModeColor = mentionModeColor
    }

    fun setHashTagModeColor(@ColorInt hashTagModeColor: Int) {
        this.hashTagModeColor = hashTagModeColor
    }

    fun setUrlModeColor(@ColorInt urlModeColor: Int) {
        this.urlModeColor = urlModeColor
    }

    fun setPhoneModeColor(@ColorInt phoneModeColor: Int) {
        this.phoneModeColor = phoneModeColor
    }

    fun setEmailModeColor(@ColorInt emailModeColor: Int) {
        this.emailModeColor = emailModeColor
    }

    fun setCustomModeColor(@ColorInt customModeColor: Int) {
        this.customModeColor = customModeColor
    }

    fun setSelectedStateColor(@ColorInt defaultSelectedColor: Int) {
        this.defaultSelectedColor = defaultSelectedColor
    }

    fun addAutoLinkMode(vararg modes: Mode) {
        this.modes.addAll(modes)
    }

    fun setBoldAutoLinkModes(vararg modes: Mode) {
        boldModes.addAll(modes)
    }

    fun setUnderLineAutoLinkModes(vararg modes: Mode) {
        underLineModes.addAll(modes)
    }

    fun onAutoLinkClick(body: (Mode, String) -> Unit) {
        onAutoLinkClick = body
    }

    override fun setTypeface(tf: Typeface?) {
        super.setTypeface(tf)
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
