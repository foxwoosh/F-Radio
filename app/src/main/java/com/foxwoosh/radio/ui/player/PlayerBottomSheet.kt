@file:OptIn(ExperimentalPagerApi::class)

package com.foxwoosh.radio.ui.player

import com.foxwoosh.radio.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.foxwoosh.radio.ui.theme.BlackOverlay
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@Composable
fun PlayerBottomSheetContent(
    offset: Float,
    backgroundColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color
) {
    val pagerState = rememberPagerState()
    val pages = listOf("Previous", "Lyrics")
    val scope = rememberCoroutineScope()

    Column(
        Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .background(BlackOverlay)
            .padding(
                top = WindowInsets.statusBars
                    .asPaddingValues()
                    .calculateTopPadding() * offset
            ),
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
                    onClick = { scope.launch { pagerState.scrollToPage(index) } },
                    selectedContentColor = primaryTextColor,
                    unselectedContentColor = secondaryTextColor
                )
            }
        }

        HorizontalPager(
            count = pages.size,
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            when (pageIndex) {
                0 -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Previous tracks")
                }
                1 -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Lyrics")
                }
            }
        }
    }
}