package com.zrnns.gglauncher.launcher

import android.view.KeyEvent
import androidx.fragment.app.Fragment
import com.zrnns.gglauncher.camera_app.CameraPageFragment
import com.zrnns.gglauncher.camera_app.openCameraActivity
import com.zrnns.gglauncher.core.CommonPagerActivity

class LauncherPagerActivity : CommonPagerActivity() {

    override fun startPosition(): Int = 0
    override fun fragments(): Array<Fragment> =
        arrayOf(
            ClockFragment(),
            CameraPageFragment()
        )

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_CAMERA) {
            openCameraActivityIfInstalled()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_CAMERA) {
            openCameraActivityIfInstalled()
            return true
        }
        return super.onKeyLongPress(keyCode, event)
    }

    override fun onTapPage(position: Int) {
        when (position) {
            1 -> {
                openCameraActivity(this)
            }
        }
    }

    private fun openCameraActivityIfInstalled() {
        openCameraActivity(this)
    }
}