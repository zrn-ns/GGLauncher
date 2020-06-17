package com.zrnns.gglauncher.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Observer
import com.zrnns.gglauncher.R


class ForegroundService : Service(), LifecycleOwner {

    private val headGestureDetector by lazy { GlassHeadGestureDetector(applicationContext) }
    private val screenStatusReceiver by lazy { ScreenStatusReceiver(applicationContext) }
    private var keepServiceAliveWakeLock: PowerManager.WakeLock? = null
    private var turnScreenOnWakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        lifecycleRegistry.currentState = Lifecycle.State.STARTED

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val name = "GGLauncher service is running..."
        val id = "GGLauncher_foreground_service"
        val notifyDescription = "To wake-up with motion sensor."

        if (manager.getNotificationChannel(id) == null) {
            val mChannel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH)
            mChannel.apply {
                description = notifyDescription
            }
            manager.createNotificationChannel(mChannel)
        }

        val notification = NotificationCompat.Builder(this,id).setContentTitle(name).setContentText(notifyDescription).apply {
            setSmallIcon(R.drawable.ic_launcher_foreground)
        }.build()

        screenStatusReceiver.startSubscribe()
        screenStatusReceiver.onScreenOn.observe(this, Observer {
            if (!headGestureDetector.isSubscribing()) {
                headGestureDetector.startSubscribe()

                val powerManager =
                    getSystemService(Context.POWER_SERVICE) as PowerManager
                keepServiceAliveWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, packageName)
                keepServiceAliveWakeLock?.acquire()
            }
        })

        headGestureDetector.startSubscribe()
        headGestureDetector.onLookup?.observe(this, Observer {
            it?.let {
                wakeFromSleep()
            }
        })
        headGestureDetector.onTakeOff?.observe(this, Observer {
            it?.let {
                headGestureDetector.endSubscribe()
                keepServiceAliveWakeLock?.release()
            }
        })

        stopForeground(Service.STOP_FOREGROUND_DETACH)

        startForeground(1, notification)

        val powerManager =
            getSystemService(Context.POWER_SERVICE) as PowerManager
        keepServiceAliveWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, packageName)
        keepServiceAliveWakeLock?.acquire()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        screenStatusReceiver.endSubscribe()
        headGestureDetector.endSubscribe()
        keepServiceAliveWakeLock?.release()
    }

    private fun wakeFromSleep() {
        if (turnScreenOnWakeLock != null && turnScreenOnWakeLock?.isHeld == true) {
            // prevent continuous call
            return
        }
        val wakelock = getPowerManager()
            .newWakeLock(
                PowerManager.FULL_WAKE_LOCK
                        or PowerManager.ACQUIRE_CAUSES_WAKEUP
                        or PowerManager.ON_AFTER_RELEASE, packageName
            )
        wakelock.acquire(5000)
        turnScreenOnWakeLock = wakelock
    }

    private fun getPowerManager(): PowerManager {
        return getSystemService(Context.POWER_SERVICE) as PowerManager
    }

    // LifecycleOwner
    override fun getLifecycle(): Lifecycle = lifecycleRegistry
    private val lifecycleRegistry = LifecycleRegistry(this)
}