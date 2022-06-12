package com.foxwoosh.radio.data.api.musixmatch

import kotlinx.serialization.Serializable

@Serializable
data class Body(
    val lyrics: Lyrics
)