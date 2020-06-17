package com.zrnns.gglauncher.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.MutableLiveData
import java.util.*
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.max
import kotlin.math.min

class SensorEventManager(context: Context) : SensorEventListener {

    companion object {
        const val RAD2DEG = 180 / Math.PI
        const val STANDARD_GRAVITY_ACCELERATION = 10f
        const val DEGREES_FOR_LOOKUP_EVENT_DEPRESSION_THRESHOLD = -5
        const val DEGREES_FOR_LOOKUP_EVENT_DEPRESSION_VARIATION = -10
        const val ABS_DEGREES_TOOK_OFF_JUDGEMENT_THRESHOLD = 1
        const val SECS_TOOK_OFF_JUDGEMENT_THRESHOLD = 30
    }

    val onLookup: MutableLiveData<UUID?> = MutableLiveData(null)
    var isTakeOff: Boolean = false
        set(newValue: Boolean) {
            val valueChanged = field != newValue

            field = newValue

            if (valueChanged) {
                endSubscribe()
                startSubscribe()
            }
        }

    fun startSubscribe() {
        // センサータイプを指定してセンサーを取得
        val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // SensorManagerにリスナーを登録
        val samplingFrequency = if (isTakeOff) SensorManager.SENSOR_DELAY_NORMAL else SensorManager.SENSOR_DELAY_UI

        sensorManager.registerListener(this, accelerometerSensor, samplingFrequency)
    }

    fun endSubscribe() {
        sensorManager.unregisterListener(this)
    }

    private var sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private var pitchLog: MutableList<Float> = mutableListOf()

    private var lastMotionDetectedDate = Date()

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }

    override fun onSensorChanged(event: SensorEvent?) {

        event?.let {
            // 💩Magnetic Field sensor cause OS hangup(maybe OS bug.)
            // Use only Accelerometer sensor to check pitch now.
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                // sinを-1.0から1.0の間に収める
                val normalizedSin = min(max(event.values[1].toDouble() / STANDARD_GRAVITY_ACCELERATION, -1.0), 1.0)

                val pitch = -1 * asin(normalizedSin) * RAD2DEG

                pitchLog.add(pitch.toFloat())
                if (pitchLog.size > 20) {
                    pitchLog.removeAt(0)
                }

                val depressionVariation = pitchLog.last() - pitchLog.first()
                if (pitchLog.last() < DEGREES_FOR_LOOKUP_EVENT_DEPRESSION_THRESHOLD && depressionVariation < DEGREES_FOR_LOOKUP_EVENT_DEPRESSION_VARIATION) {
                    onLookup.value = UUID.randomUUID()
                }

                Log.i("INFO", String.format("pitch: %.1f, pitchFirst: %.1f, pitchLast: %.1f, pitchDifference: %.1f", pitch, pitchLog.first(), pitchLog.last(), pitchLog.last() - pitchLog.first()))

                if (abs(depressionVariation) > ABS_DEGREES_TOOK_OFF_JUDGEMENT_THRESHOLD) {
                    lastMotionDetectedDate = Date()
                }

                isTakeOff = Date().time > lastMotionDetectedDate.time + SECS_TOOK_OFF_JUDGEMENT_THRESHOLD * 1000
            }
        }
    }
}