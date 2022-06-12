package com.foxwoosh.radio.data.storage.models

data class PreviousTrack(
    val title: String,
    val artist: String,
    val coverUrl: String,
    val youtubeMusicUrl: String?,
    val youtubeUrl: String?,
    val spotifyUrl: String?,
    val iTunesUrl: String?,
    val yandexMusicUrl: String?
)