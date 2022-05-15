package com.foxwoosh.radio.media_player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer

class MediaPlayerBroadcastReceiver(
    private val mediaPlayer: MediaPlayer
) : BroadcastReceiver() {

    val filter = IntentFilter().apply {
        addAction(MediaPlayerService.mediaPlayerActionStart)
        addAction(MediaPlayerService.mediaPlayerActionPause)
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            MediaPlayerService.mediaPlayerActionStart -> mediaPlayer.start()
            MediaPlayerService.mediaPlayerActionPause -> mediaPlayer.stop()
        }
    }
}