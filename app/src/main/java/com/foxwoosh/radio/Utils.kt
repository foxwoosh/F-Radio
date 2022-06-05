package com.foxwoosh.radio

import android.content.*
import android.content.ClipboardManager
import android.net.Uri
import android.provider.Browser
import android.util.Log
import android.widget.Toast
import kotlinx.serialization.json.Json

fun Context.openURL(url: String) {
    val validUrl = if (!url.startsWith("http", true)) {
        "http://$url"
    } else {
        url
    }

    val uri = Uri.parse(validUrl)
    val intent = Intent(Intent.ACTION_VIEW, uri)
    intent.putExtra(Browser.EXTRA_APPLICATION_ID, packageName)
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Log.w("Utils.openURL", "Activity was not found for intent, $intent")
    }
}

fun Context.copyToClipboard(text: String) {
    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    clipboardManager.setPrimaryClip(
        ClipData.newPlainText(
            "plain text",
            text
        )
    )
    Toast.makeText(this, R.string.common_copied_to_clipboard, Toast.LENGTH_SHORT).show()
}

val AppJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    prettyPrint = true
}