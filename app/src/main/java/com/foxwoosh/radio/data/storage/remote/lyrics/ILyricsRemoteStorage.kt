package com.foxwoosh.radio.data.storage.remote.lyrics

import com.foxwoosh.radio.domain.models.Lyrics
import com.foxwoosh.radio.domain.models.LyricsReport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface ILyricsRemoteStorage {
    val lyrics: SharedFlow<Lyrics>
    val lyricsReportUpdate: Flow<LyricsReport>

    suspend fun fetchLyrics(title: String, artist: String)

    suspend fun reportLyrics(lyricsID: Int, comment: String)

    suspend fun getUserReports(): List<LyricsReport>
}