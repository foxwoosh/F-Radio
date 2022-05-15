package com.foxwoosh.radio.api.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CurrentTrackResponse(
    @SerialName("album") val album: String?,
    @SerialName("artist") val artist: String,
    @SerialName("cover") val cover: String,
    @SerialName("cover_webp") val coverWebp: String,
    @SerialName("date") val date: String,
    @SerialName("id") val id: String,
    @SerialName("last_modified") val lastModified: String?,
    @SerialName("metadata") val metadata: String,
    @SerialName("prev_tracks") val previousTracks: List<PreviousTrackResponse>,
    @SerialName("root") val root: String,
    @SerialName("time") val time: String,
    @SerialName("title") val title: String,
    @SerialName("uniqueid") val uniqueID: String,
    @SerialName("itunes_url") val itunesUrl: String?,
    @SerialName("spotify_url") val spotifyUrl: String?,
    @SerialName("yamusic_url") val yandexMusicUrl: String?,
    @SerialName("youtube_url") val youtubeUrl: String?,
    @SerialName("ytmusic_url") val youtubeMusicUrl: String?
)