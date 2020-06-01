package com.zrnns.gglauncher.core

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import kotlin.math.abs
import kotlin.math.tan

// https://developers.google.com/glass-enterprise/guides/inputs-sensors
class GlassGestureDetector(context: Context, private val onGestureListener: OnGestureListener) :
    GestureDetector.OnGestureListener {

    private val gestureDetector = GestureDetector(context, this)

    enum class Gesture {
        TAP,
        SWIPE_FORWARD,
        SWIPE_BACKWARD,
        SWIPE_UP,
        SWIPE_DOWN
    }

    interface OnGestureListener {
        fun onGesture(gesture: Gesture): Boolean
    }

    fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(motionEvent)
    }

    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent) {}

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return onGestureListener.onGesture(Gesture.TAP)
    }

    override fun onScroll(
        e1: MotionEvent,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent) {}

    /**
     * Swipe detection depends on the:
     * - movement tan value,
     * - movement distance,
     * - movement velocity.
     *
     * To prevent unintentional SWIPE_DOWN and SWIPE_UP gestures, they are detected if movement
     * angle is only between 60 and 120 degrees.
     * Any other detected swipes, will be considered as SWIPE_FORWARD and SWIPE_BACKWARD, depends
     * on deltaX value sign.
     *
     * ______________________________________________________________
     * |                     \        UP         /                    |
     * |                       \               /                      |
     * |                         60         120                       |
     * |                           \       /                          |
     * |                             \   /                            |
     * |  BACKWARD  <-------  0  ------------  180  ------>  FORWARD  |
     * |                             /   \                            |
     * |                           /       \                          |
     * |                         60         120                       |
     * |                       /               \                      |
     * |                     /       DOWN        \                    |
     * --------------------------------------------------------------
     */
    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        val deltaX = e2.x - e1.x
        val deltaY = e2.y - e1.y
        val tan =
            if (deltaX != 0f) abs(deltaY / deltaX).toDouble() else java.lang.Double.MAX_VALUE

        return if (tan > TAN_60_DEGREES) {
            if (abs(deltaY) < SWIPE_DISTANCE_THRESHOLD_PX || Math.abs(velocityY) < SWIPE_VELOCITY_THRESHOLD_PX) {
                false
            } else if (deltaY < 0) {
                onGestureListener.onGesture(Gesture.SWIPE_UP)
            } else {
                onGestureListener.onGesture(Gesture.SWIPE_DOWN)
            }
        } else {
            if (Math.abs(deltaX) < SWIPE_DISTANCE_THRESHOLD_PX || Math.abs(velocityX) < SWIPE_VELOCITY_THRESHOLD_PX) {
                false
            } else if (deltaX < 0) {
                onGestureListener.onGesture(Gesture.SWIPE_FORWARD)
            } else {
                onGestureListener.onGesture(Gesture.SWIPE_BACKWARD)
            }
        }
    }

    companion object {

        private const val SWIPE_DISTANCE_THRESHOLD_PX = 100
        private const val SWIPE_VELOCITY_THRESHOLD_PX = 100
        private val TAN_60_DEGREES = tan(Math.toRadians(60.0))
    }
}