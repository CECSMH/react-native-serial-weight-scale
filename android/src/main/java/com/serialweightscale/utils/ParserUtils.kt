package com.serialweightscale.utils

import com.serialweightscale.exceptions.InvalidResponseException

object ParserUtils {
    fun parseWeight(weightStr: String, decimals: Int): Double {
        try {
            val cleaned = weightStr.replace("[^0-9]".toRegex(), "").toLong()
            return cleaned / Math.pow(10.0, decimals.toDouble())
        } catch (e: Exception) {
            throw InvalidResponseException("Invalid weight format: $weightStr", weightStr)
        }
    }
}