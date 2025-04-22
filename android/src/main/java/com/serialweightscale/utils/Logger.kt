package com.serialweightscale.utils

import android.util.Log

object Logger {
    private const val TAG = "ScaleModule"

    fun log(message: String) {
        Log.d(TAG, message)
    }
}