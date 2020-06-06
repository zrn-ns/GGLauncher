package com.zrnns.gglauncher.core

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.zrnns.gglauncher.R
import com.zrnns.gglauncher.core.observer.NonNullLiveData
import com.zrnns.gglauncher.core.observer.NonNullObserver


abstract class CommonPagerActivity : FragmentActivity(), GlassGestureDetector.OnGestureListener {

    private val glassGestureDetector: GlassGestureDetector by lazy { GlassGestureDetector(this, this) }

    abstract var startPosition: Int
    abstract var fragments: NonNullLiveData<List<Fragment>>

    private lateinit var mPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private var currentPosition: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_common_pager)

        mPager = findViewById(R.id.viewPager)
        mPager.addOnPageChangeListener(object: ViewPager.SimpleOnPageChangeListener() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                currentPosition = position
            }
        })
        tabLayout = findViewById(R.id.tabDots)

        fragments.observe(this, NonNullObserver {
            val pagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager)
            mPager.adapter = pagerAdapter
            mPager.currentItem = {
                currentPosition?.let {
                    if (it < fragments.value.count()) {
                        it
                    } else {
                        fragments.value.count()
                    }
                } ?: run {
                    startPosition
                }
            }()

            mPager.setPageTransformer(true,
                ZoomOutPageTransformer()
            )
        })

        tabLayout.setupWithViewPager(mPager, true)
    }

    override fun onBackPressed() {
        // Disable Back key
        selectTab(startPosition)
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

    fun setTabLayoutHidden(isHidden: Boolean) {
        tabLayout.visibility = if (isHidden) View.GONE else View.VISIBLE
    }

    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) :
        FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int {
            fragments.value.let {
                return it.size
            }
        }
        override fun getItem(position: Int): Fragment {
            return fragments.value[position]
        }
    }
}