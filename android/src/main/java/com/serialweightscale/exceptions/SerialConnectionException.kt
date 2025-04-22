package com.serialweightscale.exceptions

class SerialConnectionException(message: String, rawResponse: String? = null) :
    ScaleException(ErrorType.SERIAL_CONNECTION, -100, message, rawResponse)