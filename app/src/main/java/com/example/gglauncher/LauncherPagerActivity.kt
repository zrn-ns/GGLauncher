package com.example.gglauncher

import androidx.fragment.app.Fragment

class LauncherPagerActivity : CommonPagerActivity() {
    override fun startPosition(): Int = 2
    override fun fragments(): Array<Fragment> =
        arrayOf(ClockFragment(), ClockFragment(), ClockFragment(), ClockFragment(), ClockFragment())
}