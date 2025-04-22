package com.serialweightscale.exceptions

class NegativeWeightException(message: String, rawResponse: String? = null) :
    ScaleException(ErrorType.NEGATIVE_WEIGHT, -2, message, rawResponse)