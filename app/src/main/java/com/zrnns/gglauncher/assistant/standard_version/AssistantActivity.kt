package com.zrnns.gglauncher.assistant.standard_version

import android.graphics.Color
import android.opengl.Visibility
import android.os.Bundle
import android.util.Base64
import android.view.MotionEvent
import android.view.View
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import coil.api.clear
import coil.api.load
import com.zrnns.gglauncher.R
import com.zrnns.gglauncher.core.GlassGestureDetector
import java.util.*

class AssistantActivity : AppCompatActivity(), GlassGestureDetector.OnGestureListener {

    private lateinit var viewModel: AssistantActivityViewModel
    private val glassGestureDetector: GlassGestureDetector by lazy { GlassGestureDetector(this, this) }

    private lateinit var contentArea: LinearLayout
    private lateinit var resultImageView: ImageView
    private lateinit var resultTextView: TextView
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

        contentArea = findViewById(R.id.contentArea)
        resultImageView = findViewById(R.id.resultImageView)
        resultTextView = findViewById(R.id.resultTextView)
        inputMessageView = findViewById(R.id.inputMessageView)
    }

    private fun setupObservers() {
        viewModel =
            AssistantActivityViewModel(
                applicationContext
            )

        val contentObserver = Observer<AssistantActivityViewModel.AssistantResultContent?>() {
            it?.let {
                contentArea.visibility = View.VISIBLE
                resultTextView.text = it.text
                resultTextView.visibility = if (it.text.isNullOrEmpty()) View.GONE else View.VISIBLE
                resultImageView.load(it.firstImageUrl)
                resultImageView.visibility = if (it.firstImageUrl == null)  View.GONE else View.VISIBLE
            } ?: run {
                contentArea.visibility = View.GONE
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

        viewModel.assistantResultContent.observe(this, contentObserver)
        viewModel.message.observe(this, messageObserver)
        viewModel.messageColor.observe(this, messageColorObserver)
        viewModel.messageTextSize.observe(this, messageTextSizeObserver)
        viewModel.activityFinishTrigger.observe(this, activityFinishTriggerObserver)
    }
}