@file:OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalUnitApi::class
)

package com.foxwoosh.radio.ui.player

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import com.foxwoosh.radio.R
import com.foxwoosh.radio.copyToClipboard
import com.foxwoosh.radio.openURL
import com.foxwoosh.radio.player.PlayerService
import com.foxwoosh.radio.player.models.MusicServicesData
import com.foxwoosh.radio.player.models.PlayerColors
import com.foxwoosh.radio.player.models.PlayerState
import com.foxwoosh.radio.player.models.TrackDataState
import com.foxwoosh.radio.storage.models.PreviousTrack
import com.foxwoosh.radio.ui.borderlessClickable
import com.foxwoosh.radio.ui.singleCondition
import kotlinx.coroutines.launch

private const val colorsChangeDuration = 1_000

@Composable
fun PlayerScreen() {
    val context = LocalContext.current
    val viewModel = hiltViewModel<PlayerViewModel>()

    val trackData by viewModel.trackDataFlow.collectAsState()
    val playerState by viewModel.playerStateFlow.collectAsState()
    val lyricsState by viewModel.lyricsStateFlow.collectAsState()

    val backdropStationSelectorState =
        rememberBackdropScaffoldState(initialValue = BackdropValue.Concealed)
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()

    val scope = rememberCoroutineScope()

    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navigationBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomSheetPeekHeight = 80.dp + navigationBarHeight

    val title: String
    val artist: String
    val cover: Bitmap?
    val colors: PlayerColors
    val musicServices: MusicServicesData?
    val previousTracks: List<PreviousTrack>?

    when (trackData) {
        TrackDataState.Idle -> {
            title = context.getString(R.string.player_title_idle)
            artist = ""
            cover = null
            colors = PlayerColors.default
            musicServices = null
            previousTracks = null
        }
        TrackDataState.Loading -> {
            title = context.getString(R.string.player_title_loading)
            artist = ""
            cover = null
            colors = PlayerColors.default
            musicServices = null
            previousTracks = null
        }
        is TrackDataState.Ready -> {
            val data = trackData as TrackDataState.Ready

            title = data.title
            artist = data.artist
            cover = data.cover
            colors = data.colors
            musicServices = data.musicServices
            previousTracks = data.previousTracks
        }
    }

    LaunchedEffect(trackData) {
        if (bottomSheetScaffoldState.bottomSheetState.isExpanded
            && trackData is TrackDataState.Ready
        ) {
            viewModel.fetchLyricsForCurrentTrack()
        }
    }

    BackHandler(
        bottomSheetScaffoldState.bottomSheetState.isExpanded
                || backdropStationSelectorState.isRevealed
    ) {
        if (bottomSheetScaffoldState.bottomSheetState.isExpanded) {
            scope.launch { bottomSheetScaffoldState.bottomSheetState.collapse() }
        }
        if (backdropStationSelectorState.isRevealed) {
            scope.launch { backdropStationSelectorState.conceal() }
        }
    }

    val animationSpec: AnimationSpec<Color> = tween(colorsChangeDuration)
    val surfaceColor by animateColorAsState(
        targetValue = colors.surfaceColor,
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
            state = backdropStationSelectorState
        ) {
            BottomSheetScaffold(
                scaffoldState = bottomSheetScaffoldState,
                sheetContent = {
                    PlayerBottomSheetContent(
                        state = bottomSheetScaffoldState,
                        statusBarHeight = statusBarHeight,
                        backgroundColor = surfaceColor,
                        primaryTextColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor,
                        previousTracks = previousTracks ?: emptyList(),
                        lyricsState = lyricsState,
                        onPageBecomesVisible = { page ->
                            when (page) {
                                PlayerBottomSheetPage.LYRICS ->
                                    viewModel.fetchLyricsForCurrentTrack()
                            }
                        }
                    )
                },
                sheetShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                sheetPeekHeight = bottomSheetPeekHeight
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(surfaceColor)
                        .padding(bottom = bottomSheetPeekHeight)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_arrow_down),
                        contentDescription = "Select",
                        modifier = Modifier
                            .statusBarsPadding()
                            .borderlessClickable(
                                onClick = { scope.launch { backdropStationSelectorState.reveal() } }
                            )
                            .padding(16.dp),
                        colorFilter = ColorFilter.tint(primaryTextColor)
                    )

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CoverWithServices(
                            cover = cover,
                            musicServices = musicServices,
                            trackDataReady = trackData is TrackDataState.Ready,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.6f)
                        )

                        TrackInfo(
                            title = title,
                            primaryTextColor = primaryTextColor,
                            artist = artist,
                            secondaryTextColor = secondaryTextColor,
                            modifier = Modifier
                                .fillMaxWidth()
                        )

                        PlaybackController(
                            color = primaryTextColor,
                            playerState = playerState,
                            selectStation = { scope.launch { backdropStationSelectorState.reveal() } },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.15f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CoverWithServices(
    cover: Bitmap?,
    musicServices: MusicServicesData?,
    trackDataReady: Boolean,
    modifier: Modifier = Modifier
) {
    var musicServicesMenuOpened by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(modifier) {
        PlayerMusicServices(
            musicServices = musicServices,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .scale(animateFloatAsState(if (musicServicesMenuOpened) 1f else 0f).value),
            musicServiceSelected = { url ->
                context.openURL(url)
                musicServicesMenuOpened = false
            }
        )

        val smallScaledCover = musicServicesMenuOpened
                && trackDataReady
                && musicServices?.hasSomething == true

        val scale by animateFloatAsState(
            targetValue = if (smallScaledCover) 0.5f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy
            )
        )

        val crossfadeLoader = ImageLoader.Builder(context)
            .crossfade(durationMillis = colorsChangeDuration)
            .build()
        AsyncImage(
            model = cover,
            imageLoader = crossfadeLoader,
            contentDescription = "Cover",
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .aspectRatio(1f)
                .graphicsLayer(
                    shadowElevation = animateFloatAsState(
                        if (smallScaledCover) 30f else 0f
                    ).value,
                    scaleX = scale,
                    scaleY = scale
                )
                .singleCondition(trackDataReady) {
                    clickable { musicServicesMenuOpened = !musicServicesMenuOpened }
                }
        )
    }
}

