package com.serialweightscale.handlers

import com.serialweightscale.exceptions.*
import com.serialweightscale.utils.*

class ToledoHandler(model: String?) : BaseHandler("toledo", model ?: "general") {
    private val isStreaming: Boolean = model == "general"
    private var decimals: Int = 1000

    override fun getCommand(): String? = if (isStreaming) null else Constants.ENQ

    override fun parseResponse(response: String): Double {
        if (response.isEmpty()) throw InvalidResponseException("Empty response", response)

        if (isStreaming) {
            val weight_str = when {
                response.length >= 7 && response.startsWith(Constants.STX) -> response.substring(2, 7).trim()
                else -> response.take(6).trim()
            }
            return ParserUtils.parseWeight(weight_str, 3)
        }

        if (model == "ti420" || response.contains("#96")) {
            if (response.contains("#96")) {
                val weight_str = response.substringAfter("#96").take(6).trim()
                return ParserUtils.parseWeight(weight_str, 3)
            }
            throw UnstableWeightException("Unstable weight", response)
        }

        val initial_pos = response.lastIndexOf(Constants.STX)
        val weight_str: String
        when {
            // Protocol Eth (2090N, possibly Prix 3)
            initial_pos >= 0 && response.length > initial_pos + 61 && response.substring(initial_pos + 1, initial_pos + 3) == "02" && response[initial_pos + 60] == Constants.ETX[0] -> {
                decimals = 1000
                weight_str = response.substring(initial_pos + 6, initial_pos + 12).trim()
            }
            // Protocol A (Prix 3)
            initial_pos >= 0 && response.length > initial_pos + 21 && response[initial_pos + 21] == Constants.CR[0] -> {
                decimals = if (response[initial_pos + 8].code and 0x08 != 0) 100 else 1000 // Bit 3 of S2
                weight_str = response.substring(initial_pos + 2, initial_pos + 8).trim()
            }
            // Protocol B (Prix 3)
            initial_pos >= 0 && response.indexOf(Constants.ETX, initial_pos + 1) > 0 -> {
                decimals = 1000
                val posFim = response.indexOf(Constants.ETX, initial_pos + 1)
                weight_str = if (posFim > 0) response.substring(initial_pos + 1, posFim).trim() else response.substring(initial_pos + 1).trim()
            }
            // Protocol P03 (9091/8530/8540, possibly Prix 3)
            initial_pos >= 0 && (response.length >= initial_pos + 17 && response[initial_pos + 16] == Constants.CR[0] || response.indexOf(Constants.CR, initial_pos + 1) - initial_pos == 16) -> {
                decimals = if (initial_pos + 1 < response.length && response[initial_pos + 1].code and 0x08 != 0) 100 else 1000 // Bit 3 of SWA
                weight_str = response.substring(initial_pos + 4, initial_pos + 10).trim()
            }
            // Protocol C (BCS21, Prix 3)
            initial_pos >= 0 && response.indexOf(Constants.CR, initial_pos + 1) > 0 -> {
                decimals = 1000
                val posFim = response.indexOf(Constants.CR, initial_pos + 1)
                weight_str = if (posFim > 0) response.substring(initial_pos + 1, posFim).trim() else response.substring(initial_pos + 1).trim()
                weight_str.replace("kg", "", ignoreCase = true) // Remove "kg" 
            }
            else -> throw InvalidResponseException("Unknown protocol", response)
        }
        // BCS21-specific
        when {
            weight_str.startsWith("D+") || weight_str.startsWith("D-") -> throw UnstableWeightException("Unstable weight", response)
            weight_str.startsWith("S-") -> throw NegativeWeightException("Negative weight", response)
            weight_str.startsWith("S+") -> {
                if (weight_str.length < 10) throw InvalidResponseException("Invalid length", response)
                return ParserUtils.parseWeight(weight_str.substring(2, 10).trim(), if (decimals == 100) 2 else 3)
            }
            weight_str.contains("s", ignoreCase = true) -> throw OverloadException("Overload", response)
            weight_str.contains("z", ignoreCase = true) -> throw ZeroCaptureException("Zero capture", response)
            weight_str.contains("c", ignoreCase = true) -> throw CalibrationException("Calibration error", response)
        }

        when (weight_str.trim().firstOrNull()?.lowercaseChar()) {
            'i' -> throw UnstableWeightException("Unstable weight", response)
            'n' -> throw NegativeWeightException("Negative weight", response)
            's' -> throw OverloadException("Overload", response)
            'c' -> throw ZeroCaptureException("Zero capture", response)
            'e' -> throw CalibrationException("Calibration error", response)
        }
        return ParserUtils.parseWeight(weight_str, if (decimals == 100) 2 else 3)
    }
}