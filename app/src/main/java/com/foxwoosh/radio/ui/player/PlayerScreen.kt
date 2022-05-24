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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.foxwoosh.radio.R
import com.foxwoosh.radio.Utils
import com.foxwoosh.radio.player.models.MusicServicesData
import com.foxwoosh.radio.player.PlayerService
import com.foxwoosh.radio.player.models.Station
import com.foxwoosh.radio.ui.borderlessClickable
import com.foxwoosh.radio.ui.theme.FoxyRadioTheme
import kotlinx.coroutines.launch

private const val colorsChangeDuration = 1_000

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PlayerScreen(owner: ViewModelStoreOwner) {
    val context = LocalContext.current
    val viewModel = viewModel<PlayerViewModel>(owner)

    val trackData by viewModel.trackDataFlow.collectAsState()
    val isPlaying by viewModel.isPlayingFlow.collectAsState()

    val stationSelectorState = rememberBackdropScaffoldState(initialValue = BackdropValue.Concealed)
    val scope = rememberCoroutineScope()

    Surface {
        BackdropScaffold(
            appBar = {
                TopAppBar(
                    modifier = Modifier
                        .statusBarsPadding(),
                    elevation = 0.dp,
                    title = {
                        Text(text = "Select station")
                    }
                )
            },
            peekHeight = 0.dp,
            backLayerContent = {
                StationsList {
                    PlayerService.selectSource(context = context, station = it)
                    scope.launch { stationSelectorState.conceal() }
                }
            },
            scaffoldState = stationSelectorState,
            frontLayerContent = {
                Player(
                    title = trackData.title,
                    artist = trackData.artist,
                    cover = trackData.cover,
                    surfaceColor = animateColorAsState(
                        targetValue = trackData.surfaceColor,
                        animationSpec = tween(colorsChangeDuration)
                    ).value,
                    primaryTextColor = animateColorAsState(
                        targetValue = trackData.primaryTextColor,
                        animationSpec = tween(colorsChangeDuration)
                    ).value,
                    secondaryTextColor = animateColorAsState(
                        targetValue = trackData.secondaryTextColor,
                        animationSpec = tween(colorsChangeDuration)
                    ).value,
                    musicServices = trackData.musicServices,
                    selectStation = {
                        scope.launch { stationSelectorState.reveal() }
                    },
                    isPlaying = isPlaying
                )
            }
        )
    }
}

@Composable
fun Player(
    title: String,
    artist: String,
    cover: Bitmap?,
    surfaceColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    musicServices: MusicServicesData = MusicServicesData(),
    selectStation: () -> Unit,
    isPlaying: Boolean
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

            Crossfade(targetState = cover) {
                Image(
                    bitmap = it?.asImageBitmap()
                        ?: context
                            .getDrawable(R.drawable.ic_no_music_playing)
                            ?.toBitmap()
                            ?.asImageBitmap()!!,
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

        Spacer(modifier = Modifier.height(64.dp))

        PlayerController(color = primaryTextColor, isPlaying = isPlaying)
    }

    TopAppBar(
        title = {
            Text(
                text = "Station",
                color = primaryTextColor
            )
        },
        backgroundColor = Color.Transparent,
        navigationIcon = {
            Image(
                painter = painterResource(id = R.drawable.ic_arrow_down),
                contentDescription = "Select",
                modifier = Modifier
                    .borderlessClickable(onClick = selectStation)
                    .padding(16.dp),
                colorFilter = ColorFilter.tint(primaryTextColor)
            )
        },
        modifier = Modifier.statusBarsPadding(),
        elevation = 0.dp
    )
}

@Composable
fun PlayerMusicServicesRow(
    musicServices: MusicServicesData,
    modifier: Modifier = Modifier,
    musicServiceSelected: (url: String) -> Unit
) {
    if (musicServices.hasSomething) {
        Row(
            modifier
                .animateContentSize()
                .padding(20.dp)
        ) {
            if (!musicServices.youtubeMusic.isNullOrEmpty()) {
                Image(
                    painter = painterResource(id = R.drawable.ic_youtube_music),
                    contentDescription = "Youtube Music",
                    modifier = Modifier
                        .padding(8.dp)
                        .size(48.dp)
                        .borderlessClickable { musicServiceSelected(musicServices.youtubeMusic) }
                )
            }
            if (!musicServices.youtube.isNullOrEmpty()) {
                Image(
                    painter = painterResource(id = R.drawable.ic_youtube_play),
                    contentDescription = "Youtube",
                    modifier = Modifier
                        .padding(8.dp)
                        .size(48.dp)
                        .borderlessClickable { musicServiceSelected(musicServices.youtube) }
                )
            }
            if (!musicServices.spotify.isNullOrEmpty()) {
                Image(
                    painter = painterResource(id = R.drawable.ic_spotify),
                    contentDescription = "Spotify",
                    modifier = Modifier
                        .padding(8.dp)
                        .size(48.dp)
                        .borderlessClickable { musicServiceSelected(musicServices.spotify) }
                )
            }
            if (!musicServices.iTunes.isNullOrEmpty()) {
                Image(
                    painter = painterResource(id = R.drawable.ic_itunes),
                    contentDescription = "iTunes",
                    modifier = Modifier
                        .padding(8.dp)
                        .size(48.dp)
                        .borderlessClickable { musicServiceSelected(musicServices.iTunes) }
                )
            }
            if (!musicServices.yandexMusic.isNullOrEmpty()) {
                Image(
                    painter = painterResource(id = R.drawable.ic_yandex_music),
                    contentDescription = "Yandex Music",
                    modifier = Modifier
                        .padding(8.dp)
                        .size(48.dp)
                        .borderlessClickable { musicServiceSelected(musicServices.yandexMusic) }
                )
            }
        }
    }
}

@Composable
fun PlayerController(color: Color, isPlaying: Boolean) {
    val context = LocalContext.current

    Row {
        Crossfade(targetState = isPlaying) {
            Image(
                painter = painterResource(
                    id = if (it)
                        R.drawable.ic_player_pause_filled
                    else
                        R.drawable.ic_player_play_filled
                ),
                contentDescription = if (it) "Pause" else "Play",
                colorFilter = ColorFilter.tint(color),
                modifier = Modifier
                    .padding(8.dp)
                    .size(48.dp)
                    .borderlessClickable {
                        if (it)
                            PlayerService.pause(context)
                        else
                            PlayerService.play(context)
                    }
            )
        }
        if (isPlaying) {
            Image(
                painter = painterResource(id = R.drawable.ic_player_stop_filled),
                contentDescription = "Stop",
                colorFilter = ColorFilter.tint(color),
                modifier = Modifier
                    .padding(8.dp)
                    .size(48.dp)
                    .borderlessClickable { PlayerService.stop(context) }
            )
        }
    }
}

@Composable
fun StationsList(
    onStationSelected: (station: Station) -> Unit
) {
    LazyColumn {
        items(Station.values()) { station ->
            StationItem(source = station, onClick = { onStationSelected(station) })
        }
    }
}

@Composable
fun StationItem(source: Station, onClick: () -> Unit) {
    Text(
        text = source.stationName,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    )
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
                Color.White,
                selectStation = {},
                isPlaying = false
            )
        }
    }
}


