package com.luseen.autolinklibrary

sealed class Mode(val modeName: String, internal val autoLinkMode: AutoLinkMode)

object MODE_HASHTAG : Mode("Hashtag", AutoLinkMode.HASH_TAG)
object MODE_MENTION : Mode("Mention", AutoLinkMode.MENTION)
object MODE_URL : Mode("Url", AutoLinkMode.URL)
object MODE_PHONE : Mode("Phone", AutoLinkMode.PHONE)
object MODE_EMAIL : Mode("Email", AutoLinkMode.EMAIL)
class MODE_CUSTOM(val regex: String) : Mode("Custom", AutoLinkMode.CUSTOM)

internal sealed class AutoLinkMode {

    object HASH_TAG : AutoLinkMode()
    object MENTION : AutoLinkMode()
    object URL : AutoLinkMode()
    object PHONE : AutoLinkMode()
    object EMAIL : AutoLinkMode()
    object CUSTOM : AutoLinkMode()
}