@Composable
fun TrackInfo(
    title: String,
    primaryTextColor: Color,
    artist: String, secondaryTextColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            color = primaryTextColor,
            fontSize = TextUnit(24f, TextUnitType.Sp),
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = artist,
            color = secondaryTextColor,
            fontSize = TextUnit(14f, TextUnitType.Sp),
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun PlayerMusicServices(
    musicServices: MusicServicesData?,
    modifier: Modifier = Modifier,
    musicServiceSelected: (url: String) -> Unit
) {
    if (musicServices == null) return

    if (musicServices.hasSomething) {
        val context = LocalContext.current

        Row(
            modifier = modifier.animateContentSize(),
            horizontalArrangement = Arrangement.Center
        ) {
            if (!musicServices.youtubeMusic.isNullOrEmpty()) {
                Image(
                    painter = painterResource(id = R.drawable.ic_youtube_music),
                    contentDescription = "Youtube Music",
                    modifier = Modifier
                        .padding(8.dp)
                        .size(48.dp)
                        .borderlessClickable(
                            onClick = { musicServiceSelected(musicServices.youtubeMusic) },
                            onLongClick = { context.copyToClipboard(musicServices.youtubeMusic) }
                        )
                )
            }
            if (!musicServices.youtube.isNullOrEmpty()) {
                Image(
                    painter = painterResource(id = R.drawable.ic_youtube_play),
                    contentDescription = "Youtube",
                    modifier = Modifier
                        .padding(8.dp)
                        .size(48.dp)
                        .borderlessClickable(
                            onClick = { musicServiceSelected(musicServices.youtube) },
                            onLongClick = { context.copyToClipboard(musicServices.youtube) }
                        )
                )
            }
            if (!musicServices.spotify.isNullOrEmpty()) {
                Image(
                    painter = painterResource(id = R.drawable.ic_spotify),
                    contentDescription = "Spotify",
                    modifier = Modifier
                        .padding(8.dp)
                        .size(48.dp)
                        .borderlessClickable(
                            onClick = { musicServiceSelected(musicServices.spotify) },
                            onLongClick = { context.copyToClipboard(musicServices.spotify) }
                        )
                )
            }
            if (!musicServices.iTunes.isNullOrEmpty()) {
                Image(
                    painter = painterResource(id = R.drawable.ic_itunes),
                    contentDescription = "iTunes",
                    modifier = Modifier
                        .padding(8.dp)
                        .size(48.dp)
                        .borderlessClickable(
                            onClick = { musicServiceSelected(musicServices.iTunes) },
                            onLongClick = { context.copyToClipboard(musicServices.iTunes) }
                        )
                )
            }
            if (!musicServices.yandexMusic.isNullOrEmpty()) {
                Image(
                    painter = painterResource(id = R.drawable.ic_yandex_music),
                    contentDescription = "Yandex Music",
                    modifier = Modifier
                        .padding(8.dp)
                        .size(48.dp)
                        .borderlessClickable(
                            onClick = { musicServiceSelected(musicServices.yandexMusic) },
                            onLongClick = { context.copyToClipboard(musicServices.yandexMusic) }
                        )
                )
            }
        }
    }
}

@Composable
fun PlaybackController(
    color: Color,
    playerState: PlayerState,
    selectStation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .height(72.dp),
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