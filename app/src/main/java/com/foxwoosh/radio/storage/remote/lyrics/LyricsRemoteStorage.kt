package com.foxwoosh.radio.storage.remote.lyrics

import com.foxwoosh.radio.BuildConfig
import com.foxwoosh.radio.api.ApiService
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
                .getLyrics(
                    BuildConfig.FOXY_KEY,
                    "abs",
                    fixQuery("$artist $title")
                )
                .lyrics
        )
    }

    private fun fixQuery(query: String) = query
        .replace(Regex("\\(.*?\\)"),"")
        .replace("&", "")
}