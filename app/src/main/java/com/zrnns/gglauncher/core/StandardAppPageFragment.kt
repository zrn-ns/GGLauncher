package com.zrnns.gglauncher.core

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.zrnns.gglauncher.R

/**
 * A simple [Fragment] subclass.
 */
abstract class StandardAppPageFragment : Fragment() {

    private lateinit var iconImageView: ImageView
    private lateinit var appNameTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_standard_app_page, container, false)
        // Inflate the layout for this fragment
        this.iconImageView = view.findViewById(R.id.iconImageView)
        this.appNameTextView = view.findViewById(R.id.appNameTextView)

        return view
    }

    fun set(@DrawableRes iconImageRes: Int, @StringRes appNameTextRes: Int) {
        iconImageView.setImageResource(iconImageRes)
        appNameTextView.setText(appNameTextRes)
    }

}
