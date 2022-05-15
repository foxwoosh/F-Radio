package com.foxwoosh.radio.ui.player

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.foxwoosh.radio.R
import com.foxwoosh.radio.Utils
import com.foxwoosh.radio.ui.CenteredProgress
import com.foxwoosh.radio.ui.borderlessClickable
import com.foxwoosh.radio.ui.theme.FoxyRadioTheme

private const val colorsChangeDuration = 1_000

@Composable
fun PlayerScreen(owner: ViewModelStoreOwner) {
    val viewModel = viewModel<PlayerViewModel>(owner)

    val state by viewModel.stateFlow.collectAsState()
    var playerVisible by rememberSaveable { mutableStateOf(false) }

    Surface {
        when (state) {
            PlayerState.Loading -> CenteredProgress()
            is PlayerState.Done -> {
                val done = state as PlayerState.Done

                AnimatedVisibility(
                    visible = playerVisible,
                    enter = fadeIn(animationSpec = tween(colorsChangeDuration))
                ) {
                    Player(
                        title = done.title,
                        artist = done.artist,
                        bitmap = done.bitmap,
                        surfaceColor = animateColorAsState(
                            targetValue = done.surfaceColor,
                            animationSpec = tween(colorsChangeDuration)
                        ).value,
                        primaryTextColor = animateColorAsState(
                            targetValue = done.primaryTextColor,
                            animationSpec = tween(colorsChangeDuration)
                        ).value,
                        secondaryTextColor = animateColorAsState(
                            targetValue = done.secondaryTextColor,
                            animationSpec = tween(colorsChangeDuration)
                        ).value,
                        musicServices = done.musicServices
                    ) {
                        viewModel.reload()
                    }
                }

                playerVisible = true
            }
        }
    }
}


@Composable
fun Player(
    title: String,
    artist: String,
    bitmap: Bitmap,
    surfaceColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    musicServices: MusicServices = MusicServices(),
    onReload: () -> Unit
) {
    var musicServicesMenuOpened by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val cfg = LocalConfiguration.current
        val context = LocalContext.current

        Box {
            PlayerMusicServicesRow(
                musicServices = musicServices,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .scale(animateFloatAsState(if (musicServicesMenuOpened) 1f else 0f).value),
                musicServiceSelected = { url ->
                    Utils.openURL(context, url)
                    musicServicesMenuOpened = false
                }
            )

            Crossfade(targetState = bitmap) {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentScale = ContentScale.Fit,
                    contentDescription = "Cover",
                    modifier = Modifier
                        .size(cfg.screenWidthDp.dp - 64.dp)
                        .aspectRatio(1f)
                        .graphicsLayer(
                            shadowElevation = animateFloatAsState(
                                if (musicServicesMenuOpened) 30f else 0f
                            ).value,
                            scaleX = animateFloatAsState(
                                targetValue = if (musicServicesMenuOpened) 0.5f else 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy
                                )
                            ).value,
                            scaleY = animateFloatAsState(
                                targetValue = if (musicServicesMenuOpened) 0.5f else 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy
                                )
                            ).value
                        )
                        .clickable { musicServicesMenuOpened = !musicServicesMenuOpened }
                )
            }
        }

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

@Composable
fun PlayerMusicServicesRow(
    musicServices: MusicServices,
    modifier: Modifier = Modifier,
    musicServiceSelected: (url: String) -> Unit
) {
    if (musicServices.hasSomething) {
        Row(modifier) {
            if (!musicServices.youtubeMusic.isNullOrEmpty()) {
                Image(
                    painter = painterResource(id = R.drawable.ic_youtube_music),
                    contentDescription = "Youtube Music",
                    modifier = Modifier
                        .size(48.dp)
                        .borderlessClickable { musicServiceSelected(musicServices.youtubeMusic) }
                )
            }
            Spacer(modifier = Modifier.width(8.dp))

            if (!musicServices.youtube.isNullOrEmpty()) {
                Image(
                    painter = painterResource(id = R.drawable.ic_youtube_play),
                    contentDescription = "Youtube",
                    modifier = Modifier
                        .size(48.dp)
                        .borderlessClickable { musicServiceSelected(musicServices.youtube) }
                )
            }
            Spacer(modifier = Modifier.width(8.dp))

            if (!musicServices.spotify.isNullOrEmpty()) {
                Image(
                    painter = painterResource(id = R.drawable.ic_spotify),
                    contentDescription = "Spotify",
                    modifier = Modifier
                        .size(48.dp)
                        .borderlessClickable { musicServiceSelected(musicServices.spotify) }
                )
            }
            Spacer(modifier = Modifier.width(8.dp))

            if (!musicServices.iTunes.isNullOrEmpty()) {
                Image(
                    painter = painterResource(id = R.drawable.ic_itunes),
                    contentDescription = "iTunes",
                    modifier = Modifier
                        .size(48.dp)
                        .borderlessClickable { musicServiceSelected(musicServices.iTunes) }
                )
            }
            Spacer(modifier = Modifier.width(8.dp))

            if (!musicServices.yandexMusic.isNullOrEmpty()) {
                Image(
                    painter = painterResource(id = R.drawable.ic_yandex_music),
                    contentDescription = "Yandex Music",
                    modifier = Modifier
                        .size(48.dp)
                        .padding(4.dp)
                        .borderlessClickable { musicServiceSelected(musicServices.yandexMusic) }
                )
            }
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
                LocalContext.current.getDrawable(R.drawable.ic_youtube_music)?.toBitmap()!!,
                Color.Black,
                Color.White,
                Color.White
            ) {}
        }
    }
}


