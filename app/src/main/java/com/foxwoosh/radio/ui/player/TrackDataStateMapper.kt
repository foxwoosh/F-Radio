package com.foxwoosh.radio.ui.player

import android.content.Context
import com.foxwoosh.radio.R
import com.foxwoosh.radio.player.models.TrackDataState

fun TrackDataState.getTitle(context: Context) = when (this) {
    TrackDataState.Idle -> context.getString(R.string.player_title_idle)
    TrackDataState.Loading -> context.getString(R.string.player_title_loading)
    is TrackDataState.Ready -> title
}