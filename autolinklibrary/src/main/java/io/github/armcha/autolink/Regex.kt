package io.github.armcha.autolink

import android.util.Patterns
import java.util.regex.Pattern

internal val URL_PATTERN = Pattern.compile("(^|[\\s.:;?\\-\\]<\\(])" +
        "((https?://|www\\.|pic\\.)[-\\w;/?:@&=+$\\|\\_.!~*\\|'()\\[\\]%#,☺]+[\\w/#](\\(\\))?)" +
        "(?=$|[\\s',\\|\\(\\).:;?\\-\\[\\]>\\)])")
internal val PHONE_PATTERN: Pattern = Patterns.PHONE
internal val EMAIL_PATTERN: Pattern = Patterns.EMAIL_ADDRESS
internal val MENTION_PATTERN: Pattern = Pattern.compile("(?:^|\\s|$|[.])@[\\p{L}0-9_]*")
internal val HASH_TAG_PATTERN: Pattern = Pattern.compile("(?<![a-zA-Z0-9_])#(?=[0-9_]*[a-zA-Z])[a-zA-Z0-9_]+")
