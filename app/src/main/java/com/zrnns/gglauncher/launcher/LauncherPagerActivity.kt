package com.zrnns.gglauncher.launcher

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import com.zrnns.gglauncher.R
import com.zrnns.gglauncher.assistant.standard_version.AssistantActivity
import com.zrnns.gglauncher.camera_app.CameraPageFragment
import com.zrnns.gglauncher.camera_app.openCameraActivity
import com.zrnns.gglauncher.core.CommonPagerActivity
import com.zrnns.gglauncher.core.GlassGestureDetector
import com.zrnns.gglauncher.core.observer.NonNullLiveData
import com.zrnns.gglauncher.gallery_app.GalleryPageFragment
import com.zrnns.gglauncher.gallery_app.openGalleryActivity
import com.zrnns.gglauncher.settings_app.SettingsLauncherPageFragment
import com.zrnns.gglauncher.settings_app.openAndroidSettingsActivity
import com.zrnns.gglauncher.youtube.YoutubeMenuActivity
import com.zrnns.gglauncher.youtube.YoutubePageFragment
import kotlinx.android.synthetic.main.fragment_common_pager.*

class LauncherPagerActivity : CommonPagerActivity() {

    override var startPosition: Int = 1
    override var fragments = NonNullLiveData<List<Fragment>>(listOf(
        SettingsLauncherPageFragment(),
        ClockFragment(),
        YoutubePageFragment(),
        CameraPageFragment(),
        GalleryPageFragment()
    ).map {
        it.view?.setBackgroundColor(resources.getColor(R.color.colorBackgroundView, theme))
        it
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewPager.setBackgroundColor(resources.getColor(R.color.colorPagerBackgroundView, theme))
    }

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

    override fun onGesture(gesture: GlassGestureDetector.Gesture): Boolean {
        return when (gesture) {
            GlassGestureDetector.Gesture.SWIPE_DOWN -> {
                // Do Nothing
                return true
            }
            else -> return super.onGesture(gesture)
        }
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
                val intent = Intent(this, YoutubeMenuActivity::class.java)
                this.startActivity(intent)
            }
            3 -> {
                openCameraActivity(this)
            }
            4 -> {
                openGalleryActivity(this)
            }
        }
    }

    private fun openCameraActivityIfInstalled() {
        openCameraActivity(this)
    }
}