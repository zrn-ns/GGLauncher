package com.zrnns.gglauncher.assistant.standard_version

import android.os.Bundle
import android.view.MotionEvent
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.zrnns.gglauncher.core.CommonPagerActivity
import com.zrnns.gglauncher.core.GlassGestureDetector
import com.zrnns.gglauncher.core.StandardImagePageFragment
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
            else -> {
                return super.onGesture(gesture)
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
        viewModel.assistantResultContent.observe(this, Observer<AssistantActivityViewModel.AssistantResultContent?> { content ->
            var newFragments: List<Fragment> = listOf()
            content?.let {
                val otherImageFragments = content.otherImageUrls.map {
                    StandardImagePageFragment(it)
                }
                newFragments = listOf(firstPageFragment) + otherImageFragments
            } ?: run {
                newFragments = listOf(firstPageFragment)
            }

            if (fragments.value != newFragments) {
                fragments.value = newFragments
            }

            val needsToHideTabLayout: Boolean = {
                content?.let {
                    it.otherImageUrls.isEmpty()
                } ?: run {
                    true
                }
            }()
            super.setTabLayoutHidden(needsToHideTabLayout)
        })
        viewModel.activityFinishTrigger.observe(this, Observer<UUID> {
            finish()
        })
    }
}