package com.serialweightscale.utils

import android.util.Log

object Logger {
    private val TAG = "ScaleModule"
    private var _js_emitter;

    fun setJsEmitter(emitter: (String) -> Unit)){
        _js_emitter = emitter;
    }

    fun log(message: String) {
        _js_emitter()
        Log.d(TAG, message)
    }
}