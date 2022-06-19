package com.foxwoosh.radio

import android.content.*
import android.net.Uri
import android.provider.Browser
import android.util.Log
import android.widget.Toast
import androidx.annotation.FloatRange
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json

val Insets = MutableStateFlow(WindowInsetsCompat.CONSUMED)

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

fun Color.adjustBrightness(@FloatRange(from = 0.0) factor: Float) : Color {
    require(factor > 0) { "brightness factor should be greater than 0" }

    return  Color(
        red = (red * factor).coerceAtMost(255f),
        green = (green * factor).coerceAtMost(255f),
        blue = (blue * factor).coerceAtMost(255f),
        alpha = alpha
    )
}

val AppJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    prettyPrint = true
}