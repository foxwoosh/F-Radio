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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.view.WindowInsetsCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import com.foxwoosh.radio.Insets
import com.foxwoosh.radio.R
import com.foxwoosh.radio.copyToClipboard
import com.foxwoosh.radio.data.websocket.SocketError
import com.foxwoosh.radio.openURL
import com.foxwoosh.radio.player.PlayerService
import com.foxwoosh.radio.player.models.*
import com.foxwoosh.radio.ui.borderlessClickable
import com.foxwoosh.radio.ui.singleCondition
import com.foxwoosh.radio.ui.theme.BlackOverlay_20
import com.foxwoosh.radio.ui.theme.WhiteOverlay_20
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

private const val trackChangeDuration = 1_000

@Composable
fun PlayerScreen() {
    val config = LocalConfiguration.current
    val density = LocalDensity.current
    val context = LocalContext.current

    val viewModel = hiltViewModel<PlayerViewModel>()

    val trackData by viewModel.trackDataFlow.collectAsState()
    val playerState by viewModel.playerStateFlow.collectAsState()
    val previousTracks by viewModel.previousTracksFlow.collectAsState()
    val lyricsState by viewModel.lyricsStateFlow.collectAsState()
    val station by viewModel.stationFlow.collectAsState()
    val insets by Insets.collectAsState()

    var musicServicesMenuOpened by remember { mutableStateOf(false) }

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()

    val scope = rememberCoroutineScope()

    val statusBarHeight = with(density) {
        insets.getInsets(WindowInsetsCompat.Type.statusBars()).top.toDp()
    }
    val navigationBarHeight = with(density) {
        insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom.toDp()
    }
    val defaultBottomSheetPeekHeight = 80.dp + navigationBarHeight

    val colorAnimationSpec: AnimationSpec<Color> = tween(trackChangeDuration)
    val gradientOffsetAnimationSpec: AnimationSpec<Float> = tween(trackChangeDuration)

    val title: String
    var artist = ""
    var cover: Bitmap? = null
    var colors = PlayerColors.default
    var musicServices: MusicServicesData? = null
    var actualBottomSheetPeekHeight: Dp = 0.dp

    var gradientOffsetX = 0f
    var gradientOffsetY = 0f

    when (trackData) {
        TrackDataState.Idle -> {
            title = stringResource(R.string.player_title_idle)

            musicServicesMenuOpened = false
        }
        TrackDataState.Loading -> {
            title = stringResource(R.string.player_title_loading)
        }
        is TrackDataState.Error -> {
            title = stringResource(
                id = when ((trackData as TrackDataState.Error).error) {
                    SocketError.DEFAULT -> R.string.player_title_error_default
                    SocketError.NO_INTERNET -> R.string.player_title_error_no_internet
                }
            )
        }
        is TrackDataState.Ready -> {
            val data = trackData as TrackDataState.Ready

            title = data.title
            artist = data.artist
            cover = data.cover
            colors = data.colors
            musicServices = data.musicServices

            gradientOffsetX = animateFloatAsState(
                targetValue = data.hashCode().absoluteValue %
                    with(density) { config.screenWidthDp.dp.toPx() },
                animationSpec = gradientOffsetAnimationSpec
            ).value
            gradientOffsetY = animateFloatAsState(
                targetValue = data.hashCode().absoluteValue %
                    with(density) { config.screenHeightDp.dp.toPx() },
                animationSpec = gradientOffsetAnimationSpec
            ).value

            actualBottomSheetPeekHeight = defaultBottomSheetPeekHeight
        }
    }

    LaunchedEffect(trackData) {
        if (bottomSheetScaffoldState.bottomSheetState.isExpanded
            && trackData is TrackDataState.Ready
        ) {
            viewModel.fetchLyricsForCurrentTrack()
        }
    }

    BackHandler(bottomSheetScaffoldState.bottomSheetState.isExpanded) {
        if (bottomSheetScaffoldState.bottomSheetState.isExpanded) {
            scope.launch { bottomSheetScaffoldState.bottomSheetState.collapse() }
        }
    }

    val surfaceColor by animateColorAsState(
        targetValue = colors.surfaceColor,
        animationSpec = colorAnimationSpec
    )
    val vibrantSurfaceColor by animateColorAsState(
        targetValue = colors.vibrantSurfaceColor,
        animationSpec = colorAnimationSpec
    )
    val primaryTextColor by animateColorAsState(
        targetValue = colors.primaryTextColor,
        animationSpec = colorAnimationSpec
    )
    val secondaryTextColor by animateColorAsState(
        targetValue = colors.secondaryTextColor,
        animationSpec = colorAnimationSpec
    )

    Surface {
        BottomSheetScaffold(
            scaffoldState = bottomSheetScaffoldState,
            sheetContent = {
                PlayerBottomSheetContent(
                    state = bottomSheetScaffoldState,
                    statusBarHeight = statusBarHeight,
                    navigationBarHeight = navigationBarHeight,
                    backgroundColor = surfaceColor,
                    primaryTextColor = primaryTextColor,
                    secondaryTextColor = secondaryTextColor,
                    previousTracks = previousTracks,
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
            sheetPeekHeight = actualBottomSheetPeekHeight
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(vibrantSurfaceColor, surfaceColor),
                            center = Offset(gradientOffsetX, gradientOffsetY),
                            tileMode = TileMode.Mirror
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = statusBarHeight,
                            bottom = defaultBottomSheetPeekHeight
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    StationSelector(
                        modifier = Modifier.padding(top = 12.dp),
                        onSelectStationAction = { PlayerService.selectSource(context, it) },
                        selectedStation = station
                    )

                    CoverWithServices(
                        cover = cover,
                        musicServices = musicServices,
                        trackDataReady = trackData is TrackDataState.Ready,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.6f),
                        onServiceSelected = { url ->
                            context.openURL(url)
                            musicServicesMenuOpened = false
                        },
                        onCoverClicked = { musicServicesMenuOpened = !musicServicesMenuOpened },
                        servicesOpened = musicServicesMenuOpened
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.15f)
                    )
                }
            }
        }
    }
}

