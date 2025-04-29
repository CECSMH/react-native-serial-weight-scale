package com.serialweightscale.handlers

import com.serialweightscale.utils.Config
import com.serialweightscale.utils.Device

interface Handler {
    val brand: String
    val model: String?
    fun getDevice(): Device
    fun connect(productId: Int, config: Config)
    suspend fun readWeight(): Double
    suspend fun readWeight(timeout: Int): Double
    fun monitorWeight(): Sequence<Double>
    fun disconnect()
}