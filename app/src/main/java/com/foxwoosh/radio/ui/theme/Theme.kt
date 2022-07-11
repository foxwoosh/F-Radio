package com.foxwoosh.radio.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColorPalette = lightColors(
    primary = FoxyDark,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    background = CodGray,
    surface = CodGray,
)

@Composable
fun FoxyRadioTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colors = LightColorPalette,
        typography = Typography,
        shapes = Shapes
    ) {
        CompositionLocalProvider(
            LocalContentColor provides Color.White,
            content = content
        )
    }
}