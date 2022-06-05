package com.foxwoosh.radio.storage.remote.lyrics

import kotlinx.coroutines.flow.SharedFlow

interface ILyricsRemoteStorage {
    val lyricsFlow: SharedFlow<String>

    suspend fun fetchLyrics(title: String, artist: String)
}