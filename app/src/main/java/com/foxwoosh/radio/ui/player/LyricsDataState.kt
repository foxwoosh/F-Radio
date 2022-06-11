package com.foxwoosh.radio.ui.player

sealed class LyricsDataState {
    object NoData : LyricsDataState()
    object Loading : LyricsDataState()
    data class Error(val text: String) : LyricsDataState()
    data class Ready(val lyrics: String) : LyricsDataState()
}