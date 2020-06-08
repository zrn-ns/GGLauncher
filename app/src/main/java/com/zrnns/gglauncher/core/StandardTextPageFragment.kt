package com.zrnns.gglauncher.core

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.zrnns.gglauncher.R

class StandardTextPageFragment(@StringRes var stringRes: Int) : Fragment() {

    private lateinit var textView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_standard_text_page, container, false)
        // Inflate the layout for this fragment
        this.textView = view.findViewById(R.id.textView)
        textView.text = resources.getText(stringRes)

        return view
    }

}
