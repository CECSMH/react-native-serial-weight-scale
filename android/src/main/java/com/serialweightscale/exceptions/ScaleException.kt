package com.serialweightscale.exceptions

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap

open class ScaleException(
    val type: ErrorType,
    val code: Int,
    message: String,
    val rawResponse: String? = null
) : Exception(message) {
    fun toMap(): WritableMap = Arguments.createMap().apply {
        putString("type", type.value)
        putInt("code", code)
        putString("message", message)
        rawResponse?.let { putString("rawResponse", it) }
    }
}