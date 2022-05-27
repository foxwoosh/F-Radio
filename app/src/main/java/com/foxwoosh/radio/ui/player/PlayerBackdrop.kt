@file:OptIn(ExperimentalMaterialApi::class)

package com.foxwoosh.radio.ui.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.foxwoosh.radio.R
import com.foxwoosh.radio.player.PlayerService
import com.foxwoosh.radio.player.models.Station
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun StationsList(
    surfaceColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    onStationSelected: (station: Station) -> Unit
) {
    LazyColumn {
        items(Station.values()) { station ->
            Text(
                text = station.stationName,
                color = primaryTextColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onStationSelected(station)
                    }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
fun PlayerBackdropStationSelector(
    surfaceColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    state: BackdropScaffoldState,
    frontLayerContent: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    BackdropScaffold(
        appBar = {
            TopAppBar(
                modifier = Modifier
                    .statusBarsPadding(),
                elevation = 0.dp,
                title = {
                    Text(
                        text = stringResource(id = R.string.station_selector_title),
                        color = primaryTextColor
                    )
                },
                backgroundColor = Color.Transparent
            )
        },
        backLayerBackgroundColor = surfaceColor,
        peekHeight = 0.dp,
        backLayerContent = {
            StationsList(
                surfaceColor,
                primaryTextColor,
                secondaryTextColor
            ) {
                scope.launch {
                    state.conceal()
                    delay(50)

                    PlayerService.selectSource(context = context, station = it)
                }
            }
        },
        scaffoldState = state,
        frontLayerContent = frontLayerContent
    )
}