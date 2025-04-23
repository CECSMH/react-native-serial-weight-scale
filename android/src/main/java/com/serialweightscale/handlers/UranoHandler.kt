package com.serialweightscale.handlers

import com.serialweightscale.exceptions.*
import com.serialweightscale.utils.*

class UranoHandler(model: String?) : BaseHandler("urano", model ?: "urano") {
    private var _model = model
    override fun getCommand(): String = if (_model == "uranoudc") Constants.EOT else Constants.ENQ

    override fun connect(productId: Int, config: Config) {
        if (isConnected) return
        super.connect(productId, config)

        SerialUtils.send(serialPort!!, Constants.EOT)
        val eotResponse = SerialUtils.read(serialPort!!, 500)
        if (!eotResponse.isNullOrEmpty()) {
            _model = "uranoudc"
            return
        }

        SerialUtils.send(serialPort!!, Constants.ENQ)
        val enqResponse = SerialUtils.read(serialPort!!, 500)

        _model = when {
            enqResponse.contains("PESO L:") -> "uranopop"
            enqResponse.contains("PESO:") -> "urano"
            else -> model ?: "urano"
        }
    }

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