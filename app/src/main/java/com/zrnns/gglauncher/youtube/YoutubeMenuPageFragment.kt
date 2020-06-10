package com.zrnns.gglauncher.youtube

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zrnns.gglauncher.R
import com.zrnns.gglauncher.core.StandardAppPageFragment

class YoutubeMenuPageFragment: StandardAppPageFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        super.set(R.drawable.ic_app_icon_youtube, R.string.app_name_youtube)

        return view
    }
}