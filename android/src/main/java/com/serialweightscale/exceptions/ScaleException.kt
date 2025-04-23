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

    fun getType(): String {
        return when (type) {
            ErrorType.UNSTABLE_WEIGHT -> "unstable_weight"
            ErrorType.NEGATIVE_WEIGHT -> "negative_weight"
            ErrorType.TIMEOUT -> "timeout"
            ErrorType.OVERLOAD -> "overload"
            ErrorType.ZERO_CAPTURE -> "zero_capture"
            ErrorType.CALIBRATION_ERROR -> "calibration_error"
            ErrorType.INVALID_RESPONSE -> "invalid_response"
            ErrorType.SERIAL_CONNECTION -> "serial_connection"
            ErrorType.INVALID_SCALE_ID -> "invalid_scale_id"
       }
    }
}