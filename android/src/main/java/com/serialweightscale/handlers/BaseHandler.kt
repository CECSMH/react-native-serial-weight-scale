package com.serialweightscale.handlers

import com.serialweightscale.exceptions.*
import com.serialweightscale.utils.*

abstract class BaseHandler(override val brand: String, override val model: String?) : Handler {
    protected var serialPort: SerialPort? = null
    protected var timeout: Int = 500
    protected var retries: Int = 0
    val isConnected: Boolean get() = serialPort?.isOpen ?: false

    override fun connect(config: Config) {
        if (isConnected) return
        timeout = config.timeout ?: 500
        retries = config.retries ?: 0
        serialPort = SerialUtils.openPort(
            config.port,
            config.baudRate,
            config.dataBits,
            config.parity,
            config.stopBits
        )
        log("Connected: $brand/$model")
    }

    override fun disconnect() {
        serialPort?.let {
            SerialUtils.closePort(it)
            serialPort = null
            log("Disconnected: $brand/$model")
        }
    }

    protected fun sendCommand(command: String?) {
        if (!isConnected) throw SerialConnectionException("Not connected", null)
        command?.let {
            SerialUtils.send(serialPort!!, it)
            log("TX -> $it")
        }
    }

    protected fun readResponse(timeout: Int): String {
        if (!isConnected) throw SerialConnectionException("Not connected", null)
        val response = SerialUtils.read(serialPort!!, timeout)
        log("RX <- $response")
        if (response.isNullOrEmpty()) throw TimeoutException("No response received", null)
        return response
    }

    abstract fun parseResponse(response: String): Double

    override fun readWeight(): Double {
        repeat(retries + 1) { attempt ->
            try {
                sendCommand(getCommand())
                val response = readResponse(timeout)
                return parseResponse(response)
            } catch (e: UnstableWeightException) {
                if (attempt == retries) throw e
            }
        }
        throw UnstableWeightException("Failed after $retries retries")
    }

    override fun monitorWeight(): Sequence<Double> = sequence {
        while (isConnected) {
            try {
                val weight = readWeight()
                yield(weight)
            } catch (e: ScaleException) {
                throw e
            }
        }
    }

    abstract fun getCommand(): String?

    protected fun log(message: String) {
        Logger.log("$brand/$model: $message")
    }
}
