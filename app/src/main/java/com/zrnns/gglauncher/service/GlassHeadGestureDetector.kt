package com.zrnns.gglauncher.service

import android.content.Context
import androidx.lifecycle.LiveData
import java.util.*


class GlassHeadGestureDetector(val context: Context) {

    var sensorEventManager: SensorEventManager? = null

    val onLookup: LiveData<UUID?>?
        get() {
            return sensorEventManager?.onLookup
        }

    val onTakeOff: LiveData<UUID?>?
        get() {
            return sensorEventManager?.onTakeOff
        }

    fun isSubscribing(): Boolean {
        return sensorEventManager != null
    }

    fun startSubscribe() {
        endSubscribe()

        val sensorEventManager = SensorEventManager(context)

        this.sensorEventManager = sensorEventManager
        sensorEventManager.startSubscribe()
    }

    fun endSubscribe() {
        sensorEventManager?.endSubscribe()
        sensorEventManager = null
    }
}