@file:OptIn(ExperimentalPagerApi::class)

package com.foxwoosh.radio.ui.player

import com.foxwoosh.radio.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.foxwoosh.radio.storage.models.PreviousTrack
import com.foxwoosh.radio.ui.theme.BlackOverlay
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@Composable
fun PlayerBottomSheetContent(
    offset: Float,
    statusBarHeight: Dp,
    backgroundColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    onPageSelected: suspend () -> Unit,
    previousTracks: List<PreviousTrack>,
    lyrics: String
) {
    val pagerState = rememberPagerState()
    val pages = listOf("Previous", "Lyrics")
    val scope = rememberCoroutineScope()

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
            pages.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.scrollToPage(index)
                            onPageSelected()
                        }
                    },
                    selectedContentColor = primaryTextColor,
                    unselectedContentColor = secondaryTextColor
                )
            }
        }

        HorizontalPager(
            count = pages.size,
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .alpha(offset)
        ) { pageIndex ->
            when (pageIndex) {
                0 -> if (previousTracks.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(id = R.string.previous_tracks_no_data),
                            color = primaryTextColor
                        )
                    }
                } else {
                    PreviousTracksList(
                        previousTracks = previousTracks,
                        textColor = primaryTextColor,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                1 -> Text(
                    text = lyrics,
                    color = primaryTextColor,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun PreviousTracksList(
    previousTracks: List<PreviousTrack>,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(previousTracks) { track ->
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = track.coverUrl,
                    contentDescription = "Cover",
                    modifier = Modifier.size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "${track.title} by ${track.artist}",
                    color = textColor
                )
            }
        }
    }
}