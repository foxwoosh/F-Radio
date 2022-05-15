package com.foxwoosh.radio.storage.models

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String?,
    val imageUrl: String
)