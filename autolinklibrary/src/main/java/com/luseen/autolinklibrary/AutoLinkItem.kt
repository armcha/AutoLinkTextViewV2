package com.luseen.autolinklibrary

internal class AutoLinkItem(val startPoint: Int,
                                 val endPoint: Int,
                                 val matchedText: String,
                                 val mode: Mode){

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AutoLinkItem
        if (endPoint != other.endPoint) return false
        return true
    }

    override fun hashCode(): Int {
        return endPoint
    }
}
