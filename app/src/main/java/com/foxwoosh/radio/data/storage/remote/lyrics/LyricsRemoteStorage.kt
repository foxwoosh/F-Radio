package com.foxwoosh.radio.data.storage.remote.lyrics

import com.foxwoosh.radio.BuildConfig
import com.foxwoosh.radio.data.api.ApiService
import com.foxwoosh.radio.data.api.foxy.responses.LyricsResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject

class LyricsRemoteStorage @Inject constructor(
    private val apiService: ApiService
) : ILyricsRemoteStorage {
    override val lyricsFlow = MutableSharedFlow<String>()

    override suspend fun fetchLyrics(title: String, artist: String) {
        lyricsFlow.emit(
            apiService
                .foxy
                .getLyrics(artist, title)
                .lyrics
        )
    }

    private suspend fun musixmatch(title: String, artist: String) = apiService
        .musixmatch
        .getLyrics(BuildConfig.MUSIXMATCH_KEY, title, artist)
        .message
        .body
        .lyrics
        .lyrics_body
        .substringBefore("*****")
}