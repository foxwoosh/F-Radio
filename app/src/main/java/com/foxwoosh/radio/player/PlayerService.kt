package com.foxwoosh.radio.player

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@AndroidEntryPoint
class PlayerService : Service() {

    companion object {
        private const val ACTION_PLAYER_START = "b100252a-5bc4-4232-825b-634e36725423"
        private const val ACTION_PLAYER_STOP = "55104f5f-768c-4365-908b-4e3f97cf99e6"

        fun createService(context: Context) {
            context.startService(Intent(context, PlayerService::class.java))
        }

        fun start(context: Context) {
            context.sendBroadcast(Intent(ACTION_PLAYER_START))
        }

        fun stop(context: Context) {
            context.sendBroadcast(Intent(ACTION_PLAYER_STOP))
        }
    }

    private val player by lazy {
        ExoPlayer.Builder(this)
            .build()
    }
    private val notificationCreator by lazy {
        PlayerNotificationCreator(this, ACTION_PLAYER_START, ACTION_PLAYER_STOP)
    }

    private val mutableTrackData = MutableStateFlow(PlayerTrackData.buffering)
    val trackData = mutableTrackData.asStateFlow()

    override fun onCreate() {
        super.onCreate()

        registerReceiver(
            broadcastReceiver,
            IntentFilter().apply {
                addAction(ACTION_PLAYER_START)
                addAction(ACTION_PLAYER_STOP)
            }
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            PlayerNotificationCreator.notificationID,
            notificationCreator.getNotification(
                this,
                null,
                trackData.value,
                player.isPlaying
            )
        )
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(broadcastReceiver)
    }

    private fun play(url: String) {
        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    private fun stop() {
        player.stop()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_PLAYER_START -> play(PlayerSource.ULTRA_HD.url)
                ACTION_PLAYER_STOP -> stop()
            }
        }
    }
}