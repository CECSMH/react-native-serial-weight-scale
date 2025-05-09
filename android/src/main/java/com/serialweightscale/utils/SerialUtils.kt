
package com.serialweightscale.utils

import android.content.Context
import android.hardware.usb.UsbManager
import android.app.PendingIntent
import android.content.Intent
import android.hardware.usb.UsbDevice
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.serialweightscale.exceptions.SerialConnectionException
import java.nio.charset.StandardCharsets
import com.serialweightscale.utils.Logger
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.*
import android.content.BroadcastReceiver
import android.content.IntentFilter
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking

data class SerialPort(val driver: UsbSerialDriver, val port: UsbSerialPort, val device: UsbDevice) {
    val isOpen: Boolean get() = port.isOpen
}

data class Device(
    var name: String, 
    var vendorId: Int, 
    var productId: Int, 
    var port: String, 
    var hasPermission: Boolean
){
    fun toMap(): WritableMap {
        return Arguments.createMap().apply {
            putString("name", name)
            putInt("vendorId", vendorId)
            putInt("productId", productId)
            putString("port", port)
            putBoolean("hasPermission", hasPermission)
        }
    }
}

data class Config(
    val baudRate: Int,
    val dataBits: Int,
    val parity: String,
    val stopBits: Int,
    val timeout: Int?,
    val retries: Int?,
    val brand: String,
    val model: String?
) {
    companion object {
        fun fromMap(map: com.facebook.react.bridge.ReadableMap): Config {
            return Config(
                baudRate = map.getInt("baudRate"),
                dataBits = map.getInt("dataBits"),
                parity = map.getString("parity") ?: "none",
                stopBits = map.getInt("stopBits"),
                timeout = if (map.hasKey("timeout")) map.getInt("timeout") else null,
                retries = if (map.hasKey("retries")) map.getInt("retries") else null,
                brand = map.getString("brand") ?: throw IllegalArgumentException("Brand required"),
                model = if (map.hasKey("model")) map.getString("model") else null
            )
        }
    }
}

object SerialUtils {
    private const val ACTION_USB_PERMISSION = "com.serialweightscale.USB_PERMISSION"

    private fun getUsbManager(): UsbManager{
        val context = ContextHolder.getContext()?: throw SerialConnectionException("Context unavailable")
        return context.getSystemService(Context.USB_SERVICE) as UsbManager
    }

    fun listDevices(): List<Device> {
        val usbManager = getUsbManager()
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
         
        return availableDrivers.mapIndexed { _, driver ->
            Device(
                name = driver.device.deviceName,
                vendorId = driver.device.vendorId,
                productId = driver.device.productId,
                port = driver.device.deviceName,
                hasPermission = usbManager.hasPermission(driver.device)
            )
        }
    }

    fun hasPermission(device: UsbDevice): Boolean {
        val usbManager = getUsbManager()
        return usbManager.hasPermission(device)
    }

    suspend fun requestPermission(device: UsbDevice): Boolean {
        val context = ContextHolder.getContext() ?: throw SerialConnectionException("Context unavailable")
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val permissionGranted = CompletableDeferred<Boolean>()

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == ACTION_USB_PERMISSION) {
                    val granted = usbManager.hasPermission(device)
                    permissionGranted.complete(granted)
                    context.unregisterReceiver(this)
                }
            }
        }
        context.registerReceiver(receiver,  IntentFilter(ACTION_USB_PERMISSION), ContextCompat.RECEIVER_EXPORTED)

        val permissionIntent = PendingIntent.getBroadcast(
            context, 0, Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE
        )
        usbManager.requestPermission(device, permissionIntent)
        return permissionGranted.await()
    }


    fun openPort(productId: Int, baudRate: Int, dataBits: Int, parity: String, stopBits: Int): SerialPort {
        val usbManager = getUsbManager()

        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)

        val driver = availableDrivers.find { it.device.productId == productId } ?: throw SerialConnectionException("Device not found: ID=$productId")

        val device = driver.device

        if (!usbManager.hasPermission(device)) {
            runBlocking {
                val granted = SerialUtils.requestPermission(device)
                if (!granted) throw SerialConnectionException("USB Permission Denied for ID=$productId")
            }
        }

        val serialPort = driver.ports[0]
        serialPort.open(usbManager.openDevice(device))

        serialPort.setParameters(
            baudRate,
            dataBits,
            when (stopBits) {
                1 -> UsbSerialPort.STOPBITS_1
                2 -> UsbSerialPort.STOPBITS_2
                3 -> UsbSerialPort.STOPBITS_1_5 
                else -> throw IllegalArgumentException("Invalid stopBits: $stopBits")
            },
            when (parity.lowercase()) {
                "none" -> UsbSerialPort.PARITY_NONE
                "even" -> UsbSerialPort.PARITY_EVEN
                "odd" -> UsbSerialPort.PARITY_ODD
                else -> throw IllegalArgumentException("Invalid parity: $parity")
            }
        )

        return SerialPort(driver, serialPort, device)
    }

    fun send(port: SerialPort, command: String) {
        if (!port.isOpen) throw SerialConnectionException("Port not open")
        port.port.write(command.toByteArray(StandardCharsets.US_ASCII), 1000)
    }

    fun read(port: SerialPort, timeout: Int): String {
        if (!port.isOpen) throw SerialConnectionException("Port not open")
        val buffer = ByteArray(3072)
        val bytesRead = port.port.read(buffer, timeout)

        return if (bytesRead > 0) {
            String(buffer, 0, bytesRead, StandardCharsets.US_ASCII)
        } else {
            ""
        }
    }

    fun closePort(port: SerialPort) {
        if (port.isOpen) { port.port.close() }
    }
}

object ContextHolder {
    private var context: Context? = null

    fun setContext(ctx: Context) { context = ctx }
    fun getContext(): Context? = context
}
