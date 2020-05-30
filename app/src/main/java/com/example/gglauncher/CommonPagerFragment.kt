package com.example.gglauncher

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_common_pager.*

class CommonPagerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_common_pager, container, false)
    }

}

private const val START_POSITION = 2
private val FRAGMENTS = arrayOf(ClockFragment(), ClockFragment(), ClockFragment(), ClockFragment(), ClockFragment())

class CommonPagerActivity : FragmentActivity() {

    private lateinit var mPager: ViewPager
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_common_pager)

        mPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabDots)

        val pagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager)
        mPager.adapter = pagerAdapter
        mPager.currentItem = START_POSITION
        mPager.setPageTransformer(true, ZoomOutPageTransformer())

        tabLayout.setupWithViewPager(mPager, true)
    }

    override fun onBackPressed() {
        // Disable Back key
    }

    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getCount(): Int = FRAGMENTS.size
        override fun getItem(position: Int): Fragment = FRAGMENTS.get(position)
    }
}