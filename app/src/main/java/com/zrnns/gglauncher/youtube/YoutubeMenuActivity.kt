package com.zrnns.gglauncher.youtube

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.fragment.app.Fragment
import com.zrnns.gglauncher.R
import com.zrnns.gglauncher.core.CommonPagerActivity
import com.zrnns.gglauncher.core.GlassGestureDetector
import com.zrnns.gglauncher.core.StandardTextPageFragment
import com.zrnns.gglauncher.core.observer.NonNullLiveData


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
            val results: List<String>? =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (results != null && results.isNotEmpty() && results[0].isNotEmpty()) {
                // TODO: open search result page
                // updateUI(results[0])
            }
        } else {
            // TODO: show error if needed
        }
    }

    private fun requestVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.ACTION_WEB_SEARCH,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        startActivityForResult(
            intent,
            REQUEST_CODE
        )
    }
}