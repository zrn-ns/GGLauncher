package com.zrnns.gglauncher.youtube.model

import java.io.Serializable
import java.time.Duration
import java.util.*


class SearchResult(val title: String,
                   val publishedAt: Date,
                   val thumbnailUrl: String,
                   val channelName: String,
                   val duration: Duration,
                   val videoId: String): Serializable