package com.foxwoosh.radio.data.api.musixmatch

import kotlinx.serialization.Serializable

@Serializable
data class LyricsMatchResponse(
    val message: Message
)