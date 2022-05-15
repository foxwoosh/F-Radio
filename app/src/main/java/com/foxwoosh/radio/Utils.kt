package com.foxwoosh.radio

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Browser
import android.util.Log

object Utils {
    fun openURL(context: Context, url: String) {
        val validUrl = if (!url.startsWith("http", true)) {
            "http://$url"
        } else {
            url
        }

        val uri = Uri.parse(validUrl)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.packageName)
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.w("Utils.openURL", "Activity was not found for intent, $intent")
        }
    }
}