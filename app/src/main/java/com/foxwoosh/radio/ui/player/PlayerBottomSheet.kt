@file:OptIn(
    ExperimentalPagerApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)

package com.foxwoosh.radio.ui.player

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.foxwoosh.radio.R
import com.foxwoosh.radio.domain.models.LyricsReportState
import com.foxwoosh.radio.domain.models.Track
import com.foxwoosh.radio.openURL
import com.foxwoosh.radio.player.models.MusicServicesData
import com.foxwoosh.radio.ui.collectAsEffect
import com.foxwoosh.radio.ui.currentOffset
import com.foxwoosh.radio.ui.player.models.InProgressReportUiState
import com.foxwoosh.radio.ui.player.viewmodels.PlayerBottomSheetViewModel
import com.foxwoosh.radio.ui.theme.*
import com.foxwoosh.radio.ui.top
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch

@Composable
fun PlayerBottomSheetContent(
    state: BottomSheetScaffoldState,
    backgroundColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color
) {
    val viewModel = hiltViewModel<PlayerBottomSheetViewModel>()
    val pagerState = rememberPagerState()
    val offset = state.currentOffset

    val statusBarHeight = WindowInsets.statusBars.top

    LaunchedEffect(pagerState.currentPage, state.bottomSheetState.isExpanded) {
        if (state.bottomSheetState.isExpanded
            && pagerState.currentPage == PlayerBottomSheetPage.LYRICS.ordinal
        ) {
            viewModel.fetchLyricsForCurrentTrack()
        }
    }
    viewModel.events.collectAsEffect {
        if (state.bottomSheetState.isExpanded
            && pagerState.currentPage == PlayerBottomSheetPage.LYRICS.ordinal
        ) {
            viewModel.fetchLyricsForCurrentTrack()
        }
    }

    val previousTracks by viewModel.previousTracksFlow.collectAsState()
    val lyricsState by viewModel.lyricsStateFlow.collectAsState()
    val currentUser by viewModel.userFlow.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(top = statusBarHeight * offset),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_horizontal_bar),
            colorFilter = ColorFilter.tint(primaryTextColor),
            contentDescription = "Bar"
        )

        PlayerBottomSheetTabs(
            pagerState = pagerState,
            primaryTextColor = primaryTextColor,
            offset = offset,
            backgroundColor = backgroundColor,
            state = state,
            secondaryTextColor = secondaryTextColor
        )

        PlayerBottomSheetPager(
            pagerState = pagerState,
            offset = offset,
            previousTracks = previousTracks,
            primaryTextColor = primaryTextColor,
            secondaryTextColor = secondaryTextColor,
            backgroundColor = backgroundColor,
            lyricsState = lyricsState,
            userLoggedIn = currentUser != null
        )
    }
}

@Composable
private fun PlayerBottomSheetTabs(
    pagerState: PagerState,
    primaryTextColor: Color,
    offset: Float,
    backgroundColor: Color,
    state: BottomSheetScaffoldState,
    secondaryTextColor: Color
) {
    val scope = rememberCoroutineScope()

    TabRow(
        selectedTabIndex = pagerState.currentPage,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                modifier = Modifier
                    .pagerTabIndicatorOffset(pagerState, tabPositions),
                color = primaryTextColor.copy(alpha = offset)
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        backgroundColor = backgroundColor
    ) {
        PlayerBottomSheetPage.values().forEach {
            Tab(
                text = {
                    Text(
                        text = stringResource(
                            id = when (it) {
                                PlayerBottomSheetPage.PREVIOUS_TRACKS ->
                                    R.string.player_page_title_previous_tracks
                                PlayerBottomSheetPage.LYRICS ->
                                    R.string.player_page_title_lyrics
                            }
                        ).uppercase()
                    )
                },
                selected = pagerState.currentPage == it.ordinal,
                onClick = {
                    scope.launch {
                        if (state.bottomSheetState.isCollapsed) {
                            state.bottomSheetState.expand()
                        }
                        if (pagerState.currentPage != it.ordinal) {
                            pagerState.animateScrollToPage(it.ordinal)
                        }
                    }
                },
                selectedContentColor = primaryTextColor,
                unselectedContentColor = secondaryTextColor
            )
        }
    }
}

