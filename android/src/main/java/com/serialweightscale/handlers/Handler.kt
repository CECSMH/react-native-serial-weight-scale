package com.serialweightscale.handlers

import com.serialweightscale.utils.Config

interface Handler {
    val brand: String
    val model: String?
    fun connect(productId: Int, config: Config)
    fun readWeight(): Double
    fun monitorWeight(): Sequence<Double>
    fun disconnect()
}