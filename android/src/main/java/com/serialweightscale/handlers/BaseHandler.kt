package com.serialweightscale.handlers

import com.serialweightscale.exceptions.*
import com.serialweightscale.utils.*
import kotlinx.coroutines.*

abstract class BaseHandler(override val brand: String, override val model: String?) : Handler {
    protected var serialPort: SerialPort? = null
    protected var timeout: Int = 500
    protected var retries: Int = 0
    val isConnected: Boolean get() = serialPort?.isOpen ?: false

    override fun getDevice(): Device = Device(
        name = serialPort!!.device.deviceName,
        vendorId = serialPort!!.device.vendorId,
        productId = serialPort!!.device.productId,
        port = serialPort!!.device.deviceName,
        hasPermission = true
    )

    override fun connect(productId: Int, config: Config) {
        if (isConnected) return
        timeout = config.timeout ?: 500
        retries = config.retries ?: 0
        serialPort = SerialUtils.openPort(
            productId,
            config.baudRate,
            config.dataBits,
            config.parity,
            config.stopBits
        )
    }

    override fun disconnect() {
        serialPort?.let {
            SerialUtils.closePort(it)
            serialPort = null
        }
    }

    protected fun sendCommand(command: String?) {
        if (!isConnected) throw SerialConnectionException("Not connected", null)
        command?.let {
            SerialUtils.send(serialPort!!, it)
        }
    }

    protected fun readResponse(timeout: Int): String {
        if (!isConnected) throw SerialConnectionException("Not connected", null)
        val response = SerialUtils.read(serialPort!!, timeout)
     
        if (response.isNullOrEmpty()) throw TimeoutException("No response received", null)
        return response
    }

    abstract fun parseResponse(response: String): Double

    override suspend fun readWeight(timeout: Int): Double {
        repeat(retries + 1) { attempt ->
            try {
                sendCommand(getCommand())
                delay(200)

                //alguns modelos guardam internamente a ultima leitura, 
                //uma tentatira a mais na primeira volta resolve esse conflito.
                if((last_response == response || last_response.isEmpty()) && attempt == 0) return@repeat

                val response = readResponse(timeout)
                return parseResponse(response)
            } catch (e: ScaleException) {
                if (attempt == retries) throw e;
            } catch (e: Exception){ 
                if (attempt == retries) throw e;
            }
        }
        throw UnstableWeightException("Failed after $retries retries")
    }

    override suspend fun readWeight(): Double {
       return readWeight(timeout)
    }

    override fun monitorWeight(): Sequence<Double> = sequence {
        while (isConnected) {
            try {
                val weight = runBlocking{readWeight(50)}
                yield(weight)
            } catch (e: ScaleException) {
                throw e
            }
        }
    }

    abstract fun getCommand(): String?
}
