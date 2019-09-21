package com.luseen.autolinklibrary

import android.util.Patterns
import java.util.regex.Pattern

val PHONE_PATTERN: Pattern = Patterns.PHONE
val EMAIL_PATTERN: Pattern = Patterns.EMAIL_ADDRESS
val HASHTAG_PATTERN: Pattern = Pattern.compile("(?<![a-zA-Z0-9_])#(?=[0-9_]*[a-zA-Z])[a-zA-Z0-9_]+")
val MENTION_PATTERN: Pattern = Pattern.compile("(?:^|\\s|$|[.])@[\\p{L}0-9_]*")
val URL_PATTERN = Patterns.WEB_URL
