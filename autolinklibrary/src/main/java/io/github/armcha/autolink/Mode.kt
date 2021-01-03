package io.github.armcha.autolink

import android.util.Log
import java.util.regex.Pattern

sealed class Mode(val modeName: String)

object MODE_HASHTAG : Mode("Hashtag")
object MODE_MENTION : Mode("Mention")
object MODE_URL : Mode("Url")
object MODE_PHONE : Mode("Phone")
object MODE_EMAIL : Mode("Email")
class MODE_CUSTOM(vararg val regex: String) : Mode("Custom")

fun Mode.toPattern(): List<Pattern> {
    return when (this) {
        is MODE_HASHTAG -> listOf(HASH_TAG_PATTERN)
        is MODE_MENTION -> listOf(MENTION_PATTERN)
        is MODE_PHONE -> listOf(PHONE_PATTERN)
        is MODE_EMAIL -> listOf(EMAIL_PATTERN)
        is MODE_URL -> listOf(URL_PATTERN)
        is MODE_CUSTOM -> {
            regex.map {
                if (it.length > 2) {
                    Pattern.compile(it)
                } else {
                    Log.w(AutoLinkTextView.TAG, "Your custom regex is null, returning URL_PATTERN")
                    URL_PATTERN
                }
            }
        }
    }
}



