package com.foxwoosh.radio.player.models

enum class Station(
    val stationName: String,
    val url: String,
    val code: Int
) {
    ULTRA(
        "Radio Ultra",
        "https://nashe1.hostingradio.ru:80/ultra-128.mp3",
        0
    ),
    ULTRA_HD(
        "Radio Ultra HD",
        "https://nashe1.hostingradio.ru:80/ultra-192.mp3",
        0
    )
}