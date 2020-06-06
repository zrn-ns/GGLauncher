package com.zrnns.gglauncher.assistant.standard_version

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import coil.api.load
import com.zrnns.gglauncher.R

class AssistantFirstPageFragment() : Fragment() {

    private lateinit var contentArea: LinearLayout
    private lateinit var resultImageView: ImageView
    private lateinit var resultTextView: TextView
    private lateinit var inputMessageView: TextView

    var viewModel: AssistantActivityViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_assistant_first_page, container, false)
        contentArea = view.findViewById(R.id.contentArea)
        resultImageView = view.findViewById(R.id.resultImageView)
        resultTextView = view.findViewById(R.id.resultTextView)
        inputMessageView = view.findViewById(R.id.inputMessageView)

        setupObservers()

        return view
    }

    private fun setupObservers() {
        val contentObserver = Observer<AssistantActivityViewModel.AssistantResultContent?>() {
            it?.let {
                contentArea.visibility = View.VISIBLE
                resultTextView.text = it.text
                resultTextView.visibility = if (it.text.isNullOrEmpty()) View.GONE else View.VISIBLE
                resultImageView.load(it.firstImageUrl)
                resultImageView.visibility = if (it.firstImageUrl == null)  View.GONE else View.VISIBLE
            } ?: run {
                contentArea.visibility = View.GONE
            }
        }
        val messageObserver = Observer<String>() {
            inputMessageView.text = it
        }
        val messageColorObserver = Observer<Color>() {
            inputMessageView.setTextColor(it.toArgb())
        }
        val messageTextSizeObserver = Observer<Float>() {
            inputMessageView.textSize = it
        }

        viewModel?.let {
            it.assistantResultContent.observe(this, contentObserver)
            it.message.observe(this, messageObserver)
            it.messageColor.observe(this, messageColorObserver)
            it.messageTextSize.observe(this, messageTextSizeObserver)
        }
    }
}
