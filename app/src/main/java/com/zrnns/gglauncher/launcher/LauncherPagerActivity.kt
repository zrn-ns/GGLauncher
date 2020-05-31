package com.zrnns.gglauncher.launcher

import android.view.KeyEvent
import androidx.fragment.app.Fragment
import com.zrnns.gglauncher.core.CommonPagerActivity
import com.zrnns.gglauncher.launcher.ClockFragment

class LauncherPagerActivity : CommonPagerActivity() {
    override fun startPosition(): Int = 2
    override fun fragments(): Array<Fragment> =
        arrayOf(
            ClockFragment(),
            ClockFragment(),
            ClockFragment(),
            ClockFragment(),
            ClockFragment()
        )
}