package com.foxwoosh.radio.data.storage.remote.lyrics

import com.foxwoosh.radio.BuildConfig
import com.foxwoosh.radio.data.api.ApiService
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject

class LyricsRemoteStorage @Inject constructor(
    private val apiService: ApiService
) : ILyricsRemoteStorage {
    override val lyricsFlow = MutableSharedFlow<String>()

    override suspend fun fetchLyrics(title: String, artist: String) {
        lyricsFlow.emit(
            apiService
                .musixmatch
                .getLyrics(BuildConfig.MUSIXMATCH_KEY, title, artist)
                .message
                .body
                .lyrics
                .lyrics_body
                .substringBefore("*****")
        )
    }

    private fun fixQuery(query: String) = query
        .replace(Regex("\\(.*?\\)"),"")
        .replace("&", "")
}