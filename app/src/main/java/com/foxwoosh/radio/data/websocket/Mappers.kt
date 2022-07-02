package com.foxwoosh.radio.data.websocket

import com.foxwoosh.radio.data.websocket.messages.incoming.SongDataIncomingMessage
import com.foxwoosh.radio.domain.models.Track

fun SongDataIncomingMessage.mapToModel() = Track(
    id = id,
    title = title,
    artist = artist,
    album = album,
    coverUrl = "${root}${cover}",
    metadata = metadata,
    date = date,
    time = time,
    youtubeMusicUrl = youtubeMusicUrl,
    youtubeUrl = youtubeUrl,
    spotifyUrl = spotifyUrl,
    iTunesUrl = iTunesUrl,
    yandexMusicUrl = yandexMusicUrl,
//    previousTracks = previousTracks.map {
//        PreviousTrack(
//            it.title,
//            it.artist,
//            "${root}${it.cover}",
//            it.date,
//            it.time,
//            it.youtubeMusicUrl,
//            it.youtubeUrl,
//            it.spotifyUrl,
//            it.itunesUrl,
//            it.yandexMusicUrl
//        )
//    }.reversed()
)