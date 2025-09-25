package com.serialweightscale.handlers


class ElginHandler() : BaseHandler("elgin", null){
    private var decimals: Int = 3

    override fun getCommand(): String? = Constants.ENQ

    override fun parseResponse(response: String): Double {
        if (response.isEmpty()) throw InvalidResponseException("Empty response", response)

        val initial_pos = response.lastIndexOf(Constants.STX)
        val weight_str: String
        when {
            // Protocol Eth 
            initial_pos >= 0 && response.length > initial_pos + 61 && response.substring(initial_pos + 1, initial_pos + 3) == "02" && response[initial_pos + 60] == Constants.ETX[0] -> {
                decimals = 3
                weight_str = response.substring(initial_pos + 6, initial_pos + 12).trim()
            }
            // Protocol A 
            initial_pos >= 0 && response.length > initial_pos + 21 && response[initial_pos + 21] == Constants.CR[0] -> {
                decimals = if (response[initial_pos + 8].code and 0x08 != 0) 2 else 3 // Bit 3 of S2
                weight_str = response.substring(initial_pos + 2, initial_pos + 8).trim()
            }
            // Protocol B 
            initial_pos >= 0 && response.indexOf(Constants.ETX, initial_pos + 1) > 0 -> {
                decimals = 3
                val posFim = response.indexOf(Constants.ETX, initial_pos + 1)
                weight_str = if (posFim > 0) response.substring(initial_pos + 1, posFim).trim() else response.substring(initial_pos + 1).trim()
            }
            // Protocol P03
            initial_pos >= 0 && (response.length >= initial_pos + 17 && response[initial_pos + 16] == Constants.CR[0] || response.indexOf(Constants.CR, initial_pos + 1) - initial_pos == 16) -> {
                decimals = if (initial_pos + 1 < response.length && response[initial_pos + 1].code and 0x08 != 0) 2 else 3 // Bit 3 of SWA
                weight_str = response.substring(initial_pos + 4, initial_pos + 10).trim()
            }
            // Protocol C 
            initial_pos >= 0 && response.indexOf(Constants.CR, initial_pos + 1) > 0 -> {
                decimals = 3
                val posFim = response.indexOf(Constants.CR, initial_pos + 1)
                weight_str = if (posFim > 0) response.substring(initial_pos + 1, posFim).trim() else response.substring(initial_pos + 1).trim()
                weight_str.replace("kg", "", ignoreCase = true) // Remove "kg" 
            }
            else -> throw InvalidResponseException("Unknown protocol", response)
        }

        when (weight_str.trim().firstOrNull()?.lowercaseChar()) {
            'i' -> throw UnstableWeightException("Unstable weight", response)
            'n' -> throw NegativeWeightException("Negative weight", response)
            's' -> throw OverloadException("Overload", response)
            'c' -> throw ZeroCaptureException("Zero capture", response)
            'e' -> throw CalibrationException("Calibration error", response)
        }
        return ParserUtils.parseWeight(weight_str, decimals)
    }
}