package com.serialweightscale.exceptions

class TimeoutException(message: String, rawResponse: String? = null) :
    ScaleException(ErrorType.TIMEOUT, -9, message, rawResponse)