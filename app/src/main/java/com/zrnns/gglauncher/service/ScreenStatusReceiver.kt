package com.zrnns.gglauncher.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.MutableLiveData
import java.util.*

class ScreenStatusReceiver(val context: Context) {

    val onScreenOn: MutableLiveData<UUID> = MutableLiveData<UUID>()
    val onScreenOff: MutableLiveData<UUID> = MutableLiveData<UUID>()

    fun startSubscribe() {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_SCREEN_ON)
        context.registerReceiver(screenStatusReceiver, filter)
    }

    fun endSubscribe() {
        context.unregisterReceiver(screenStatusReceiver)
    }

    private var mIsScreenOn = true
        set(newValue) {
            val valueChanged = field != newValue

            field = newValue

            if (valueChanged) {
                if (newValue) {
                    onScreenOn.value = UUID.randomUUID()
                } else {
                    onScreenOff.value = UUID.randomUUID()
                }
            }
        }
    private val screenStatusReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            // Receive screen off
            if (intent.action == Intent.ACTION_SCREEN_OFF) {
                mIsScreenOn = false
            }
            if (intent.action == Intent.ACTION_SCREEN_ON) {
                mIsScreenOn = true
            }
        }
    }
}