package com.zrnns.gglauncher.settings_app

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast

private const val PACKAGE_NAME = "com.google.android.glass.settings"
private const val CLASS_NAME = "com.google.android.glass.settings.MainActivity"

fun openAndroidSettingsActivity(fromActivity: Activity) {
    val intent = OpenAndroidSettingsIntent()
    if (fromActivity.packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL).count() > 0) {
        fromActivity.startActivity(intent)
    } else {
        Toast.makeText(
            fromActivity.applicationContext,
            "App Not Installed.",
            Toast.LENGTH_LONG
        ).show()
    }
}

private class OpenAndroidSettingsIntent: Intent() {
    init {
        setClassName(
            PACKAGE_NAME,
            CLASS_NAME
        )
    }
}
