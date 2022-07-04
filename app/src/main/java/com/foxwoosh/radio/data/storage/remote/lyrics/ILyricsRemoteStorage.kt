package com.foxwoosh.radio.data.storage.remote.lyrics

import kotlinx.coroutines.flow.MutableSharedFlow

interface ILyricsRemoteStorage {
    val lyricsFlow: MutableSharedFlow<String>

    suspend fun fetchLyrics(title: String, artist: String)
}