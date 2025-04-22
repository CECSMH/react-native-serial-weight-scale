package com.serialweightscale.exceptions;

class CalibrationException(message: String, rawResponse: String? = null) :
    ScaleException(ErrorType.CALIBRATION_ERROR, -12, message, rawResponse)