package com.foxwoosh.radio.ui.player

sealed class PlayerState {
    object Loading : PlayerState()
    data class Done(
        val title: String,
        val artist: String,
        val album: String,
        val image: String
    ) : PlayerState()

}