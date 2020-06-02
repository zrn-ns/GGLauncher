package com.zrnns.gglauncher.settings_app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.zrnns.gglauncher.R
import com.zrnns.gglauncher.core.StandardAppPageFragment


class SettingsLauncherPageFragment: StandardAppPageFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        super.set(R.drawable.ic_app_icon_settings, R.string.app_name_settings)

        return view
    }
}
