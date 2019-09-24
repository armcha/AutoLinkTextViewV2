package com.luseen.activetextview

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.luseen.autolinklibrary.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val custom = MODE_CUSTOM("\\sAllo\\b")
        autoLinkTextView.addAutoLinkMode(
                MODE_HASHTAG,
                MODE_PHONE,
                MODE_URL,
                MODE_EMAIL,
                MODE_MENTION,
                custom)

        autoLinkTextView.addSpan(MODE_URL, StyleSpan(Typeface.BOLD_ITALIC), UnderlineSpan())
//        autoLinkTextView.addSpan(MODE_PHONE, RelativeSizeSpan(1f))
//        autoLinkTextView.addSpan(MODE_HASHTAG, BackgroundColorSpan(Color.BLUE), UnderlineSpan(), ForegroundColorSpan(Color.WHITE))

        autoLinkTextView.hashTagModeColor = ContextCompat.getColor(this, R.color.color2)
        autoLinkTextView.phoneModeColor = ContextCompat.getColor(this, R.color.color3)
        autoLinkTextView.customModeColor = ContextCompat.getColor(this, R.color.color1)
        autoLinkTextView.mentionModeColor = ContextCompat.getColor(this, R.color.color5)
        autoLinkTextView.emailModeColor = ContextCompat.getColor(this, R.color.colorPrimary)

        autoLinkTextView.text = getString(R.string.long_text)

        autoLinkTextView.onAutoLinkClick { mode, matchedText ->
            showDialog(mode.modeName, matchedText)
        }
    }

    private fun showDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
                .setTitle(title)
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, id -> dialog.dismiss() }
        val alert = builder.create()
        alert.show()
    }
}
