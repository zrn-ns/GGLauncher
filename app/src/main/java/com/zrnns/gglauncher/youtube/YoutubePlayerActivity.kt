package com.zrnns.gglauncher.youtube

import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.zrnns.gglauncher.R
import com.zrnns.gglauncher.core.GlassGestureDetector
import kotlinx.android.synthetic.main.activity_youtube_player.*
import java.lang.Float.max


class YoutubePlayerActivity : AppCompatActivity(), GlassGestureDetector.OnGestureListener {

    companion object {
        const val ACTIVITY_INPUT_VIDEO_ID = "ACTIVITY_INPUT_VIDEO_ID"
    }

    private val glassGestureDetector: GlassGestureDetector by lazy { GlassGestureDetector(this, this) }
    private var youtubeVideoId: String = ""

    private var playerStatus: PlayerConstants.PlayerState = PlayerConstants.PlayerState.UNSTARTED
    private var playerCurrentSecond: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_youtube_player)
    }

    override fun onResume() {
        super.onResume()

        val youtubePlayerView =
            findViewById<YouTubePlayerView>(R.id.youtubePlayerView)
        youtubePlayerView.isEnabled = false
        lifecycle.addObserver(youtubePlayerView)

        val videoId = intent.getStringExtra(ACTIVITY_INPUT_VIDEO_ID)
        videoId?.let {
            this.youtubeVideoId = videoId
        }

        youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayer.loadVideo(youtubeVideoId, 0f)
                youTubePlayer.addListener(object: YouTubePlayerListener {
                    override fun onApiChange(youTubePlayer: YouTubePlayer) {
                    }
                    override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                        playerCurrentSecond = second
                    }
                    override fun onError(
                        youTubePlayer: YouTubePlayer,
                        error: PlayerConstants.PlayerError
                    ) {
                    }
                    override fun onPlaybackQualityChange(
                        youTubePlayer: YouTubePlayer,
                        playbackQuality: PlayerConstants.PlaybackQuality
                    ) {
                    }

                    override fun onPlaybackRateChange(
                        youTubePlayer: YouTubePlayer,
                        playbackRate: PlayerConstants.PlaybackRate
                    ) {
                    }

                    override fun onReady(youTubePlayer: YouTubePlayer) {
                    }

                    override fun onStateChange(
                        youTubePlayer: YouTubePlayer,
                        state: PlayerConstants.PlayerState
                    ) {
                        playerStatus = state
                    }

                    override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                    }

                    override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
                    }

                    override fun onVideoLoadedFraction(
                        youTubePlayer: YouTubePlayer,
                        loadedFraction: Float
                    ) {
                    }

                })
            }
        })
    }

    override fun onGesture(gesture: GlassGestureDetector.Gesture): Boolean {
        when (gesture) {
            GlassGestureDetector.Gesture.TAP -> {
                youtubePlayerView.getYouTubePlayerWhenReady(object: YouTubePlayerCallback {
                    override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                        when (playerStatus) {
                            PlayerConstants.PlayerState.PLAYING -> {
                                youTubePlayer.pause()
                            }
                            PlayerConstants.PlayerState.PAUSED -> {
                                youTubePlayer.play()
                            }
                        }
                    }
                })
                return true
            }
            GlassGestureDetector.Gesture.SWIPE_BACKWARD -> {
                youtubePlayerView.getYouTubePlayerWhenReady(object: YouTubePlayerCallback {
                    override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                        when (playerStatus) {
                            PlayerConstants.PlayerState.PLAYING, PlayerConstants.PlayerState.PAUSED -> {
                                val seek = max(playerCurrentSecond - 10, 0f)
                                youTubePlayer.seekTo(seek)
                            }
                        }
                    }
                })
            }
            GlassGestureDetector.Gesture.SWIPE_FORWARD -> {
                youtubePlayerView.getYouTubePlayerWhenReady(object: YouTubePlayerCallback {
                    override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                        when (playerStatus) {
                            PlayerConstants.PlayerState.PLAYING, PlayerConstants.PlayerState.PAUSED -> {
                                val seek = playerCurrentSecond + 10
                                youTubePlayer.seekTo(seek)
                            }
                        }
                    }
                })
            }
            GlassGestureDetector.Gesture.SWIPE_DOWN -> {
                finish()
                return true
            }
        }
        return true
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        return if (glassGestureDetector.onTouchEvent(event)) {
            true
        } else super.dispatchTouchEvent(event)
    }
}