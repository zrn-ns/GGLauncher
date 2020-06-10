package com.zrnns.gglauncher.youtube

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.zrnns.gglauncher.R
import com.zrnns.gglauncher.core.CommonPagerActivity
import com.zrnns.gglauncher.core.GlassGestureDetector
import com.zrnns.gglauncher.core.StandardTextPageFragment
import com.zrnns.gglauncher.core.observer.NonNullLiveData
import com.zrnns.gglauncher.youtube.model.SearchResult
import kotlinx.android.synthetic.main.fragment_common_pager.*

class SearchResultsActivity : CommonPagerActivity() {

    companion object {
        const val ACTIVITY_INPUT_SEARCH_RESULTS = "ACTIVITY_INPUT_SEARCH_RESULTS"
    }

    override var startPosition: Int = 0
    override var fragments: NonNullLiveData<List<Fragment>> = NonNullLiveData<List<Fragment>>(listOf(
        StandardTextPageFragment(R.string.youtube_menu_search)
    ))
    var searchResults: List<SearchResult> = listOf()
        set(value) {
            field = value
            fragments.value = value.map { SearchResultPageFragment(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val searchResults = intent.getSerializableExtra(ACTIVITY_INPUT_SEARCH_RESULTS) as ArrayList<*>
        this.searchResults = searchResults.filterIsInstance<SearchResult>()
    }

    override fun onGesture(gesture: GlassGestureDetector.Gesture): Boolean {
        when (gesture) {
            GlassGestureDetector.Gesture.TAP -> {
                openYoutubePlayer()
            }
            else -> {
                return super.onGesture(gesture)
            }
        }
        return true
    }

    private fun openYoutubePlayer() {
        val videoId = searchResults[viewPager.currentItem].videoId
        val intent = Intent(this, YoutubePlayerActivity::class.java)
        intent.putExtra(YoutubePlayerActivity.ACTIVITY_INPUT_VIDEO_ID, videoId)
        this.startActivity(intent)
    }
}