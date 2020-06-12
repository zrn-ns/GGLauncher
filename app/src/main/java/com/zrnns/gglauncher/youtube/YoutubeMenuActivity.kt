package com.zrnns.gglauncher.youtube

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.zrnns.gglauncher.R
import com.zrnns.gglauncher.core.CommonPagerActivity
import com.zrnns.gglauncher.core.GlassGestureDetector
import com.zrnns.gglauncher.core.StandardTextPageFragment
import com.zrnns.gglauncher.core.observer.NonNullLiveData
import com.zrnns.gglauncher.core.speech_recognizer.SpeechRecognizerActivity
import com.zrnns.gglauncher.youtube.model.SearchResult
import java.util.*


class YoutubeMenuActivity : CommonPagerActivity() {

    companion object {
        private const val REQUEST_CODE = 999
    }

    override var startPosition: Int = 0
    override var fragments: NonNullLiveData<List<Fragment>> = NonNullLiveData<List<Fragment>>(listOf(
            StandardTextPageFragment(R.string.youtube_menu_search)
        ))

    override fun onGesture(gesture: GlassGestureDetector.Gesture): Boolean {
        when (gesture) {
            GlassGestureDetector.Gesture.TAP -> {
                requestVoiceRecognition()
            }
            else -> {
                return super.onGesture(gesture)
            }
        }
        return true
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val resultText: String? = data?.getStringExtra(SpeechRecognizerActivity.EXTRA_RESULT_TEXT)
            resultText?.let {
                val q = it

                Thread {
                    val appName = applicationContext.getString(R.string.app_name)
                    val apiKey: String = applicationContext.resources.openRawResource(R.raw.google_cloud_api_key).bufferedReader().readLine()

                    val youtube = YouTube.Builder(NetHttpTransport(), JacksonFactory(), null).setApplicationName(appName).build()
                    val search = youtube.Search().list("snippet")
                    search.key = apiKey
                    search.type = "video"
                    search.q = q
                    search.maxResults = 20
                    search.regionCode = Locale.getDefault().country

                    val res = search.execute()
                    val searchResults = res.items.map {
                        SearchResult(it.snippet.title,
                            Date(it.snippet.publishedAt.value),
                            it.snippet.thumbnails.medium.url,
                            it.snippet.channelTitle,
                            it.id.videoId)
                    }

                    runOnUiThread {
                        val intent = Intent(this, SearchResultsActivity::class.java)
                        intent.putExtra(SearchResultsActivity.ACTIVITY_INPUT_SEARCH_RESULTS, ArrayList(searchResults))
                        this.startActivity(intent)
                    }

                }.start()
            }
        } else {
            // TODO: show error if needed
        }
    }

    private fun requestVoiceRecognition() {
        val intent = Intent(this, SpeechRecognizerActivity::class.java)
        startActivityForResult(
            intent,
            REQUEST_CODE
        )
    }
}