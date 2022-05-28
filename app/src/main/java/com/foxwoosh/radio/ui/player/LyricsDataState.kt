package com.foxwoosh.radio.ui.player

sealed class LyricsDataState {
    object Loading : LyricsDataState()
    data class Ready(val lyrics: String) : LyricsDataState()
}