package com.serialweightscale.exceptions

class InvalidScaleIdException(message: String, rawResponse: String? = null) :
    ScaleException(ErrorType.INVALID_SCALE_ID, -101, message, rawResponse)