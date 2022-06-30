@file:OptIn(ExperimentalPagerApi::class, ExperimentalMaterialApi::class)

package com.foxwoosh.radio.ui.player

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.foxwoosh.radio.R
import com.foxwoosh.radio.domain.models.PreviousTrack
import com.foxwoosh.radio.domain.models.Track
import com.foxwoosh.radio.openURL
import com.foxwoosh.radio.player.models.MusicServicesData
import com.foxwoosh.radio.ui.currentOffset
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@Composable
fun PlayerBottomSheetContent(
    state: BottomSheetScaffoldState,
    statusBarHeight: Dp,
//    navigationBarHeight: Dp,
    backgroundColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    onPageBecomesVisible: (page: PlayerBottomSheetPage) -> Unit,
    previousTracks: List<Track>,
    lyricsState: LyricsDataState
) {
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()
    val offset = state.currentOffset

    LaunchedEffect(pagerState.currentPage) {
        if (state.bottomSheetState.isExpanded) {
            onPageBecomesVisible(
                PlayerBottomSheetPage[pagerState.currentPage]
            )
        }
    }

    LaunchedEffect(state.bottomSheetState.isExpanded) {
        if (state.bottomSheetState.isExpanded) {
            onPageBecomesVisible(
                PlayerBottomSheetPage[pagerState.currentPage]
            )
        }
    }

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
                    lyricsState = lyricsState
                )
            }
        }
    }
}

@Composable
fun LyricsPage(primaryTextColor: Color, lyricsState: LyricsDataState) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (lyricsState) {
            is LyricsDataState.NoData,
            is LyricsDataState.Error -> Text(
                text = stringResource(id = R.string.player_page_no_data_lyrics),
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
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    Column(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalAlignment = Alignment.CenterHorizontally
//                    ) {
//                        Text(
//                            text = "Something wrong with lyrics?",
//                            color = secondaryTextColor,
//                            fontWeight = FontWeight.Medium
//                        )
//                        Button(
//                            onClick = { /*TODO*/ },
//                            colors = ButtonDefaults.buttonColors(
//                                backgroundColor = secondaryTextColor,
//                                contentColor = backgroundColor
//                            )
//                        ) {
//                            Text(text = "Click here to report!")
//                        }
//                    }
                }
            }
        }
    }
}

@Composable
fun PreviousTracksList(
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
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .fillMaxWidth()
                    .animateContentSize(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = track.coverUrl,
                        contentDescription = "Cover",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )

                    Spacer(modifier = Modifier.width(12.dp))

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