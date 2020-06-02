package com.zrnns.gglauncher.camera_app

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zrnns.gglauncher.R
import com.zrnns.gglauncher.core.StandardAppPageFragment
import java.util.*

class CameraPageFragment: StandardAppPageFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        super.set(R.drawable.ic_app_icon_camera, R.string.app_name_camera)

        return view
    }
}