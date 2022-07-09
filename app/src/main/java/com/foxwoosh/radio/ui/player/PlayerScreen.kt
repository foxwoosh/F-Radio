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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.foxwoosh.radio.MainActivity
import com.foxwoosh.radio.R
import com.foxwoosh.radio.copyToClipboard
import com.foxwoosh.radio.data.websocket.SocketState
import com.foxwoosh.radio.openURL
import com.foxwoosh.radio.player.models.*
import com.foxwoosh.radio.ui.*
import com.foxwoosh.radio.ui.player.viewmodels.PlayerViewModel
import com.foxwoosh.radio.ui.widgets.DoubleSelector
import com.foxwoosh.radio.utils.crossfadeImageLoader
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import kotlin.math.absoluteValue

private const val trackChangeDuration = 1_000
private val bottomSheetShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)

@Composable
fun PlayerScreen(
    navigateToSettings: () -> Unit
) {
    val config = LocalConfiguration.current
    val density = LocalDensity.current
    val context = LocalContext.current

    val viewModel = hiltViewModel<PlayerViewModel>()

    val trackData by viewModel.trackDataFlow.collectAsState()
    val playerState by viewModel.playerStateFlow.collectAsState()
    val station by viewModel.stationFlow.collectAsState()
    val socketState by viewModel.socketState.collectAsState()

    var musicServicesMenuOpened by remember { mutableStateOf(false) }

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()

    val scope = rememberCoroutineScope()
    val defaultBottomSheetPeekHeight = 80.dp + WindowInsets.navigationBars.bottom

    val colorAnimationSpec: AnimationSpec<Color> = tween(trackChangeDuration)
    val gradientOffsetAnimationSpec: AnimationSpec<Float> = tween(trackChangeDuration)

    val title: String
    var artist = ""
    var cover: Bitmap? = null
    var colors = PlayerColors.default
    var musicServices: MusicServicesData? = null
    var actualBottomSheetPeekHeight: Dp = 0.dp

    var gradientOffsetValueX by remember { mutableStateOf(0f) }
    var gradientOffsetValueY by remember { mutableStateOf(0f) }

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
                id = if ((trackData as TrackDataState.Error).t is UnknownHostException) {
                    R.string.player_title_error_no_internet
                } else {
                    R.string.player_title_error_default
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

            val gradientKey = data.title + data.artist + data.album
            gradientOffsetValueX = gradientKey.hashCode().absoluteValue %
                with(density) { config.screenWidthDp.dp.toPx() }
            gradientOffsetValueY = gradientKey.hashCode().absoluteValue %
                with(density) { config.screenHeightDp.dp.toPx() }

            actualBottomSheetPeekHeight = defaultBottomSheetPeekHeight
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

    val gradientOffsetX by animateFloatAsState(
        targetValue = gradientOffsetValueX,
        animationSpec = gradientOffsetAnimationSpec
    )
    val gradientOffsetY by animateFloatAsState(
        targetValue = gradientOffsetValueY,
        animationSpec = gradientOffsetAnimationSpec
    )

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetContent = {
            PlayerBottomSheetContent(
                state = bottomSheetScaffoldState,
                backgroundColor = surfaceColor,
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor
            )
        },
        sheetShape = bottomSheetShape,
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
                .padding(bottom = defaultBottomSheetPeekHeight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    SyncIcon(socketState = socketState)

                    StationSelector(
                        modifier = Modifier
                            .align(Alignment.Center),
                        onSelectStationAction = { viewModel.selectStation(context, it) },
                        selectedStation = station
                    )

                    IconButton(
                        onClick = { navigateToSettings() },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }

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
                        .padding(horizontal = 12.dp)
                )

                PlaybackController(
                    color = primaryTextColor,
                    playerState = playerState,
                    onPlay = { viewModel.play(context) },
                    onPause = { viewModel.pause(context) },
                    onStop = { viewModel.stop(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.15f)
                )
            }
        }
    }

    LaunchedEffect(trackData) {
        if (MainActivity.waitForInitialDrawing) {
            MainActivity.waitForInitialDrawing = false
        }
    }
}

@Composable
private fun SyncIcon(socketState: SocketState) {
    AnimatedVisibility(
        visible = socketState !is SocketState.Connected
    ) {
        val infiniteTransition = rememberInfiniteTransition()
        val changingAlpha by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        Icon(
            imageVector = Icons.Filled.SyncProblem,
            contentDescription = "Sync",
            tint = when (socketState) {
                is SocketState.Connecting -> Color.Yellow
                is SocketState.Failure,
                is SocketState.Disconnected -> Color.Red
                else -> Color.White
            },
            modifier = Modifier.graphicsLayer {
                alpha = changingAlpha
            }
        )
    }
}

@Composable
fun StationSelector(
    selectedStation: Station?,
    onSelectStationAction: (Station) -> Unit,
    modifier: Modifier = Modifier,
) {
    DoubleSelector(
        modifier = modifier,
        selectedIndex = selectedStation?.ordinal ?: -1,
        firstItemText = Station.ULTRA.stationName,
        secondItemText = Station.ULTRA_HD.stationName,
        onSelectAction = { onSelectStationAction(Station.values()[it]) }
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
    Box(modifier) {
        val servicesScale by animateFloatAsState(if (servicesOpened) 1f else 0f)
        var coverSize by remember { mutableStateOf(IntSize(0, 0)) }

        PlayerMusicServices(
            musicServices = musicServices,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .graphicsLayer {
                    scaleX = servicesScale
                    scaleY = servicesScale
                    translationY = (coverSize.height / 2f) * servicesScale
                },
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

        AsyncImage(
            model = cover,
            imageLoader = LocalContext.current.crossfadeImageLoader,
            contentDescription = "Cover",
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .aspectRatio(1f)
                .onGloballyPositioned {
                    if (coverSize != it.size) {
                        coverSize = it.size
                    }
                }
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
                        .size(42.dp)
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
                        .size(42.dp)
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
                        .size(42.dp)
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
                        .size(42.dp)
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
                        .size(42.dp)
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
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                    onClick = onPause
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
                    onClick = onPlay
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
                    .borderlessClickable(onClick = onStop)
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