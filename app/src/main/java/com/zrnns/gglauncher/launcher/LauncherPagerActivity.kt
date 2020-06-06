package com.zrnns.gglauncher.launcher

import android.content.Intent
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import com.zrnns.gglauncher.assistant.standard_version.AssistantActivity
import com.zrnns.gglauncher.camera_app.CameraPageFragment
import com.zrnns.gglauncher.camera_app.openCameraActivity
import com.zrnns.gglauncher.core.CommonPagerActivity
import com.zrnns.gglauncher.core.observer.NonNullLiveData
import com.zrnns.gglauncher.gallery_app.GalleryPageFragment
import com.zrnns.gglauncher.gallery_app.openGalleryActivity
import com.zrnns.gglauncher.settings_app.SettingsLauncherPageFragment
import com.zrnns.gglauncher.settings_app.openAndroidSettingsActivity

class LauncherPagerActivity : CommonPagerActivity() {

    override var startPosition: Int = 1
    override var fragments = NonNullLiveData<List<Fragment>>(listOf(
        SettingsLauncherPageFragment(),
        ClockFragment(),
        CameraPageFragment(),
        GalleryPageFragment()
    ))

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
            0 -> {
                openAndroidSettingsActivity(this)
            }
            1 -> {
                val intent = Intent(this, AssistantActivity::class.java)
                this.startActivity(intent)
            }
            2 -> {
                openCameraActivity(this)
            }
            3 -> {
                openGalleryActivity(this)
            }
        }
    }

    private fun openCameraActivityIfInstalled() {
        openCameraActivity(this)
    }
}