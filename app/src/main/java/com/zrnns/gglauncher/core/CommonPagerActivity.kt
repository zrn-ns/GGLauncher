package com.zrnns.gglauncher.core

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.zrnns.gglauncher.R

abstract class CommonPagerActivity : FragmentActivity() {

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

    fun selectTab(position: Int) {
        mPager.currentItem = position
    }

    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) :
        FragmentStatePagerAdapter(fm) {
        override fun getCount(): Int = fragments().size
        override fun getItem(position: Int): Fragment = fragments().get(position)
    }
}