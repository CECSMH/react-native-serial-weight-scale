package com.serialweightscale.handlers

import com.serialweightscale.exceptions.*
import com.serialweightscale.utils.*

class MichelettiHandler : BaseHandler("micheletti", null) {
    override fun getCommand(): String = Constants.ENQ

    override fun parseResponse(response: String): Double {
        if (response.length < 10) throw InvalidResponseException("Invalid response length: ${response.length}", response)
        val weightStr = response.substring(3, 10).trim()
        return ParserUtils.parseWeight(weightStr, 3)
    }
}