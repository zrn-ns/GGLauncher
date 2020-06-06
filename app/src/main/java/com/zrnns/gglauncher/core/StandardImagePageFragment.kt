package com.zrnns.gglauncher.core

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import coil.api.load
import com.zrnns.gglauncher.R

class StandardImagePageFragment(private val imageUrl: String) : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_standard_image_page, container, false)
        val imageView: ImageView = view.findViewById(R.id.imageView)
        imageView.load(imageUrl)
        return view
    }

}
