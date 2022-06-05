package com.foxwoosh.radio.ui.player

sealed class LyricsDataState {
    object NoData : LyricsDataState()
    object Loading : LyricsDataState()
    object Error : LyricsDataState()
    data class Ready(val lyrics: String) : LyricsDataState()
}