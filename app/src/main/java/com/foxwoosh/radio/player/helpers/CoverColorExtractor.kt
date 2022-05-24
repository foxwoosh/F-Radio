package com.foxwoosh.radio.player.helpers

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette

object CoverColorExtractor {
    fun extractColors(bitmap: Bitmap): Triple<Color, Color, Color> {
        val palette = Palette.Builder(bitmap).generate()

        return palette.darkVibrantSwatch?.let { getColorsFromSwatch(it) }
            ?: palette.mutedSwatch?.let { getColorsFromSwatch(it) }
            ?: palette.dominantSwatch?.let { getColorsFromSwatch(it) }
            ?: Triple(Color.Black, Color.White, Color.White)
    }

    private fun getColorsFromSwatch(swatch: Palette.Swatch) = Triple(
        Color(swatch.rgb),
        Color(swatch.bodyTextColor),
        Color(swatch.titleTextColor)
    )
}