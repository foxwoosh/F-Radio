package com.foxwoosh.radio.player.models

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color

data class PlayerTrackData(
    val title: String,
    val artist: String,
    val album: String?,
    val cover: Bitmap?,
    val surfaceColor: Color,
    val primaryTextColor: Color,
    val secondaryTextColor: Color,
    val musicServices: MusicServicesData
) {
    companion object {
        val chooseStation = PlayerTrackData(
            "Choose station",
            "",
            null,
            null,
            Color.Black,
            Color.White,
            Color.White,
            MusicServicesData.empty
        )

        val buffering = PlayerTrackData(
            "Buffering...",
            "",
            null,
            null,
            Color.Black,
            Color.White,
            Color.White,
            MusicServicesData.empty
        )
    }
}