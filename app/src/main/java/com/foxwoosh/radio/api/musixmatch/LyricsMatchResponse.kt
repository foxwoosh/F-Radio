package com.foxwoosh.radio.api.musixmatch

import kotlinx.serialization.Serializable

@Serializable
data class LyricsMatchResponse(
    val message: Message
)