package com.serialweightscale

import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.facebook.react.turbomodule.core.interfaces.TurboModule
import com.serialweightscale.exceptions.*
import com.serialweightscale.handlers.*
import com.serialweightscale.utils.*
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

class SerialWeightScaleModule(reactContext: ReactApplicationContext) :NativeSerialWeightScaleSpec(reactContext), TurboModule {
    companion object {
        const val NAME = "SerialWeightScale"
    }
    private val handlers = ConcurrentHashMap<String, Handler>()
    private val monitoringJobs = ConcurrentHashMap<String, Job>()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun getName(): String = NAME

    override fun listDevices(promise: Promise) {
        try {
            val devices = SerialUtils.listDevices(reactApplicationContext)
            val deviceArray = Arguments.createArray()
            devices.forEach { device ->
                deviceArray.pushMap(Arguments.createMap().apply {
                    putString("name", device.name)
                    putInt("vendorId", device.vendorId)
                    putInt("productId", device.productId)
                    putString("port", device.port)
                    putBoolean("hasPermission", device.hasPermission)
                })
            }
            promise.resolve(deviceArray)
        } catch (e: Exception) {
            promise.reject("SERIAL_ERROR", SerialConnectionException("Failed to list devices: ${e.message}", null).toMap())
        }
    }

    override fun connect(scaleId: String, configMap: ReadableMap, promise: Promise) {
        try {
            val config = Config.fromMap(configMap)
            val handler = HandlerFactory.create(config.brand, config.model)
            handler.connect(config)
            handlers[scaleId] = handler
            promise.resolve(null)
        } catch (e: ScaleException) {
            promise.reject("SCALE_ERROR", e.toMap())
        } catch (e: Exception) {
            promise.reject("SERIAL_ERROR", SerialConnectionException("Serial error: ${e.message}", null).toMap())
        }
    }

    override fun readWeight(scaleId: String, promise: Promise) {
        try {
            val handler = handlers[scaleId] ?: throw InvalidScaleIdException("Unknown scale ID: $scaleId", null)
            val weight = handler.readWeight()
            promise.resolve(Arguments.createMap().apply { putDouble("weight", weight) })
        } catch (e: ScaleException) {
            promise.reject("SCALE_ERROR", e.toMap())
        } catch (e: Exception) {
            promise.reject("SERIAL_ERROR", SerialConnectionException("Serial error: ${e.message}", null).toMap())
        }
    }

    override fun startMonitoringWeight(scaleId: String, promise: Promise) {
        try {
            val handler = handlers[scaleId] ?: throw InvalidScaleIdException("Unknown scale ID: $scaleId", null)
            if (monitoringJobs.containsKey(scaleId)) {
                promise.resolve(null)
                return
            }
            val job = coroutineScope.launch {
                try {
                    handler.monitorWeight().forEach { weight: Double ->
                        val resultMap = Arguments.createMap().apply { putDouble("weight", weight) }
                        emitWeightUpdate(scaleId, resultMap)
                    }
                } catch (e: ScaleException) {
                    val resultMap = Arguments.createMap().apply { putMap("error", e.toMap()) }
                    emitWeightUpdate(scaleId, resultMap)
                } catch (e: Exception) {
                    val resultMap = Arguments.createMap().apply {
                        putMap("error", SerialConnectionException("Serial error: ${e.message}", null).toMap())
                    }
                    emitWeightUpdate(scaleId, resultMap)
                }
            }
            monitoringJobs[scaleId] = job
            promise.resolve(null)
        } catch (e: ScaleException) {
            promise.reject("SCALE_ERROR", e.toMap())
        } catch (e: Exception) {
            promise.reject("SERIAL_ERROR", SerialConnectionException("Serial error: ${e.message}", null).toMap())
        }
    }

    override fun stopMonitoringWeight(scaleId: String, promise: Promise) {
        try {
            monitoringJobs.remove(scaleId)?.cancel()
            promise.resolve(null)
        } catch (e: Exception) {
            promise.reject("SCALE_ERROR", InvalidScaleIdException("Unknown scale ID: $scaleId", null).toMap())
        }
    }

    override fun disconnect(scaleId: String, promise: Promise) {
        try {
            val handler = handlers.remove(scaleId) ?: throw InvalidScaleIdException("Unknown scale ID: $scaleId", null)
            monitoringJobs.remove(scaleId)?.cancel()
            handler.disconnect()
            promise.resolve(null)
        } catch (e: ScaleException) {
            promise.reject("SCALE_ERROR", e.toMap())
        } catch (e: Exception) {
            promise.reject("SERIAL_ERROR", SerialConnectionException("Serial error: ${e.message}", null).toMap())
        }
    }

    override fun disconnectAll(promise: Promise) {
        try {
            monitoringJobs.forEach { entry -> entry.value.cancel() }
            monitoringJobs.clear()
            handlers.forEach { entry -> entry.value.disconnect() }
            handlers.clear()
            promise.resolve(null)
        } catch (e: Exception) {
            promise.reject("SERIAL_ERROR", SerialConnectionException("Serial error: ${e.message}", null).toMap())
        }
    }

    private fun emitWeightUpdate(scaleId: String, result: WritableMap) {
        val event = Arguments.createMap().apply {
            putString("scaleId", scaleId)
            putMap("result", result)
        }
        reactApplicationContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit("WeightUpdate", event)
    }
}