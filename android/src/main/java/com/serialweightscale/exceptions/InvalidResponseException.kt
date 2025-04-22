package com.serialweightscale.exceptions

class InvalidResponseException(message: String, rawResponse: String? = null) :
    ScaleException(ErrorType.INVALID_RESPONSE, 0, message, rawResponse)