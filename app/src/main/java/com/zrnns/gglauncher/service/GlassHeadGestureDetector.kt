package com.zrnns.gglauncher.service

import android.content.Context
import androidx.lifecycle.LiveData
import java.util.*


class GlassHeadGestureDetector(val context: Context) {

    var sensorEventManager: SensorEventManager = SensorEventManager(context)

    val onLookup: LiveData<UUID?>
        get() {
            return sensorEventManager.onLookup
        }

    val onTakeOff: LiveData<UUID?>
        get() {
            return sensorEventManager.onTakeOff
        }

    var isSubscribing: Boolean = false

    fun startSubscribe() {
        isSubscribing = true
        sensorEventManager.startSubscribe()
    }

    fun endSubscribe() {
        sensorEventManager.endSubscribe()
        isSubscribing = false
    }
}