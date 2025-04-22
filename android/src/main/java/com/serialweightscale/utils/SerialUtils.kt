
package com.serialweightscale.utils

import android.content.Context
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.serialweightscale.exceptions.SerialConnectionException
import java.nio.charset.StandardCharsets

data class SerialPort(val driver: UsbSerialDriver, val port: UsbSerialPort) {
    val isOpen: Boolean get() = port.isOpen
}

data class Device(val name: String, val vendorId: Int, val productId: Int, val port: String, val hasPermission: Boolean)

data class Config(
    val port: String,
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
                port = map.getString("port") ?: throw IllegalArgumentException("Port required"),
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
    fun listDevices(context: Context): List<Device> {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        return availableDrivers.mapIndexed { index, driver ->
            Device(
                name = driver.device.deviceName,
                vendorId = driver.device.vendorId,
                productId = driver.device.productId,
                port = "/dev/ttyUSB$index",
                hasPermission = usbManager.hasPermission(driver.device)
            )
        }
    }

    fun openPort(port: String, baudRate: Int, dataBits: Int, parity: String, stopBits: Int): SerialPort {
        val context = ContextHolder.getContext()
            ?: throw SerialConnectionException("Context unavailable")
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        val driver = availableDrivers.find { it.device.deviceName.contains(port) }
            ?: throw SerialConnectionException("Device not found: $port")
        if (!usbManager.hasPermission(driver.device)) {
            throw SerialConnectionException("No permission for device: $port")
        }
        val serialPort = driver.ports[0]
        serialPort.open(usbManager.openDevice(driver.device))
        serialPort.setParameters(
            baudRate,
            dataBits,
            when (stopBits) {
                1 -> UsbSerialPort.STOPBITS_1
                2 -> UsbSerialPort.STOPBITS_2
                else -> throw IllegalArgumentException("Invalid stopBits: $stopBits")
            },
            when (parity.lowercase()) {
                "none" -> UsbSerialPort.PARITY_NONE
                "even" -> UsbSerialPort.PARITY_EVEN
                "odd" -> UsbSerialPort.PARITY_ODD
                else -> throw IllegalArgumentException("Invalid parity: $parity")
            }
        )
        return SerialPort(driver, serialPort)
    }

    fun send(port: SerialPort, command: String) {
        if (!port.isOpen) throw SerialConnectionException("Port not open")
        port.port.write(command.toByteArray(StandardCharsets.US_ASCII), 1000)
    }

    fun read(port: SerialPort, timeout: Int): String {
        if (!port.isOpen) throw SerialConnectionException("Port not open")
        val buffer = ByteArray(1024)
        val bytesRead = port.port.read(buffer, timeout)
        return if (bytesRead > 0) {
            String(buffer, 0, bytesRead, StandardCharsets.US_ASCII)
        } else {
            ""
        }
    }

    fun closePort(port: SerialPort) {
        if (port.isOpen) {
            port.port.close()
        }
    }
}

object ContextHolder {
    private var context: Context? = null

    fun setContext(ctx: Context) {
        context = ctx
    }

    fun getContext(): Context? = context
}
