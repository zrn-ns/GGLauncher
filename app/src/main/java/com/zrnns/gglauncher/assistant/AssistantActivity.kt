package com.zrnns.gglauncher.assistant

import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.view.MotionEvent
import android.view.View
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.zrnns.gglauncher.R
import com.zrnns.gglauncher.core.GlassGestureDetector
import java.util.*

class AssistantActivity : AppCompatActivity(), GlassGestureDetector.OnGestureListener {

    private lateinit var viewModel: AssistantActivityViewModel
    private val glassGestureDetector: GlassGestureDetector by lazy { GlassGestureDetector(this, this) }

    private lateinit var mWebView: WebView
    private lateinit var inputMessageView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupViews()
        setupObservers()

        viewModel.onCreateAction()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onDestroyAction()
    }

    override fun onGesture(gesture: GlassGestureDetector.Gesture): Boolean {
        when (gesture) {
            GlassGestureDetector.Gesture.TAP -> {
                viewModel.viewTappedAction()
            }
            GlassGestureDetector.Gesture.SWIPE_UP -> {
                viewModel.viewSwipedUpAction()
            }
        }
        return true
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        return if (glassGestureDetector.onTouchEvent(event)) {
            true
        } else super.dispatchTouchEvent(event)
    }

    private fun setupViews() {
        setContentView(R.layout.activity_assistant)

        mWebView = findViewById(R.id.webview)
        mWebView.setInitialScale(100)
        mWebView.settings.javaScriptEnabled = true
        // 見辛いのでいったんやめる
//        mWebView.settings.useWideViewPort = true
//        mWebView.settings.loadWithOverviewMode = true
        mWebView.setBackgroundColor(resources.getColor(R.color.colorTransparent, theme))

        inputMessageView = findViewById(R.id.inputMessageView)
    }

    private fun setupObservers() {
        viewModel = AssistantActivityViewModel(applicationContext)

        val htmlObserver = Observer<String?>() {
            it?.let {
                mWebView.visibility = View.VISIBLE

                val data = it.toByteArray(charset(Charsets.UTF_8.name()))
                val base64String =
                    Base64.encodeToString(
                        data,
                        Base64.DEFAULT
                    )
                mWebView.loadData(base64String, "text/html; charset=utf-8", "base64")
            } ?: run {
                mWebView.visibility = View.GONE
            }
        }
        val messageObserver = Observer<String>() {
            inputMessageView.text = it
        }
        val messageColorObserver = Observer<Color>() {
            inputMessageView.setTextColor(it.toArgb())
        }
        val messageTextSizeObserver = Observer<Float>() {
            inputMessageView.textSize = it
        }
        val activityFinishTriggerObserver = Observer<UUID> {
            finish()
        }

        viewModel.html.observe(this, htmlObserver)
        viewModel.message.observe(this, messageObserver)
        viewModel.messageColor.observe(this, messageColorObserver)
        viewModel.messageTextSize.observe(this, messageTextSizeObserver)
        viewModel.activityFinishTrigger.observe(this, activityFinishTriggerObserver)
    }
}