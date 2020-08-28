package io.github.armcha.autolink

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.FontMetricsInt
import android.graphics.RectF
import android.text.style.ReplacementSpan
import androidx.annotation.NonNull


class RoundedBackgroundSpan(private val mBackgroundColor: Int, private val mTextColor: Int, private val mCornerRadius: Float, private val mPaddingStart: Float, private val mPaddingEnd: Float, private val mMarginStart: Float) : ReplacementSpan() {
    override fun getSize(@NonNull paint: Paint, text: CharSequence, start: Int, end: Int, fm: FontMetricsInt?): Int {
        return (mPaddingStart + paint.measureText(text.subSequence(start, end).toString()) + mPaddingEnd).toInt()
    }

    override fun draw(@NonNull canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, @NonNull paint: Paint) {
        val width = paint.measureText(text.subSequence(start, end).toString())
        val rect = RectF(x - mPaddingStart + mMarginStart, top.toFloat(), x + width + mPaddingEnd + mMarginStart, bottom.toFloat())
        paint.color = mBackgroundColor
        canvas.drawRoundRect(rect, mCornerRadius, mCornerRadius, paint)
        paint.color = mTextColor
        canvas.drawText(text, start, end, x + mMarginStart, y.toFloat()-4, paint)
    }
}