@Composable
private fun PlayerBottomSheetPager(
    pagerState: PagerState,
    offset: Float,
    previousTracks: List<Track>,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    backgroundColor: Color,
    lyricsState: LyricsDataState,
    userLoggedIn: Boolean
) {
    HorizontalPager(
        count = PlayerBottomSheetPage.size,
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .alpha(offset)
    ) { pageIndex ->
        when (pageIndex) {
            PlayerBottomSheetPage.PREVIOUS_TRACKS.ordinal -> if (previousTracks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.player_page_no_data_previous_tracks),
                        color = primaryTextColor
                    )
                }
            } else {
                PreviousTracksList(
                    previousTracks = previousTracks,
                    primaryTextColor = primaryTextColor,
                    secondaryTextColor = secondaryTextColor,
                    modifier = Modifier.fillMaxSize()
                )
            }
            PlayerBottomSheetPage.LYRICS.ordinal -> LyricsPage(
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor,
                backgroundColor = backgroundColor,
                lyricsState = lyricsState,
                userLoggedIn = userLoggedIn
            )
        }
    }
}

@Composable
private fun LyricsPage(
    primaryTextColor: Color,
    secondaryTextColor: Color,
    backgroundColor: Color,
    lyricsState: LyricsDataState,
    userLoggedIn: Boolean
) {
    val viewModel = hiltViewModel<PlayerBottomSheetViewModel>()

    val inProgressReportState by viewModel.inProgressReportState.collectAsState()
    val sendingReportProgress by viewModel.sendingReportProgress.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (lyricsState) {
            is LyricsDataState.NoData -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.player_page_no_data_lyrics),
                        color = primaryTextColor
                    )

                    Spacer(modifier = Modifier.height(dp16))

                    if (userLoggedIn) {
                        LyricsReportSection(
                            secondaryTextColor = secondaryTextColor,
                            backgroundColor = backgroundColor,
                            titleVisible = false
                        )
                    }
                }
            }
            is LyricsDataState.Error -> Text(
                text = stringResource(id = R.string.player_page_error_lyrics),
                color = primaryTextColor
            )
            is LyricsDataState.Loading -> CircularProgressIndicator(color = primaryTextColor)
            is LyricsDataState.Ready -> {
                val scrollState = rememberScrollState()

                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .navigationBarsPadding()
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp
                        )
                ) {
                    SelectionContainer {
                        Text(
                            text = lyricsState.lyrics,
                            color = primaryTextColor,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    if (userLoggedIn) {
                        LyricsReportSection(
                            secondaryTextColor = secondaryTextColor,
                            backgroundColor = backgroundColor,
                            titleVisible = true
                        )
                    }
                }
            }
        }

        inProgressReportState?.let { report ->
            LyricsReportDialog(
                report = report,
                sendingReportProgress = sendingReportProgress,
                dismissReport = { viewModel.dismissReport() },
                sendReport = { viewModel.sendReport() },
                setReportComment = { viewModel.setReportComment(it) }
            )
        } ?: LocalSoftwareKeyboardController.current?.hide()
    }
}

@Composable
private fun LyricsReportSection(
    secondaryTextColor: Color,
    backgroundColor: Color,
    titleVisible: Boolean
) {
    val viewModel = hiltViewModel<PlayerBottomSheetViewModel>()

    val currentReportState by viewModel.currentLyricsReportState.collectAsState()

    Spacer(modifier = Modifier.height(dp16))

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        currentReportState.state?.let {
            Icon(
                imageVector = when (it) {
                    LyricsReportState.SUBMITTED -> Icons.Filled.Done
                    LyricsReportState.SOLVED -> Icons.Filled.DoneAll
                    LyricsReportState.DECLINED -> Icons.Filled.Close
                },
                contentDescription = "Report state",
                tint = secondaryTextColor
            )
            Text(
                text = stringResource(
                    when (it) {
                        LyricsReportState.SUBMITTED -> R.string.lyrics_report_state_submitted_title
                        LyricsReportState.SOLVED -> R.string.lyrics_report_state_solved_title
                        LyricsReportState.DECLINED -> R.string.lyrics_report_state_declined_title
                    }
                ),
                color = secondaryTextColor,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        } ?: run {
            if (titleVisible) {
                Text(
                    text = stringResource(R.string.lyrics_report_section_title),
                    color = secondaryTextColor,
                    fontWeight = FontWeight.Medium
                )
            }
            Button(
                onClick = { viewModel.initReport() },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = secondaryTextColor,
                    contentColor = backgroundColor
                )
            ) {
                Text(text = stringResource(R.string.lyrics_report_section_button))
            }
        }
    }
}

