package io.github.armcha

import android.graphics.Typeface
import android.os.Bundle
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.text.style.UnderlineSpan
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import io.github.armcha.autolink.*
import kotlinx.android.synthetic.main.activity_static_text.*

class StaticTextActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_static_text)

        val custom = MODE_CUSTOM("\\sAndroid\\b")
        autoLinkTextView.addAutoLinkMode(
                MODE_HASHTAG,
                MODE_EMAIL,
                MODE_URL,
                MODE_PHONE,
                custom,
                MODE_MENTION)

        autoLinkTextView.addUrlTransformations(
                "https://en.wikipedia.org/wiki/Wear_OS" to "Wear OS",
                "https://en.wikipedia.org/wiki/Fire_OS" to "FIRE")

        autoLinkTextView.attachUrlProcessor {
            when {
                it.contains("google") -> "Google"
                it.contains("github") -> "Github"
                else -> it
            }
        }

        autoLinkTextView.addSpan(MODE_URL, StyleSpan(Typeface.ITALIC), UnderlineSpan())
        autoLinkTextView.addSpan(MODE_HASHTAG, UnderlineSpan(), TypefaceSpan("monospace"))
        autoLinkTextView.addSpan(custom, StyleSpan(Typeface.BOLD))

        autoLinkTextView.hashTagModeColor = ContextCompat.getColor(this, R.color.color5)
        autoLinkTextView.phoneModeColor = ContextCompat.getColor(this, R.color.color3)
        autoLinkTextView.customModeColor = ContextCompat.getColor(this, R.color.color1)
        autoLinkTextView.mentionModeColor = ContextCompat.getColor(this, R.color.color6)
        autoLinkTextView.emailModeColor = ContextCompat.getColor(this, R.color.colorPrimary)

        autoLinkTextView.text = getString(R.string.android_text)

        autoLinkTextView.onAutoLinkClick {
            val message = if (it.originalText == it.transformedText) it.originalText
            else "Original text - ${it.originalText} \n\nTransformed text - ${it.transformedText}"
            val url = if (it.mode is MODE_URL) it.originalText else null
            showDialog(it.mode.modeName, message, url)
        }
    }
}
