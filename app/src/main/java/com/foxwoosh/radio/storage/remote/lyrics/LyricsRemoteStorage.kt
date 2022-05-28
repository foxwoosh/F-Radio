package com.foxwoosh.radio.storage.remote.lyrics

import com.foxwoosh.radio.BuildConfig
import com.foxwoosh.radio.api.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class LyricsRemoteStorage @Inject constructor(
    private val apiService: ApiService
) : ILyricsRemoteStorage {
    override val lyricsFlow = MutableStateFlow<String>("")

    override suspend fun fetchLyrics(title: String, artist: String) {
        lyricsFlow.emit(
            try {
                apiService
                    .musixmatch
                    .getLyrics(BuildConfig.MUSIXMATCH_KEY, title, artist)
                    .message
                    .body
                    .lyrics
                    .lyrics_body
            } catch (e: Exception) {
                ""
            }
        )
    }
}