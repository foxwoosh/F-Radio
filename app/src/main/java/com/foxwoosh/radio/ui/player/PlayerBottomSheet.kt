@file:OptIn(ExperimentalPagerApi::class, ExperimentalMaterialApi::class)

package com.foxwoosh.radio.ui.player

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.foxwoosh.radio.R
import com.foxwoosh.radio.storage.models.PreviousTrack
import com.foxwoosh.radio.ui.currentFraction
import com.foxwoosh.radio.ui.theme.BlackOverlay
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@Composable
fun PlayerBottomSheetContent(
    state: BottomSheetScaffoldState,
    statusBarHeight: Dp,
    backgroundColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    onPageBecomesVisible: (page: PlayerBottomSheetPage) -> Unit,
    onTabClicked: suspend (page: PlayerBottomSheetPage) -> Unit,
    previousTracks: List<PreviousTrack>,
    lyricsState: LyricsDataState
) {
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()
    val offset = state.currentFraction

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
            .background(BlackOverlay)
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
                    color = primaryTextColor
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            backgroundColor = Color.Transparent
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
                            )
                        )
                    },
                    selected = pagerState.currentPage == it.ordinal,
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(it.ordinal) }
                        scope.launch { onTabClicked(it) }
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
                            text = stringResource(id = R.string.previous_tracks_no_data),
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
                PlayerBottomSheetPage.LYRICS.ordinal -> {
                    when (lyricsState) {
                        is LyricsDataState.Loading -> Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = primaryTextColor
                            )
                        }
                        is LyricsDataState.Ready -> {
                            val scrollState = rememberScrollState()
                            Text(
                                text = lyricsState.lyrics,
                                color = primaryTextColor,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState)
                                    .padding(
                                        start = 16.dp,
                                        end = 16.dp,
                                        top = 16.dp,
                                        bottom = 0.dp
                                    )
                                    .navigationBarsPadding()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PreviousTracksList(
    previousTracks: List<PreviousTrack>,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = WindowInsets.navigationBars.asPaddingValues()
    ) {
        items(previousTracks + previousTracks) { track ->
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
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