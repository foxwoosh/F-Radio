@file:OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)

package com.foxwoosh.radio.ui.player

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import com.foxwoosh.radio.R
import com.foxwoosh.radio.Utils
import com.foxwoosh.radio.player.PlayerService
import com.foxwoosh.radio.player.models.MusicServicesData
import com.foxwoosh.radio.player.models.PlayerColors
import com.foxwoosh.radio.player.models.PlayerState
import com.foxwoosh.radio.player.models.TrackDataState
import com.foxwoosh.radio.storage.models.PreviousTrack
import com.foxwoosh.radio.ui.borderlessClickable
import com.foxwoosh.radio.ui.currentFraction
import com.foxwoosh.radio.ui.singleCondition
import kotlinx.coroutines.launch

private const val colorsChangeDuration = 1_000

@Composable
fun PlayerScreen() {
    val context = LocalContext.current
    val viewModel = hiltViewModel<PlayerViewModel>()

    val trackData by viewModel.trackDataFlow.collectAsState()
    val playerState by viewModel.playerStateFlow.collectAsState()

    val stationSelectorState = rememberBackdropScaffoldState(initialValue = BackdropValue.Concealed)
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed)
    )

    val scope = rememberCoroutineScope()

    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navigationBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomSheetPeekHeight = 80.dp + navigationBarHeight

    val title: String
    val artist: String
    val cover: Bitmap
    val colors: PlayerColors
    val musicServices: MusicServicesData?
    val previousTracks: List<PreviousTrack>?
    val lyrics: String

    when (trackData) {
        TrackDataState.Idle -> {
            title = context.getString(R.string.player_title_idle)
            artist = ""
            cover = context.getDrawable(R.drawable.ic_no_music_playing)!!.toBitmap()
            colors = PlayerColors.default
            musicServices = null
            previousTracks = null
            lyrics = ""
        }
        TrackDataState.Loading -> {
            title = context.getString(R.string.player_title_loading)
            artist = ""
            cover = context.getDrawable(R.drawable.ic_no_music_playing)!!.toBitmap()
            colors = PlayerColors.default
            musicServices = null
            previousTracks = null
            lyrics = ""
        }
        is TrackDataState.Ready -> {
            val data = trackData as TrackDataState.Ready

            title = data.title
            artist = data.artist
            cover = data.cover
            colors = data.colors
            musicServices = data.musicServices
            previousTracks = data.previousTracks
            lyrics = data.lyrics
        }
    }

    val animationSpec: AnimationSpec<Color> = tween(colorsChangeDuration)
    val surfaceColor by animateColorAsState(
        targetValue = colors.surfaceColor,
        animationSpec = animationSpec
    )
    val vibrantSurfaceColor by animateColorAsState(
        targetValue = colors.vibrantSurfaceColor,
        animationSpec = animationSpec
    )
    val primaryTextColor by animateColorAsState(
        targetValue = colors.primaryTextColor,
        animationSpec = animationSpec
    )
    val secondaryTextColor by animateColorAsState(
        targetValue = colors.secondaryTextColor,
        animationSpec = animationSpec
    )

    Surface {
        PlayerBackdropStationSelector(
            surfaceColor = surfaceColor,
            primaryTextColor = primaryTextColor,
            secondaryTextColor = secondaryTextColor,
            state = stationSelectorState,
        ) {
            BottomSheetScaffold(
                scaffoldState = bottomSheetScaffoldState,
                sheetContent = {
                    PlayerBottomSheetContent(
                        offset = bottomSheetScaffoldState.currentFraction,
                        statusBarHeight = statusBarHeight,
                        backgroundColor = surfaceColor,
                        primaryTextColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor,
                        previousTracks = previousTracks ?: emptyList(),
                        lyrics = lyrics,
                        onPageSelected = {
                            if (bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
                                bottomSheetScaffoldState.bottomSheetState.expand()
                            }
                        }
                    )
                },
                sheetShape = RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp
                ),
                backgroundColor = vibrantSurfaceColor,
                sheetPeekHeight = bottomSheetPeekHeight
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(surfaceColor)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_arrow_down),
                        contentDescription = "Select",
                        modifier = Modifier
                            .statusBarsPadding()
                            .borderlessClickable(
                                onClick = { scope.launch { stationSelectorState.reveal() } }
                            )
                            .padding(16.dp),
                        colorFilter = ColorFilter.tint(primaryTextColor)
                    )

                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CoverWithServices(
                            cover = cover,
                            musicServices = musicServices,
                            trackDataReady = trackData is TrackDataState.Ready
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        TrackInfo(
                            title = title,
                            primaryTextColor = primaryTextColor,
                            artist = artist,
                            secondaryTextColor = secondaryTextColor
                        )

                        Spacer(modifier = Modifier.height(48.dp))

                        PlaybackController(
                            color = primaryTextColor,
                            playerState = playerState,
                            selectStation = { scope.launch { stationSelectorState.reveal() } }
                        )

                        Spacer(modifier = Modifier.height(bottomSheetPeekHeight))
                    }
                }
            }
        }
    }
}

