package com.zrnns.gglauncher.gallery_app

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast


private const val PACKAGE_NAME = "com.example.glass.gallerysample"
private const val CLASS_NAME = "com.example.glass.gallerysample.MainActivity"

// Open Gallery Sample App
// https://github.com/googlesamples/glass-enterprise-samples/tree/master/GallerySample
fun openGalleryActivity(fromActivity: Activity) {
    val intent = OpenCameraAppIntent()
    if (fromActivity.packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL).count() > 0) {
        fromActivity.startActivity(intent)
    } else {
        Toast.makeText(
            fromActivity.applicationContext,
            "App Not Installed. Please install from https://github.com/googlesamples/glass-enterprise-samples.",
            Toast.LENGTH_LONG
        ).show()
    }
}

private class OpenCameraAppIntent: Intent() {
    init {
        setClassName(
            PACKAGE_NAME,
            CLASS_NAME
        )
    }
}