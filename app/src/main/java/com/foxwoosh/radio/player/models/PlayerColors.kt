package com.foxwoosh.radio.player.models

import androidx.compose.ui.graphics.Color
import com.foxwoosh.radio.ui.theme.Tundora

data class PlayerColors(
    val surfaceColor: Color,
    val vibrantSurfaceColor: Color,
    val primaryTextColor: Color,
    val secondaryTextColor: Color
) {
    companion object {
        val default = PlayerColors(
            Tundora,
            Tundora,
            Color.White,
            Color.White
        )
    }
}