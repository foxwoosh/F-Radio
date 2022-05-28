package com.foxwoosh.radio.storage.remote.lyrics

import kotlinx.coroutines.flow.StateFlow

interface ILyricsRemoteStorage {
    val lyricsFlow: StateFlow<String>

    suspend fun fetchLyrics(title: String, artist: String)
}