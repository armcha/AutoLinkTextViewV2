package com.luseen.autolinklibrary

import android.util.Log

import java.util.regex.Pattern

fun Mode.toPattern(): Pattern {
    return when (this) {
        is MODE_HASHTAG -> HASHTAG_PATTERN
        is MODE_MENTION -> MENTION_PATTERN
        is MODE_PHONE -> PHONE_PATTERN
        is MODE_EMAIL -> EMAIL_PATTERN
        is MODE_URL -> URL_PATTERN
        is MODE_CUSTOM -> {
            if (regex.isNotEmpty() && regex.length > 2) {
                Pattern.compile(regex)
            } else {
                Log.w(AutoLinkTextView.TAG, "Your custom regex is null, returning URL_PATTERN")
                URL_PATTERN
            }
        }
    }
}
