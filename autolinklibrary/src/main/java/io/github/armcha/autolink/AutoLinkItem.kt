package io.github.armcha.autolink

data class AutoLinkItem(var startPoint: Int,
                        var endPoint: Int,
                        val originalText: String,
                        val transformedText: String,
                        val mode: Mode)
