package com.luseen.activetextview

import android.graphics.Typeface
import android.os.Bundle
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.luseen.autolinklibrary.*
import kotlinx.android.synthetic.main.activity_recycler_view.*


class RecyclerViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_view)

        recyclerView.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = layoutInflater.inflate(R.layout.recycler_item, parent, false)
                return object : RecyclerView.ViewHolder(view) {}
            }

            override fun getItemCount() = 200

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val autoLinkTextView = holder.itemView.findViewById<AutoLinkTextView>(R.id.autoLinkTextView)

                val custom = MODE_CUSTOM("\\sAllo\\b")
                autoLinkTextView.addAutoLinkMode(
                        MODE_URL,
                        MODE_EMAIL,
                        MODE_HASHTAG,
                        MODE_MENTION,
                        MODE_PHONE,
                        custom)

                autoLinkTextView.addUrlTransformations(
                        "https://google.com" to "GOOGLE",
                        "https://allo.google.com" to "ALLO",
                        "https://www.youtube.com/watch?v=pwfKLfqoMeM" to "WATCH THIS VIDEO")

                autoLinkTextView.addSpan(MODE_URL, StyleSpan(Typeface.BOLD_ITALIC), UnderlineSpan())
                autoLinkTextView.addSpan(MODE_HASHTAG, UnderlineSpan())

                val context = holder.itemView.context
                autoLinkTextView.hashTagModeColor = ContextCompat.getColor(context, R.color.color2)
                autoLinkTextView.phoneModeColor = ContextCompat.getColor(context, R.color.color3)
                autoLinkTextView.customModeColor = ContextCompat.getColor(context, R.color.color1)
                autoLinkTextView.mentionModeColor = ContextCompat.getColor(context, R.color.color5)
                autoLinkTextView.emailModeColor = ContextCompat.getColor(context, R.color.colorPrimary)

                autoLinkTextView.text = getString(R.string.long_text)

                autoLinkTextView.onAutoLinkClick { mode, matchedText ->
                    Toast.makeText(holder.itemView.context, matchedText, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
