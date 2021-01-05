# AutoLinkTextView V2

[1]: https://github.com/armcha/AutoLinkTextView
AutoLinkTextViewV2 is the new version of the [AutoLinkTextView][1].

**The main differences between the old and new version are**
- Fully migration to Kotlin
- Added several new features
- Some improvements and fixes

**It supports automatic detection and click handling for**
* Hashtags (#)
* Mentions (@) 
* URLs (https://)
* Phone Numbers
* Emails
* Multiple Custom Regex

.<img src="screens/static.png" width="400">
<img src="screens/static_gif.gif" width="400">

The current minSDK version is API level 16.

### Download sample [apk][77]
[77]: https://github.com/armcha/AutoLinkTextViewV2/blob/master/screens/AutoLinkTextView.apk

### Features

* Default support for **Hashtag, Mention, Link, Phone number and Email**
* Support for **custom types** via regex
* Transform url to short clickable text
* Ability to apply **multiple spans** to any mode
* Ability to set specific text color
* Ability to set pressed state color

-----------------------

### Download

Gradle:
```groovy
implementation 'com.github.armcha:AutoLinkTextViewV2:3.0.0'
```

### Setup and Usage

Add AutoLinkTextView to your layout
```xml
<io.github.armcha.autolink.AutoLinkTextView
    android:id="@+id/autolinkTextView"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content" />
```

```kotlin
val autoLinkTextView = findViewById<AutoLinkTextView>(R.id.autolinkTextView);
```

Add one or multiple modes
```kotlin
autoLinkTextView.addAutoLinkMode(
                MODE_HASHTAG,
                MODE_URL)
```
-----------------------
Add url transformations for transforming them to short clickable text
```kotlin
autoLinkTextView.addUrlTransformations(
                "https://google.com" to "Google",
                "https://en.wikipedia.org/wiki/Wear_OS" to "Wear OS")
```

Or you can attach urlProcessor and transform it
```kotlin
autoLinkTextView.attachUrlProcessor { originalUrl: String ->
    when {
        originalUrl.startsWith("https://en.wikipedia") -> "Wiki"
        originalUrl.contains("android") -> "Android"
        else -> originalUrl
    }
}
```

<img src="screens/transformation_before.png" width="400"> <img src="screens/transformation_after.png" width="400">
-----------------------
Add one or multiple spans to specific mode
```kotlin
autoLinkTextView.addSpan(MODE_URL, StyleSpan(Typeface.ITALIC), UnderlineSpan())
autoLinkTextView.addSpan(MODE_HASHTAG, UnderlineSpan(), TypefaceSpan("monospace"))
```
-----------------------
Set AutoLinkTextView click listener
```kotlin
autoLinkTextView.onAutoLinkClick { item: AutoLinkItem ->
}
```
-----------------------
Set text to AutoLinkTextView
```kotlin
autoLinkTextView.text = getString(R.string.android_text)
```


Customizing
---------

All possible modes

#### MODE_PHONE

<img src="screens/phone.png" width="400">

#### MODE_HASHTAG

<img src="screens/hashtag.png" width="400">

#### MODE_URL

<img src="screens/url.png" width="400">

#### MODE_MENTION

<img src="screens/mention.png" width="400">

#### MODE_EMAIL

<img src="screens/gmail.png" width="400">

#### MODE_CUSTOM

<img src="screens/custom.png" width="400">

For use of custom mode you can add multiple custom regex

```kotlin
val custom = MODE_CUSTOM("\\sAndroid\\b", "\\sGoogle\\b")
```
-------------------------
You can change text color for the specific mode
```kotlin
autoLinkTextView.hashTagModeColor = ContextCompat.getColor(this, R.color.color2)
autoLinkTextView.phoneModeColor = ContextCompat.getColor(this, R.color.color3)
```
-------------------------
You can also change pressed text color
```kotlin
autoLinkTextView.pressedTextColor = ContextCompat.getColor(this, R.color.pressedTextColor)
```

### Contact :book:

:arrow_forward:  **Email**: chatikyana@gmail.com

:arrow_forward:  **LinkedIn**: https://www.linkedin.com/in/chatikyan

:arrow_forward:  **Medium**: https://medium.com/@chatikyan

:arrow_forward:  **Twitter**: https://twitter.com/ChatikyanArman

License
--------


      Auto Link TextView V2 library for Android
      Copyright (c) 2021 Arman Chatikyan (https://github.com/armcha/AutoLinkTextViewV2).

      Licensed under the Apache License, Version 2.0 (the "License");
      you may not use this file except in compliance with the License.
      You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

      Unless required by applicable law or agreed to in writing, software
      distributed under the License is distributed on an "AS IS" BASIS,
      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
      See the License for the specific language governing permissions and
      limitations under the License.
