package com.foxwoosh.radio.player.helpers

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import com.foxwoosh.radio.player.models.PlayerColors
import com.foxwoosh.radio.ui.theme.Tundora

object CoverColorExtractor {
    fun extractColors(bitmap: Bitmap): PlayerColors {
        val palette = Palette.Builder(bitmap).generate()

        val swatches = getSwatchesInPriorityOrder(palette)

        val firstSwatch = swatches.getOrNull(0)
        val secondSwatch = swatches.getOrNull(1)

        return PlayerColors(
            firstSwatch?.rgb?.let { Color(it) } ?: Color.Black,
            secondSwatch?.rgb?.let { Color(it) } ?: Tundora,
            firstSwatch?.bodyTextColor?.let { Color(it) } ?: Color.White,
            firstSwatch?.bodyTextColor?.let { Color(it) } ?: Color.White
        )
    }

    private fun getSwatchesInPriorityOrder(palette: Palette): List<Palette.Swatch> {
        val list = mutableListOf<Palette.Swatch>()

        palette.darkVibrantSwatch?.let { list.add(it) }
        palette.mutedSwatch?.let { list.add(it) }
        palette.darkMutedSwatch?.let { list.add(it) }
        palette.dominantSwatch?.let { list.add(it) }

        return list
    }
}