package com.foxwoosh.radio.ui.player

import com.foxwoosh.radio.domain.models.Track
import com.foxwoosh.radio.player.models.MusicServicesData
import com.foxwoosh.radio.player.models.TrackDataState
import com.foxwoosh.radio.player.models.TrackDetails

fun Track.toReadyState() = TrackDataState.Ready(
    id = id,
    title = title,
    artist = artist,
    album = album,
    cover = cover,
    colors = colors,
    musicServices = MusicServicesData(
        youtubeMusic = youtubeMusicUrl,
        youtube = youtubeUrl,
        spotify = spotifyUrl,
        iTunes = iTunesUrl,
        yandexMusic = yandexMusicUrl
    ),
    details = TrackDetails(
        album = album,
        metadata = metadata,
        date = date,
        time = time
    )
)