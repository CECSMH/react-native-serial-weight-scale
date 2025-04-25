package com.serialweightscale.utils

import android.content.Context
import android.hardware.usb.UsbManager
import android.hardware.usb.UsbDevice
import com.serialweightscale.utils.Logger
import android.content.BroadcastReceiver

import android.content.Intent
import com.serialweightscale.exceptions.SerialConnectionException

class UsbReceiver : BroadcastReceiver() {
    private val ACTION_USB_PERMISSION = "com.serialweightscale.USB_PERMISSION"

    private var _attached_emitter: (Device) -> Unit;
    private var _detached_emitter: (Device) -> Unit;
    private var _permission_emitter: (Device) -> Unit;

    fun setAttachedEmitter(emitter: (Device) -> Unit){
        _attached_emitter = emitter;
    }
    fun setDetachedEmitter(emitter: (Device) -> Unit){
        _detached_emitter = emitter;
    }
    fun setPermissionEmitter(emitter: (Device) -> Unit){
        _permission_emitter = emitter;
    }

    override fun onReceive(context: Context, intent: Intent) {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)

        when (intent.action) {
            UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                val _d: Device = Device(hasPermission = false)
                _d.fromUsbDevice(device)
                _attached_emitter?.invoke(_d)
            }
            UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                val _d: Device = Device(hasPermission = SerialUtils.hasPermission(device))
                _d.fromUsbDevice(_d)
                _detached_emitter?.invoke(_d)
            }
            ACTION_USB_PERMISSION -> {
                val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                if (granted) {
                    Logger.log("permissão concedida ${device?.deviceName}")
                } else {
                    Logger.log("permissão negada ${device?.deviceName}")
                }
            }
        }
    }
}