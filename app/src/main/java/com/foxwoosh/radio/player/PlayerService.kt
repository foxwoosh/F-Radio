package com.foxwoosh.radio.player

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Bundle
import android.os.IBinder
import android.os.ResultReceiver
import android.util.Log
import androidx.media3.common.*
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.RenderersFactory
import androidx.media3.exoplayer.audio.MediaCodecAudioRenderer
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import com.foxwoosh.radio.di.modules.PlayerServiceCoroutineScope
import com.foxwoosh.radio.domain.player_service_interactor.IPlayerServiceInteractor
import com.foxwoosh.radio.notifications.NotificationPublisher
import com.foxwoosh.radio.player.helpers.PlayerNotificationFabric
import com.foxwoosh.radio.player.models.PlayerState
import com.foxwoosh.radio.player.models.Station
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import javax.inject.Inject

@AndroidEntryPoint
class PlayerService : Service() {

    companion object {
        private const val KEY_STATION = "0e877531-9b37-477d-853d-357462d88c63"

        fun selectSource(context: Context, station: Station) {
            context.startService(
                Intent(context, PlayerService::class.java)
                    .putExtra(KEY_STATION, station)
            )
        }

        fun play(context: Context) {
            context.sendBroadcast(Intent(PlayerNotificationFabric.ACTION_PLAYER_PLAY))
        }

        fun pause(context: Context) {
            context.sendBroadcast(Intent(PlayerNotificationFabric.ACTION_PLAYER_PAUSE))
        }

        fun stop(context: Context) {
            context.sendBroadcast(Intent(PlayerNotificationFabric.ACTION_PLAYER_STOP))
        }
    }

    @PlayerServiceCoroutineScope
    @Inject
    lateinit var playerScope: CoroutineScope

    @Inject
    lateinit var interactor: IPlayerServiceInteractor

    private val notificationFabric by lazy { PlayerNotificationFabric(this) }

    private val player by lazy {
        val audioOnlyRenderersFactory = RenderersFactory { handler, _, audioListener, _, _ ->
            arrayOf(
                MediaCodecAudioRenderer(
                    this,
                    MediaCodecSelector.DEFAULT,
                    handler,
                    audioListener
                )
            )
        }

        ExoPlayer.Builder(this, audioOnlyRenderersFactory)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .build()
            .also {
                it.playWhenReady = true
                it.addListener(playerStateListener)
            }
    }

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        registerReceiver(
            broadcastReceiver,
            broadcastReceiver.filter
        )

        mediaSession = MediaSession(this, packageName)
            .apply {
                setCallback(mediaSessionCallback)
            }

        subscribeStateChanged()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val station = intent?.getSerializableExtra(KEY_STATION) as? Station

        if (station != null && interactor.station.value != station) {
            startForeground(
                PlayerNotificationFabric.notificationID,
                notificationFabric.getNotification(
                    interactor.trackData.value,
                    mediaSession,
                    interactor.playerState.value
                )
            )
            play(station.url)

            interactor.startFetching(station)

            interactor.station.value = station
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        interactor.station.value?.let { interactor.stopFetching(it) }
        interactor.station.value = null

        mediaSession?.isActive = false

        unregisterReceiver(broadcastReceiver)
        player.release()

        playerScope.cancel()

        super.onDestroy()
    }

    private fun play(url: String) {
        val mediaItem = MediaItem.fromUri(url)

        player.playWhenReady = true
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()

        mediaSession?.isActive = true
    }

    /**
     * For radio stream pause should stop player
     */
    private fun pause() {
        player.stop()
    }

    private fun subscribeStateChanged() {
        interactor
            .trackData
            .combine(interactor.playerState) { trackData, playerState ->
                mediaSession?.setPlaybackState(
                    PlaybackState.Builder()
                        .setState(
                            when (playerState) {
                                PlayerState.PLAYING -> PlaybackState.STATE_PLAYING
                                PlayerState.BUFFERING -> PlaybackState.STATE_BUFFERING
                                PlayerState.PAUSED -> PlaybackState.STATE_PAUSED
                                PlayerState.IDLE -> PlaybackState.STATE_STOPPED
                            },
                            0,
                            1f
                        )
                        .setActions(
                            when (playerState) {
                                PlayerState.PLAYING -> {
                                    PlaybackState.ACTION_PAUSE
                                }
                                PlayerState.PAUSED -> {
                                    PlaybackState.ACTION_PLAY
                                }
                                else -> 0
                            }
                        )
                        .build()
                )

                if (playerState != PlayerState.IDLE) {
                    NotificationPublisher.notify(
                        this,
                        PlayerNotificationFabric.notificationID,
                        notificationFabric.getNotification(
                            trackData,
                            mediaSession,
                            playerState
                        )
                    )
                }
            }
            .debounce(100)
            .launchIn(playerScope)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private val broadcastReceiver = object : BroadcastReceiver() {
        val filter = IntentFilter().apply {
            addAction(PlayerNotificationFabric.ACTION_PLAYER_PLAY)
            addAction(PlayerNotificationFabric.ACTION_PLAYER_PAUSE)
            addAction(PlayerNotificationFabric.ACTION_PLAYER_STOP)
        }

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                PlayerNotificationFabric.ACTION_PLAYER_PLAY ->
                    interactor.station.value?.let { play(it.url) }
                PlayerNotificationFabric.ACTION_PLAYER_PAUSE -> pause()
                PlayerNotificationFabric.ACTION_PLAYER_STOP -> stopSelf()
            }
        }
    }

    private val playerStateListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            interactor.playerState.value = when (playbackState) {
                Player.STATE_BUFFERING -> PlayerState.BUFFERING
                Player.STATE_READY -> PlayerState.PLAYING
                Player.STATE_IDLE -> if (interactor.station.value == null)
                    PlayerState.IDLE
                else
                    PlayerState.PAUSED
                else -> {
                    Log.e("PlayerService", "Unsupported player state")
                    PlayerState.IDLE
                }
            }
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            super.onPlayWhenReadyChanged(playWhenReady, reason)

            if (!playWhenReady) {
                pause()
            }
        }
    }

    private val mediaSessionCallback = object : MediaSession.Callback() {
        override fun onPlay() {
            interactor.station.value?.let { play(it.url) }
        }

        override fun onStop() {
            pause()
        }

        override fun onPause() {
            pause()
        }
    }
}