@Composable
fun StationSelector(
    selectedStation: Station?,
    onSelectStationAction: (Station) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BlackOverlay_20)
    ) {
        StationButton(
            station = Station.ULTRA,
            selected = selectedStation == Station.ULTRA,
            onSelectStationAction = onSelectStationAction
        )
        StationButton(
            station = Station.ULTRA_HD,
            selected = selectedStation == Station.ULTRA_HD,
            onSelectStationAction = onSelectStationAction
        )
    }
}

@Composable
fun StationButton(
    station: Station,
    selected: Boolean,
    onSelectStationAction: (Station) -> Unit
) {
    Text(
        text = station.stationName,
        color = Color.White,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onSelectStationAction(station) }
            .singleCondition(selected) { background(WhiteOverlay_20) }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        fontSize = 14.sp
    )
}

@Composable
fun CoverWithServices(
    cover: Bitmap?,
    musicServices: MusicServicesData?,
    trackDataReady: Boolean,
    modifier: Modifier = Modifier,
    servicesOpened: Boolean,
    onCoverClicked: () -> Unit,
    onServiceSelected: (url: String) -> Unit
) {
    val context = LocalContext.current

    Box(modifier) {
        PlayerMusicServices(
            musicServices = musicServices,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .scale(animateFloatAsState(if (servicesOpened) 1f else 0f).value),
            musicServiceSelected = onServiceSelected
        )

        val smallScaledCover = servicesOpened
            && trackDataReady
            && musicServices?.hasSomething == true

        val scale by animateFloatAsState(
            targetValue = if (smallScaledCover) 0.5f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy
            )
        )

        val crossfadeLoader = ImageLoader.Builder(context)
            .crossfade(durationMillis = trackChangeDuration)
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
                    clickable(onClick = onCoverClicked)
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    ConstraintLayout(
        modifier = modifier
            .height(72.dp)
    ) {
        val (centerBox, stopButton) = createRefs()

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.constrainAs(centerBox) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }
        ) {
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

        AnimatedVisibility(
            visible = playerState == PlayerState.PLAYING || playerState == PlayerState.PAUSED,
            enter = scaleIn(),
            exit = scaleOut(),
            modifier = Modifier
                .constrainAs(stopButton) {
                    start.linkTo(centerBox.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
                .padding(start = 16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_player_stop_filled),
                contentDescription = "Stop",
                colorFilter = ColorFilter.tint(color),
                modifier = Modifier
                    .padding(12.dp)
                    .size(42.dp)
                    .borderlessClickable(onClick = { PlayerService.stop(context) })
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
            .size(72.dp)
            .borderlessClickable(onClick = onClick)
    )
}