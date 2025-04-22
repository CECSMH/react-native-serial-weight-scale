package com.serialweightscale.exceptions

class UnstableWeightException(message: String, rawResponse: String? = null) :
    ScaleException(ErrorType.UNSTABLE_WEIGHT, -1, message, rawResponse)