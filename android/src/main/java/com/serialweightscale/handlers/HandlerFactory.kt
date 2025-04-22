package com.serialweightscale.handlers

object HandlerFactory {
    fun create(brand: String, model: String?): Handler = when (brand.lowercase()) {
        "toledo" -> ToledoHandler(model)
        "filizola" -> FilizolaHandler()
        "urano" -> UranoHandler(model)
        "micheletti" -> MichelettiHandler()
        else -> throw IllegalArgumentException("Unsupported brand: $brand")
    }
}