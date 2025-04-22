package com.serialweightscale.handlers

import com.serialweightscale.exceptions.*
import com.serialweightscale.utils.*

class FilizolaHandler : BaseHandler("filizola", null) {
    override fun getCommand(): String = Constants.ENQ

    override fun parseResponse(response: String): Double {
        when {
            response.isEmpty() -> throw InvalidResponseException("Empty response", response)
            response.contains("I") -> throw UnstableWeightException("Unstable weight", response)
            response.contains("N") -> throw NegativeWeightException("Negative weight", response)
            response.contains("S") -> throw OverloadException("Overload", response)
        } 

        val stxIndex = response.indexOf(Constants.STX)
        val etxIndex = response.indexOf(Constants.ETX, stxIndex + 1).let { if (it == -1) response.indexOf(Constants.CR, stxIndex + 1) else it }
        if (stxIndex == -1 || etxIndex == -1) throw InvalidResponseException("Invalid format", response)
        val weightStr = response.substring(stxIndex + 1, etxIndex).trim()
        return ParserUtils.parseWeight(weightStr, 3)
    }
}