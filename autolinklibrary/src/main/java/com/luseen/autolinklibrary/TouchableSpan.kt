package com.luseen.autolinklibrary

import android.graphics.Color
import android.text.TextPaint
import android.text.style.ClickableSpan

internal abstract class TouchableSpan(private val normalTextColor: Int,
                                      private val pressedTextColor: Int) : ClickableSpan() {

    var isPressed: Boolean = false

    override fun updateDrawState(textPaint: TextPaint) {
        super.updateDrawState(textPaint)
        val textColor = if (isPressed) pressedTextColor else normalTextColor
        with(textPaint) {
            isAntiAlias = true
            color = textColor
            isUnderlineText = false
            bgColor = Color.TRANSPARENT
        }
    }
}