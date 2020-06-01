package com.zrnns.gglauncher.core

import android.os.Bundle
import android.view.MotionEvent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.zrnns.gglauncher.R
import com.zrnns.gglauncher.camera_app.openCameraActivity


abstract class CommonPagerActivity : FragmentActivity(), GlassGestureDetector.OnGestureListener {

    private val glassGestureDetector: GlassGestureDetector by lazy { GlassGestureDetector(this, this) }

    abstract fun startPosition(): Int
    abstract fun fragments(): Array<Fragment>

    private lateinit var mPager: ViewPager
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_common_pager)

        mPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabDots)

        val pagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager)
        mPager.adapter = pagerAdapter
        mPager.currentItem = startPosition()
        mPager.setPageTransformer(true,
            ZoomOutPageTransformer()
        )

        tabLayout.setupWithViewPager(mPager, true)
    }

    override fun onBackPressed() {
        // Disable Back key
        selectTab(startPosition())
    }

    override fun onGesture(gesture: GlassGestureDetector.Gesture): Boolean {
        return when (gesture) {
            GlassGestureDetector.Gesture.TAP -> {
                onTapPage(mPager.currentItem)
                return true
            }
            else -> false
        }
    }

    open fun onTapPage(position: Int) {
        TODO("Your default implementation here")
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        return if (glassGestureDetector.onTouchEvent(event)) {
            true
        } else super.dispatchTouchEvent(event)
    }

    fun selectTab(position: Int) {
        mPager.currentItem = position
    }

    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) :
        FragmentStatePagerAdapter(fm) {
        override fun getCount(): Int = fragments().size
        override fun getItem(position: Int): Fragment = fragments().get(position)
    }
}