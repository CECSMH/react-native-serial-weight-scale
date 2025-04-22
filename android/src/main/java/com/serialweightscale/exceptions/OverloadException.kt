package com.serialweightscale.exceptions

class OverloadException(message: String, rawResponse: String? = null) :
    ScaleException(ErrorType.OVERLOAD, -10, message, rawResponse)