package com.zrnns.gglauncher.assistant.standard_version

import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import coil.api.load
import com.zrnns.gglauncher.R
import com.zrnns.gglauncher.core.CommonPagerActivity
import com.zrnns.gglauncher.core.GlassGestureDetector
import com.zrnns.gglauncher.core.observer.NonNullLiveData
import java.util.*

class AssistantActivity : CommonPagerActivity() {

    private var firstPageFragment = AssistantFirstPageFragment()
    override var startPosition: Int = 0
    override var fragments = NonNullLiveData<List<Fragment>>(listOf(firstPageFragment))

    private lateinit var viewModel: AssistantActivityViewModel
    private val glassGestureDetector: GlassGestureDetector by lazy { GlassGestureDetector(this, this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = AssistantActivityViewModel(applicationContext)
        firstPageFragment.viewModel = viewModel

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

    private fun setupObservers() {
        val activityFinishTriggerObserver = Observer<UUID> {
            finish()
        }
        viewModel.activityFinishTrigger.observe(this, activityFinishTriggerObserver)
    }
}