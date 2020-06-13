package com.zrnns.gglauncher.youtube

import android.content.Intent
import android.os.Bundle
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
import com.zrnns.gglauncher.youtube.model.Playlist
import com.zrnns.gglauncher.youtube.model.SearchResult
import kotlinx.android.synthetic.main.fragment_common_pager.*
import java.time.Duration
import java.util.*
import kotlin.collections.ArrayList

class PlaylistsActivity : CommonPagerActivity() {

    companion object {
        const val ACTIVITY_INPUT_PLAYLISTS = "ACTIVITY_INPUT_PLAYLISTS"
    }

    private val appName by lazy { applicationContext.getString(R.string.app_name) }
    private val apiKey: String by lazy { applicationContext.resources.openRawResource(R.raw.google_cloud_api_key).bufferedReader().readLine() }
    private val youtube by lazy { YouTube.Builder(NetHttpTransport(), JacksonFactory(), HttpCredentialsAdapter(credentials)).setApplicationName(appName).build() }
    private val credentials by lazy { EmbeddedAssistant.generateCredentials(applicationContext, R.raw.credentials) }

    override var startPosition: Int = 0
    override var fragments: NonNullLiveData<List<Fragment>> = NonNullLiveData<List<Fragment>>(listOf(
        StandardTextPageFragment(R.string.youtube_menu_search)
    ))
    var playlists: List<Playlist> = listOf()
        set(value) {
            field = value
            fragments.value = value.map { StandardTextPageFragment(it.title) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val playlists = intent.getSerializableExtra(ACTIVITY_INPUT_PLAYLISTS) as ArrayList<*>
        this.playlists = playlists.filterIsInstance<Playlist>()
    }

    override fun onGesture(gesture: GlassGestureDetector.Gesture): Boolean {
        when (gesture) {
            GlassGestureDetector.Gesture.TAP -> {
                //TODO: open movies page
                fetchPlaylistItems()
            }
            else -> {
                return super.onGesture(gesture)
            }
        }
        return true
    }

    private fun fetchPlaylistItems() {
        Thread {
            // first, search videos
            val playlistItemSearch = youtube.PlaylistItems().list("id,snippet")
            playlistItemSearch.key = apiKey
            playlistItemSearch.playlistId = playlists[viewPager.currentItem].playlistId
            playlistItemSearch.maxResults = 50

            // next, get video details
            val detailsSearch = youtube.Videos().list("snippet,contentDetails")
            detailsSearch.key = apiKey
            detailsSearch.id = playlistItemSearch.execute().items.map { it.snippet.resourceId.videoId }.joinToString(",")

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
                intent.putExtra(
                    SearchResultsActivity.ACTIVITY_INPUT_SEARCH_RESULTS,
                    ArrayList(searchResults)
                )
                this.startActivity(intent)
            }
        }.start()
    }
}