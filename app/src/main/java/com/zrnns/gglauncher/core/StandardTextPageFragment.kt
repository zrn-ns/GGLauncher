package com.zrnns.gglauncher.core

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.zrnns.gglauncher.R

class StandardTextPageFragment(val string: String?) : Fragment() {
    constructor(@StringRes stringRes: Int) : this(null) {
        this.stringRes = stringRes
    }

    private lateinit var textView: TextView

    private var stringRes: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_standard_text_page, container, false)
        // Inflate the layout for this fragment
        this.textView = view.findViewById(R.id.textView)

        string?.let {
            textView.text = it
        }
        stringRes?.let {
            textView.text = resources.getText(it)
        }

        return view
    }

}
