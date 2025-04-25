package com.serialweightscale.utils

import android.util.Log

object Logger {
    private val TAG = "ScaleModule"
    private var _js_emitter: ((String) -> Unit)? = null;

    fun setJsEmitter(emitter: (String) -> Unit) {
        _js_emitter = emitter;
    }

    @Synchronized
    fun log(message: String) {
        _js_emitter?.invoke(TAG + ": " + message)
        Log.d(TAG, message)
    }
}