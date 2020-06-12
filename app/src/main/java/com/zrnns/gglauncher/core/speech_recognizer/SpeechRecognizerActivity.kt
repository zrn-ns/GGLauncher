package com.zrnns.gglauncher.core.speech_recognizer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.zrnns.gglauncher.R
import com.zrnns.gglauncher.core.GlassGestureDetector
import kotlinx.android.synthetic.main.activity_speech_recognizer.*

class SpeechRecognizerActivity : AppCompatActivity(), GlassGestureDetector.OnGestureListener {

    companion object {
        const val EXTRA_RESULT_TEXT = "EXTRA_RESULT_TEXT"
    }

    private val glassGestureDetector: GlassGestureDetector by lazy { GlassGestureDetector(this, this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speech_recognizer)
    }

    override fun onResume() {
        super.onResume()

        textView.text = "Listening..."

        SpeechRecognizer(applicationContext).start(object: SpeechRecognizer.SpeechRecognizerCallback() {
            override fun recognitionTimeout() {
                cancelAndFinishRecognition()
            }

            override fun recognitionInProgress(text: String) {
                this@SpeechRecognizerActivity.runOnUiThread {
                    textView.text = text
                }
            }

            override fun recognitionCompleted(text: String) {
                val intent = Intent()
                intent.putExtra(EXTRA_RESULT_TEXT, text)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        })
    }

    override fun onGesture(gesture: GlassGestureDetector.Gesture): Boolean {
        return when (gesture) {
            GlassGestureDetector.Gesture.TAP -> {
                cancelAndFinishRecognition()
                return true
            }
            GlassGestureDetector.Gesture.SWIPE_DOWN -> {
                cancelAndFinishRecognition()
                return true
            }
            else -> false
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        return if (glassGestureDetector.onTouchEvent(event)) {
            true
        } else super.dispatchTouchEvent(event)
    }

    private fun cancelAndFinishRecognition() {
        //TODO: 音声認識が途中なら中止する処理

        val intent = Intent()
        setResult(Activity.RESULT_CANCELED, intent)
        finish()
    }

}