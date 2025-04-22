package com.serialweightscale.handlers

import com.serialweightscale.exceptions.*
import com.serialweightscale.utils.*

class UranoHandler(model: String?) : BaseHandler("urano", model ?: "urano") {
    override fun getCommand(): String = if (model == "uranoudc") Constants.EOT else Constants.ENQ

    override fun parseResponse(response: String): Double {
        when {
            response.isEmpty() || response.contains("TARA:") -> throw InvalidResponseException("Invalid response", response)
            response.contains("I") -> throw UnstableWeightException("Unstable weight", response)
            response.contains("N") -> throw NegativeWeightException("Negative weight", response)
        }
        val weight_str = when (model) {
            "uranopop" -> response.split(if (response.contains("PESO L:")) "PESO L:" else "kg")[1].split("kg")[0].trim()
            else -> response.split("PESO:")[1].split("kg")[0].trim()
        }
        return ParserUtils.parseWeight(weight_str, 3)
    }
}