@Composable
private fun LyricsReportDialog(
    report: InProgressReportUiState,
    sendingReportProgress: Boolean,
    dismissReport: () -> Unit,
    sendReport: () -> Unit,
    setReportComment: (String) -> Unit
) {
    Dialog(
        onDismissRequest = dismissReport,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(dp16)
                ) {
                    Text(
                        text = stringResource(R.string.lyrics_report_dialog_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(dp8))
                    Text(
                        text = stringResource(
                            R.string.lyrics_report_dialog_track_title,
                            report.title
                        ),
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = stringResource(
                            R.string.lyrics_report_dialog_track_artist,
                            report.artist
                        ),
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(dp8))
                    Text(
                        text = stringResource(R.string.lyrics_report_dialog_description),
                        color = White_70
                    )
                    Spacer(modifier = Modifier.height(dp8))
                    OutlinedTextField(
                        value = report.comment,
                        label = { Text(text = stringResource(R.string.lyrics_report_dialog_comment_hint)) },
                        enabled = !sendingReportProgress,
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(KeyboardCapitalization.Sentences),
                        onValueChange = { if (it.length <= 200) setReportComment(it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    modifier = Modifier
                        .padding(horizontal = dp8)
                        .align(Alignment.End),
                    horizontalArrangement = Arrangement.spacedBy(dp4),
                ) {
                    if (!sendingReportProgress) {
                        TextButton(
                            onClick = dismissReport
                        ) {
                            Text(
                                text = stringResource(R.string.lyrics_report_dialog_button_negative),
                                color = Color.White
                            )
                        }
                    }

                    TextButton(
                        onClick = sendReport,
                        enabled = !sendingReportProgress
                    ) {
                        if (sendingReportProgress) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(dp16),
                                strokeWidth = dp2
                            )
                        } else {
                            Text(text = stringResource(R.string.lyrics_report_dialog_button_positive))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviousTracksList(
    previousTracks: List<Track>,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var openedServicesHash by rememberSaveable { mutableStateOf(0) }

    LazyColumn(
        modifier = modifier,
        contentPadding = WindowInsets.navigationBars.asPaddingValues()
    ) {
        items(items = previousTracks) { track ->
            val trackHash = track.hashCode()
            Column(
                modifier = Modifier
                    .clickable {
                        openedServicesHash = if (openedServicesHash == trackHash) {
                            0
                        } else {
                            trackHash
                        }
                    }
                    .padding(horizontal = dp16, vertical = 10.dp)
                    .fillMaxWidth()
                    .animateContentSize(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = track.cover,
                        contentDescription = "Cover",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(dp8))
                    )

                    Spacer(modifier = Modifier.width(dp12))

                    Column(
                        Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = track.title,
                            color = primaryTextColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = track.artist,
                            color = secondaryTextColor
                        )
                    }
                }

                if (trackHash == openedServicesHash) {
                    PlayerMusicServices(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        musicServices = MusicServicesData(
                            track.youtubeMusicUrl,
                            track.youtubeUrl,
                            track.spotifyUrl,
                            track.iTunesUrl,
                            track.yandexMusicUrl
                        ),
                        musicServiceSelected = { url ->
                            context.openURL(url)
                            openedServicesHash = 0
                        }
                    )
                }
            }
        }
    }
}

enum class PlayerBottomSheetPage {
    PREVIOUS_TRACKS, LYRICS;

    companion object {
        operator fun get(index: Int) = values()[index]
        val size: Int
            get() = values().size
    }
}