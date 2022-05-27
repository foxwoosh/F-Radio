package com.foxwoosh.radio.api.musixmatch

import kotlinx.serialization.Serializable

@Serializable
data class Body(
    val lyrics: Lyrics
)