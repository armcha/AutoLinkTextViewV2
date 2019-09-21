package com.luseen.autolinklibrary

import android.text.Selection
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.view.MotionEvent
import android.widget.TextView

internal class LinkTouchMovementMethod : LinkMovementMethod() {

    private var pressedSpan: TouchableSpan? = null

    override fun onTouchEvent(textView: TextView, spannable: Spannable, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                pressedSpan = getPressedSpan(textView, spannable, event)
                if (pressedSpan != null) {
                    pressedSpan?.isPressed = true
                    Selection.setSelection(spannable, spannable.getSpanStart(pressedSpan),
                            spannable.getSpanEnd(pressedSpan))
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val touchedSpan = getPressedSpan(textView, spannable, event)
                if (pressedSpan != null && touchedSpan != pressedSpan) {
                    pressedSpan?.isPressed = false
                    pressedSpan = null
                    Selection.removeSelection(spannable)
                }
            }
            else -> {
                if (pressedSpan != null) {
                    pressedSpan?.isPressed = false
                    super.onTouchEvent(textView, spannable, event)
                }
                pressedSpan = null
                Selection.removeSelection(spannable)
            }
        }
        return true
    }

    private fun getPressedSpan(textView: TextView, spannable: Spannable, event: MotionEvent): TouchableSpan? {

        var x = event.x.toInt()
        var y = event.y.toInt()

        x -= textView.totalPaddingLeft
        y -= textView.totalPaddingTop

        x += textView.scrollX
        y += textView.scrollY

        val layout = textView.layout
        val verticalLine = layout.getLineForVertical(y)
        val horizontalOffset = layout.getOffsetForHorizontal(verticalLine, x.toFloat())

        val link = spannable.getSpans(horizontalOffset, horizontalOffset, TouchableSpan::class.java)
        return link.getOrNull(0)
    }
}
