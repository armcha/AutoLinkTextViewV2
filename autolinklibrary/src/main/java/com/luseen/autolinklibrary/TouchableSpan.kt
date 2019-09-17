package com.luseen.autolinklibrary

import android.graphics.Color
import android.text.TextPaint
import android.text.style.ClickableSpan

internal abstract class TouchableSpan(private val normalTextColor: Int,
                                      private val pressedTextColor: Int,
                                      private val isUnderLineEnabled: Boolean) : ClickableSpan() {

    var isPressed: Boolean = false

    override fun updateDrawState(textPaint: TextPaint) {
        super.updateDrawState(textPaint)
        val textColor = if (isPressed) pressedTextColor else normalTextColor
        with(textPaint) {
            isAntiAlias = true
            color = textColor
            bgColor = Color.TRANSPARENT
            isUnderlineText = isUnderLineEnabled
        }
    }
}