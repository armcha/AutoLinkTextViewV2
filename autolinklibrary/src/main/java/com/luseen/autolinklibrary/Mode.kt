package com.luseen.autolinklibrary

sealed class Mode(val modeName: String)

object MODE_HASHTAG : Mode("Hashtag")
object MODE_MENTION : Mode("Mention")
object MODE_URL : Mode("Url")
object MODE_PHONE : Mode("Phone")
object MODE_EMAIL : Mode("Email")
class MODE_CUSTOM(val regex: String) : Mode("Custom")



