package com.zrnns.gglauncher.core.utils

import java.time.Duration

class DurationUtils {
    companion object {
        fun format(duration: Duration): String {
            val seconds: Long = duration.seconds % 60
            val minutes: Long = duration.seconds / 60 % 60
            val hours: Long = duration.seconds / 3600
            return String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
    }
}