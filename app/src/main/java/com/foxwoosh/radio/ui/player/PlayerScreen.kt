package com.foxwoosh.radio.ui.player

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.animation.Animatable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.foxwoosh.radio.ui.CenteredProgress
import com.foxwoosh.radio.ui.theme.FoxyRadioTheme
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun PlayerScreen(owner: ViewModelStoreOwner) {
    val viewModel = viewModel<PlayerViewModel>(owner)

    val state by viewModel.stateFlow.collectAsState()

    Surface {
        when (state) {
            PlayerState.Loading -> CenteredProgress()
            is PlayerState.Done -> {
                val done = state as PlayerState.Done

                val initialSurfaceColor = MaterialTheme.colors.surface
                val initialTextColor = MaterialTheme.colors.onSurface

                var bitmap by remember { mutableStateOf<Bitmap?>(null) }
                var stateSurfaceColor by remember { mutableStateOf(initialSurfaceColor) }
                var statePrimaryTextColor by remember { mutableStateOf(initialTextColor) }
                var stateSecondaryTextColor by remember { mutableStateOf(initialTextColor) }

                val surfaceColor = animateColorAsState(
                    targetValue = stateSurfaceColor,
                    animationSpec = tween(1000)
                )
                val primaryTextColor = animateColorAsState(
                    targetValue = statePrimaryTextColor,
                    animationSpec = tween(1000)
                )
                val secondaryTextColor = animateColorAsState(
                    targetValue = stateSecondaryTextColor,
                    animationSpec = tween(1000)
                )

                Player(
                    done.title,
                    done.artist,
                    bitmap,
                    surfaceColor.value,
                    primaryTextColor.value,
                    secondaryTextColor.value
                ) {
                    viewModel.reload()
                }

                ExtractColors(imageUrl = done.imageUrl) { b, surface, primary, secondary ->
                    stateSurfaceColor = Color(surface)
                    statePrimaryTextColor = Color(primary)
                    stateSecondaryTextColor = Color(secondary)
                    bitmap = b

                    Log.i("DDLOG", "colors extracted")
                }
            }
        }
    }
}


@Composable
fun Player(
    title: String,
    artist: String,
    bitmap: Bitmap?,
    surfaceColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    onReload: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GlideImage(
            imageModel = bitmap,
            contentScale = ContentScale.Inside,
            contentDescription = "Avatar",
            modifier = Modifier
                .fillMaxWidth()
                .size(260.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.h6,
            color = primaryTextColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = artist,
            style = MaterialTheme.typography.body1,
            color = secondaryTextColor
        )

        Spacer(modifier = Modifier.height(100.dp))

        OutlinedButton(onClick = onReload) {
            Text(text = "Reload")
        }
    }
}

@Preview
@Composable
fun PreviewPlayer() {
    FoxyRadioTheme {
        Surface {
            Player(
                "Song name",
                "Artist",
                null,
                Color.Black,
                Color.White,
                Color.White
            ) {}
        }
    }
}