@Composable
fun CoverWithServices(
    cover: Bitmap,
    musicServices: MusicServicesData?,
    trackDataReady: Boolean
) {
    var musicServicesMenuOpened by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .aspectRatio(1f)
    ) {
        PlayerMusicServices(
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
            val smallScaledCover = musicServicesMenuOpened && trackDataReady

            Image(
                bitmap = it.asImageBitmap(),
                contentScale = ContentScale.Fit,
                contentDescription = "Cover",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        shadowElevation = animateFloatAsState(
                            if (smallScaledCover) 30f else 0f
                        ).value,
                        scaleX = animateFloatAsState(
                            targetValue = if (smallScaledCover) 0.5f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy
                            )
                        ).value,
                        scaleY = animateFloatAsState(
                            targetValue = if (smallScaledCover) 0.5f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy
                            )
                        ).value
                    )
                    .singleCondition(trackDataReady) {
                        clickable { musicServicesMenuOpened = !musicServicesMenuOpened }
                    }
            )
        }
    }
}

@Composable
fun TrackInfo(title: String, primaryTextColor: Color, artist: String, secondaryTextColor: Color) {
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
}

@Composable
fun PlayerMusicServices(
    musicServices: MusicServicesData?,
    modifier: Modifier = Modifier,
    musicServiceSelected: (url: String) -> Unit
) {
    if (musicServices == null) return

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
fun PlaybackController(
    color: Color,
    playerState: PlayerState,
    selectStation: () -> Unit
) {
    val context = LocalContext.current

    Box(
        Modifier
            .height(72.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = playerState == PlayerState.IDLE,
            enter = scaleIn(),
            exit = scaleOut()
        ) {
            OutlinedButton(onClick = selectStation) {
                Text(text = stringResource(id = R.string.player_select_station_button))
            }
        }

        AnimatedVisibility(
            visible = playerState == PlayerState.PLAYING,
            enter = scaleIn(),
            exit = fadeOut()
        ) {
            PlayerControllerButton(
                picRes = R.drawable.ic_player_pause_filled,
                contentDescription = "Pause",
                color = color,
                onClick = { PlayerService.pause(context) }
            )
        }

        AnimatedVisibility(
            visible = playerState == PlayerState.PAUSED,
            enter = fadeIn(),
            exit = scaleOut()
        ) {
            PlayerControllerButton(
                picRes = R.drawable.ic_player_play_filled,
                contentDescription = "Play",
                color = color,
                onClick = { PlayerService.play(context) }
            )
        }

        AnimatedVisibility(
            visible = playerState == PlayerState.BUFFERING,
            enter = scaleIn(),
            exit = scaleOut()
        ) {
            CircularProgressIndicator(
                color = color,
                modifier = Modifier
                    .size(48.dp)
            )
        }
    }
}

@Composable
fun PlayerControllerButton(
    @DrawableRes picRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(picRes),
        contentDescription = contentDescription,
        colorFilter = ColorFilter.tint(color),
        modifier = modifier
            .padding(8.dp)
            .size(64.dp)
            .borderlessClickable(onClick = onClick)
    )
}