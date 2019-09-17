package com.luseen.activetextview

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.luseen.autolinklibrary.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val autoLinkTextView = findViewById<AutoLinkTextView>(R.id.active)

        autoLinkTextView.addAutoLinkMode(
                MODE_HASHTAG,
                MODE_PHONE,
                MODE_URL,
                MODE_EMAIL,
                MODE_MENTION,
                MODE_CUSTOM("\\sAllo\\b"))

        autoLinkTextView.setBoldAutoLinkModes(MODE_HASHTAG)
        autoLinkTextView.setUnderLineAutoLinkModes(MODE_URL, MODE_HASHTAG)

        autoLinkTextView.setHashTagModeColor(ContextCompat.getColor(this, R.color.color2))
        autoLinkTextView.setPhoneModeColor(ContextCompat.getColor(this, R.color.color3))
        autoLinkTextView.setCustomModeColor(ContextCompat.getColor(this, R.color.color1))
        autoLinkTextView.setMentionModeColor(ContextCompat.getColor(this, R.color.color5))

        autoLinkTextView.text = getString(R.string.aaaaaaa)

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
