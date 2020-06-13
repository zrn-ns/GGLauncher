package com.zrnns.gglauncher.youtube

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.auth.http.HttpCredentialsAdapter
import com.zrnns.gglauncher.R
import com.zrnns.gglauncher.assistant.EmbeddedAssistant
import com.zrnns.gglauncher.core.CommonPagerActivity
import com.zrnns.gglauncher.core.GlassGestureDetector
import com.zrnns.gglauncher.core.StandardTextPageFragment
import com.zrnns.gglauncher.core.observer.NonNullLiveData
import com.zrnns.gglauncher.core.speech_recognizer.SpeechRecognizerActivity
import com.zrnns.gglauncher.youtube.model.Playlist
import com.zrnns.gglauncher.youtube.model.SearchResult
import kotlinx.android.synthetic.main.fragment_common_pager.*
import java.time.Duration
import java.util.*


class YoutubeMenuActivity : CommonPagerActivity() {

    companion object {
        private const val REQUEST_CODE = 999
    }

    override var startPosition: Int = 0
    override var fragments: NonNullLiveData<List<Fragment>> = NonNullLiveData<List<Fragment>>(listOf(
            StandardTextPageFragment(R.string.youtube_menu_search),
            StandardTextPageFragment(R.string.youtube_menu_playlists)
        ))

    private val appName by lazy { applicationContext.getString(R.string.app_name) }
    private val apiKey: String by lazy { applicationContext.resources.openRawResource(R.raw.google_cloud_api_key).bufferedReader().readLine() }
    private val youtube by lazy { YouTube.Builder(NetHttpTransport(), JacksonFactory(), HttpCredentialsAdapter(credentials)).setApplicationName(appName).build() }
    private val credentials by lazy { EmbeddedAssistant.generateCredentials(applicationContext, R.raw.credentials) }

    override fun onGesture(gesture: GlassGestureDetector.Gesture): Boolean {
        when (gesture) {
            GlassGestureDetector.Gesture.TAP -> {
                when (viewPager.currentItem) {
                    0 -> {
                        requestVoiceRecognition()
                    }
                    1 -> {
                        fetchPlaylists()
                    }
                }
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
            resultText?.let { speechText ->

                Thread {
                    // first, search videos
                    val search = youtube.Search().list("id")
                    search.key = apiKey
                    search.type = "video"
                    search.q = speechText
                    search.maxResults = 20
                    search.regionCode = Locale.getDefault().country

                    // next, get video details
                    val detailsSearch = youtube.Videos().list("snippet,contentDetails")
                    detailsSearch.key = apiKey
                    detailsSearch.id = search.execute().items.map { it.id.videoId }.joinToString(",")

                    val searchResults = detailsSearch.execute().items.map {
                        SearchResult(it.snippet.title,
                            Date(it.snippet.publishedAt.value),
                            it.snippet.thumbnails.medium.url,
                            it.snippet.channelTitle,
                            Duration.parse(it.contentDetails.duration),
                            it.id)
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

    private fun fetchPlaylists() {
        Thread {
            // first, search videos
            val search = youtube.Playlists().list("snippet,contentDetails")
            search.key = apiKey
            search.maxResults = 50
            search.mine = true
            val playlists = search.execute().items.filter { it.contentDetails.itemCount > 0 }.sortedByDescending { it.contentDetails.itemCount }.map {
                Playlist(it.snippet.title, it.snippet.thumbnails.medium.url, it.id)
            }

            runOnUiThread {
                val intent = Intent(this, PlaylistsActivity::class.java)
                intent.putExtra(
                    PlaylistsActivity.ACTIVITY_INPUT_PLAYLISTS,
                    ArrayList(playlists)
                )
                this.startActivity(intent)
            }
        }.start()
    }
}