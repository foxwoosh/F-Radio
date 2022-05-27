package com.foxwoosh.radio.api.ultra.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PreviousTrackResponse(
    @SerialName("artist") val artist: String,
    @SerialName("cover") val cover: String,
    @SerialName("cover_webp") val coverWebp: String,
    @SerialName("date") val date: String,
    @SerialName("last_modified") val lastModified: String?,
    @SerialName("time") val time: String,
    @SerialName("title") val title: String,
    @SerialName("uniqueid") val uniqueID: String,
    @SerialName("itunes_url") val itunesUrl: String?,
    @SerialName("spotify_url") val spotifyUrl: String?,
    @SerialName("yamusic_url") val yandexMusicUrl: String?,
    @SerialName("youtube_url") val youtubeUrl: String?,
    @SerialName("ytmusic_url") val youtubeMusicUrl: String?
)