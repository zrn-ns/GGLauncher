package com.zrnns.gglauncher.youtube

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import coil.api.load
import com.zrnns.gglauncher.R
import com.zrnns.gglauncher.youtube.model.SearchResult
import kotlinx.android.synthetic.main.fragment_youtube_movie_page.*
import java.text.SimpleDateFormat
import java.util.*

class SearchResultPageFragment(
    private val dependency: SearchResult
): Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_youtube_movie_page, container, false)
    }

    override fun onResume() {
        super.onResume()

        titleTextView.text = dependency.title
        uploadDateTextView.text = formattedDate(dependency.publishedAt)
        thumbnailImageView.load(dependency.thumbnailUrl)
        channelNameTextView.text = dependency.channelName
    }

    private fun formattedDate(date: Date): String {
        val local = Locale.getDefault()
        val format = android.text.format.DateFormat.getBestDateTimePattern(local, "yyyyMMdd")
        val dateFormat = SimpleDateFormat(format, local)

        return dateFormat.format(date)
    }
}