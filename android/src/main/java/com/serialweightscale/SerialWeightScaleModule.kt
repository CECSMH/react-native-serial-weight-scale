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
        val NAME = "SerialWeightScale"
    }
    private val handlers = ConcurrentHashMap<Int, Handler>()
    private val monitoringJobs = ConcurrentHashMap<Int, Job>()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        ContextHolder.setContext(reactApplicationContext)
        Logger.setJsEmitter(::emitOnLog)
    }

    override fun getName(): String = NAME

    override fun listDevices(promise: Promise) {
        try {
            val devices = SerialUtils.listDevices()
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
            promise.reject("serial_connection", e.message)
        }
    }

    override fun connect(productId: Double, configMap: ReadableMap, promise: Promise) {
        try {
            val _productId = productId.toInt() 
            val config = Config.fromMap(configMap)
          
            val handler = HandlerFactory.create(config.brand, config.model)
            handler.connect(_productId, config) 
            handlers[_productId] = handler
            promise.resolve(null)
        } catch (e: ScaleException) {
            promise.reject(e.getType(), e.message)
        } catch (e: Exception) {
            promise.reject("serial_connection", e.message)
        }
    }

    override fun readWeight(productId: Double, promise: Promise) {
        try {
            val _productId = productId.toInt() 
            val handler = handlers[_productId] ?: throw InvalidScaleIdException("Unknown scale ID: $_productId", null)
            val weight = handler.readWeight()
            promise.resolve(Arguments.createMap().apply { putDouble("weight", weight) })
        } catch (e: ScaleException) {
            promise.reject(e.getType(), e.message)
        } catch (e: Exception) {
            promise.reject("serial_connection", e.message)
        }
    }

    override fun startMonitoringWeight(productId: Double, promise: Promise) {
        try {
            val _productId = productId.toInt() 
            val handler = handlers[_productId] ?: throw InvalidScaleIdException("Unknown scale ID: $_productId", null)
            if (monitoringJobs.containsKey(_productId)) {
                promise.resolve(null)
                return
            }
            val job = coroutineScope.launch {
                try {
                    handler.monitorWeight().forEach { weight: Double ->
                        val resultMap = Arguments.createMap().apply { putDouble("weight", weight) }
                        emitWeightUpdate(_productId, resultMap)
                    }
                } catch (e: ScaleException) {
                    val resultMap = Arguments.createMap().apply { putMap("error", e.toMap()) }
                    emitWeightUpdate(_productId, resultMap)
                } catch (e: Exception) {
                    val resultMap = Arguments.createMap().apply {
                        putMap("error", SerialConnectionException("Serial error: ${e.message}", null).toMap())
                    }
                    emitWeightUpdate(_productId, resultMap)
                }
            }
            monitoringJobs[_productId] = job
            promise.resolve(null)
        } catch (e: ScaleException) {
            promise.reject(e.getType(), e.message)
        } catch (e: Exception) {
            promise.reject("serial_connection", e.message)
        }
    }

    override fun stopMonitoringWeight(productId: Double, promise: Promise) {
        try {
            val _productId = productId.toInt() 

            monitoringJobs.remove(_productId)?.cancel()
            promise.resolve(null)
        } catch (e: Exception) {
            promise.reject("invalid_response", "Unknown scale ID: $productId")
        }
    }

    override fun disconnect(productId: Double, promise: Promise) {
        try {
            val _productId = productId.toInt() 

            val handler = handlers.remove(_productId) ?: throw InvalidScaleIdException("Unknown scale ID: $_productId", null)
            monitoringJobs.remove(_productId)?.cancel()
            handler.disconnect()
            promise.resolve(null)
        } catch (e: ScaleException) {
            promise.reject(e.getType(), e.message)
        } catch (e: Exception) {
            promise.reject("serial_connection", e.message)
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
            promise.reject("serial_connection", e.message)
        }
    }

    private fun emitWeightUpdate(productId: Int, result: WritableMap) {
        val event = Arguments.createMap().apply {
            putInt("productId", productId)
            putMap("result", result)
        }
        emitOnWeightUpdate(event)
    }
}