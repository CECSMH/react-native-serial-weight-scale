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

    private var _attached_emitter: ((Device) -> Unit)? = null;
    private var _detached_emitter:  ((Device) -> Unit)? = null;

    fun setAttachedEmitter(emitter: (Device) -> Unit){
        _attached_emitter = emitter;
    }
    fun setDetachedEmitter(emitter: (Device) -> Unit){
        _detached_emitter = emitter;
    }
 
    override fun onReceive(context: Context, intent: Intent) {
        val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)

        when (intent.action) {
            UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                if(device != null){
                    val _d: Device = Device(
                        name = device.deviceName,
                        vendorId = device.vendorId,
                        productId = device.productId,
                        port = device.deviceName,
                        hasPermission = false
                    )
                _attached_emitter?.invoke(_d)
               } 
            }
            UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                if(device != null){
                    val _d: Device = Device(
                        name = device.deviceName,
                        vendorId = device.vendorId,
                        productId = device.productId,
                        port = device.deviceName,
                        hasPermission = false
                    )
                _detached_emitter?.invoke(_d)
               } 
            }
        }
    }
}