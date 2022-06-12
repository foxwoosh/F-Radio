package com.foxwoosh.radio.data.websocket.messages

import com.foxwoosh.radio.data.websocket.UltraWebSocketResponseType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class UltraSongDataWebSocketMessage(
    @SerialName("type") override val type: UltraWebSocketResponseType,
    @SerialName("id") val id: String,
    @SerialName("album") val album: String? = null,
    @SerialName("artist") val artist: String,
    @SerialName("cover") val cover: String,
    @SerialName("root") val root: String,
    @SerialName("title") val title: String,
    @SerialName("itunes_url") val iTunesUrl: String?,
    @SerialName("spotify_url") val spotifyUrl: String?,
    @SerialName("yamusic_url") val yandexMusicUrl: String?,
    @SerialName("youtube_url") val youtubeUrl: String?,
    @SerialName("ytmusic_url") val youtubeMusicUrl: String?,
    @SerialName("prev_tracks") val previousTracks: List<PreviousTrack>
) : UltraWebSocketMessage {

    @kotlinx.serialization.Serializable
    data class PreviousTrack(
        @SerialName("artist") val artist: String,
        @SerialName("cover") val cover: String,
        @SerialName("title") val title: String,
        @SerialName("itunes_url") val itunesUrl: String?,
        @SerialName("spotify_url") val spotifyUrl: String?,
        @SerialName("yamusic_url") val yandexMusicUrl: String?,
        @SerialName("youtube_url") val youtubeUrl: String?,
        @SerialName("ytmusic_url") val youtubeMusicUrl: String?
    )
}