package com.serialweightscale.exceptions

class ZeroCaptureException(message: String, rawResponse: String? = null) :
    ScaleException(ErrorType.ZERO_CAPTURE, -11, message, rawResponse)