package com.foxwoosh.radio.player

data class MusicServicesData(
    val youtubeMusic: String? = null,
    val youtube: String? = null,
    val spotify: String? = null,
    val iTunes: String? = null,
    val yandexMusic: String? = null
) {
    val hasSomething: Boolean
        get() = youtubeMusic.isNullOrEmpty().not()
                || youtube.isNullOrEmpty().not()
                || spotify.isNullOrEmpty().not()
                || iTunes.isNullOrEmpty().not()
                || yandexMusic.isNullOrEmpty().not()

    companion object {
        val empty = MusicServicesData()
    }
}