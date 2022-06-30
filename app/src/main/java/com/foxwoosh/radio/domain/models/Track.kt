package com.foxwoosh.radio.domain.models

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String?,
    val coverUrl: String,
    val metadata: String,
    val date: String,
    val time: String,
    val youtubeMusicUrl: String?,
    val youtubeUrl: String?,
    val spotifyUrl: String?,
    val iTunesUrl: String?,
    val yandexMusicUrl: String?
)