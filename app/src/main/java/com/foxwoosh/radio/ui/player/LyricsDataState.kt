package com.foxwoosh.radio.ui.player

sealed class LyricsDataState {
    data class Loading(val request: Boolean) : LyricsDataState()
    data class Error(val text: String) : LyricsDataState()

    data class NoData(val id: Int) : LyricsDataState()

    data class Ready(
        val id: Int,
        val lyrics: String
    ) : LyricsDataState()
}