package com.zrnns.gglauncher.service

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.MutableLiveData
import java.util.*


class GlassHeadGestureDetector(val context: Context): SensorEventListener {

    companion object {
        const val RAD2DEG = 180 / Math.PI
    }

    val lookupGlassEvent: MutableLiveData<UUID?> = MutableLiveData(null)
    private var sensorManager: SensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager

    private var rotationMatrix = FloatArray(9)
    private var gravity = FloatArray(3)
    private var geomagnetic = FloatArray(3)
    private var attitude = FloatArray(3)

    private var pitchLog: MutableList<Float> = mutableListOf()

    fun startSubscribe() {
        // センサータイプを指定してセンサーを取得
        val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        // SensorManagerにリスナーを登録
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, magneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun endSubscribe() {
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }

    override fun onSensorChanged(event: SensorEvent?) {

        event?.let {

            when (event.sensor.type) {
                Sensor.TYPE_MAGNETIC_FIELD -> geomagnetic = event.values.clone()
                Sensor.TYPE_ACCELEROMETER -> gravity = event.values.clone()
            }

            SensorManager.getRotationMatrix(
                rotationMatrix, null,
                gravity, geomagnetic);

            SensorManager.getOrientation(
                rotationMatrix,
                attitude);
//
//            val azimuth = attitude[0] * RAD2DEG
            val pitch = attitude[1] * RAD2DEG
//            val roll = attitude[2] * RAD2DEG
//            val strTmp = String.format(
//                Locale.US, "TYPE_ACCELEROMETER azimuth: %f, pitch: %f, roll: %f", azimuth, pitch, roll
//            )
//            Log.i("INFO", strTmp)

            pitchLog.add(pitch.toFloat())
            if (pitchLog.size > 20) {
                pitchLog.removeAt(0)
            }

//            Log.i("INFO", String.format("pitch: %.1f, pitchFirst: %.1f, pitchLast: %.1f, pitchDifference: %.1f", pitch, pitchLog.first(), pitchLog.last(), pitchLog.last() - pitchLog.first()))

            // あとで統計で求める方法に変える
            // 変数を外出しする
            if (pitchLog.last() - pitchLog.first() < -10 && pitchLog.last() < -5) {
                lookupGlassEvent.value = UUID.randomUUID()
            }
        }
    }
}