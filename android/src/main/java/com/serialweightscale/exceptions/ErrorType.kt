package com.serialweightscale.exceptions

enum class ErrorType(val value: String) {
    UNSTABLE_WEIGHT("unstable_weight"),
    NEGATIVE_WEIGHT("negative_weight"),
    TIMEOUT("timeout"),
    OVERLOAD("overload"),
    ZERO_CAPTURE("zero_capture"),
    CALIBRATION_ERROR("calibration_error"),
    INVALID_RESPONSE("invalid_response"),
    SERIAL_CONNECTION("serial_connection"),
    INVALID_SCALE_ID("invalid_scale_id")
}