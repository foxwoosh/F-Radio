package com.foxwoosh.radio.media_player

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color

data class MediaPlayerTrackData(
    val title: String,
    val artist: String,
    val album: String?,
    val cover: Bitmap,
    val surfaceColor: Color,
    val primaryTextColor: Color,
    val secondaryTextColor: Color,
    val musicServices: MusicServicesData
) {
    companion object {
        val buffering = MediaPlayerTrackData(
            "Buffering",
            "",
            null,
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888),
            Color.Black,
            Color.White,
            Color.White,
            MusicServicesData()
        )
    }
}