package com.foxwoosh.radio.player

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.IBinder
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import android.media.AudioAttributes as AndroidAudioAttributes
import com.foxwoosh.radio.image_loader.ImageLoader
import com.foxwoosh.radio.notifications.NotificationPublisher
import com.foxwoosh.radio.player.helpers.CoverColorExtractor
import com.foxwoosh.radio.player.helpers.PlayerNotificationFabric
import com.foxwoosh.radio.player.models.MusicServicesData
import com.foxwoosh.radio.player.models.PlayerTrackData
import com.foxwoosh.radio.player.models.Station
import com.foxwoosh.radio.storage.local.player.IPlayerLocalStorage
import com.foxwoosh.radio.storage.remote.ultra.IUltraDataRemoteStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import javax.inject.Inject

@AndroidEntryPoint
class PlayerService : Service(), CoroutineScope {

    companion object {
        private const val KEY_STATION = "0e877531-9b37-477d-853d-357462d88c63"

        private var currentStation: Station? = null
        var isRunning = false
            private set

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

    override val coroutineContext = SupervisorJob() + Dispatchers.IO

    @Inject
    lateinit var playerLocalStorage: IPlayerLocalStorage
    @Inject
    lateinit var ultraDataRemoteStorage: IUltraDataRemoteStorage
    @Inject
    lateinit var imageLoader: ImageLoader

    private val notificationFabric by lazy { PlayerNotificationFabric(this) }

    private val player by lazy {
        ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AndroidAudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AndroidAudioAttributes.USAGE_MEDIA)
                    .build(),
                true
            )
            .build()
            .also {
                it.addListener(playerStateListener)
            }
    }

    private var mediaSession: MediaSession? = null
    private var playerPolling: Job? = null

    override fun onCreate() {
        super.onCreate()

        isRunning = true

        registerReceiver(
            broadcastReceiver,
            broadcastReceiver.filter
        )

        mediaSession = MediaSession(this, packageName)
            .apply {
                setCallback(mediaSessionCallback)
            }

        playerLocalStorage.trackData.combine(playerLocalStorage.isPlaying) { trackData, isPlaying ->
            mediaSession?.setPlaybackState(
                PlaybackState.Builder()
                    .setActions(
                        PlaybackState.ACTION_STOP or if (isPlaying)
                            PlaybackState.ACTION_STOP
                        else
                            PlaybackState.ACTION_PLAY
                    )
                    .build()
            )
            mediaSession?.setMetadata(
                MediaMetadata.Builder()
                    .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, trackData.cover)
                    .putString(MediaMetadata.METADATA_KEY_ALBUM, trackData.album)
                    .putString(MediaMetadata.METADATA_KEY_ARTIST, trackData.artist)
                    .putString(MediaMetadata.METADATA_KEY_TITLE, trackData.title)
                    .build()
            )

            NotificationPublisher.notify(
                this,
                PlayerNotificationFabric.notificationID,
                notificationFabric.getNotification(
                    playerLocalStorage.trackData.value,
                    mediaSession?.sessionToken,
                    isPlaying
                )
            )
        }.launchIn(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val station = intent?.getSerializableExtra(KEY_STATION) as? Station

        if (station != null && currentStation != station) {
            startForeground(
                PlayerNotificationFabric.notificationID,
                notificationFabric.getNotification(
                    playerLocalStorage.trackData.value,
                    mediaSession?.sessionToken,
                    player.isPlaying
                )
            )
            play(station.url)

            playerPolling?.cancel()
            playerPolling = startPlayerPolling()

            currentStation = station
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        Log.i("DDLOG", "destroying service")

        isRunning = false

        unregisterReceiver(broadcastReceiver)
        player.release()
        playerPolling?.cancel()
        playerPolling = null

        super.onDestroy()
    }

    private fun play(url: String) {
        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    /**
     * For radio stream pause should stop player
     */
    private fun pause() {
        player.stop()
    }

    private fun startPlayerPolling() = launch {
        playerLocalStorage.setPlayerTrackData(PlayerTrackData.buffering)

        var currentUniqueID: String? = null

        while (isActive) {
            val fetchedUniqueID = ultraDataRemoteStorage.getUniqueID()
            if (fetchedUniqueID != currentUniqueID) {
                val track = ultraDataRemoteStorage.loadCurrentData()
                val coverBitmap = imageLoader.load(track.imageUrl)
                val (surfaceColor, primaryTextColor, secondaryTextColor) =
                    CoverColorExtractor.extractColors(coverBitmap)

                playerLocalStorage.setPlayerTrackData(
                    PlayerTrackData(
                        track.title,
                        track.artist,
                        track.album,
                        coverBitmap,
                        surfaceColor,
                        primaryTextColor,
                        secondaryTextColor,
                        MusicServicesData(
                            track.youtubeMusicUrl,
                            track.youtubeUrl,
                            track.spotifyUrl,
                            track.iTunesUrl,
                            track.yandexMusicUrl
                        )
                    )
                )

                currentUniqueID = fetchedUniqueID
            }

            delay(10000)
        }
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
                PlayerNotificationFabric.ACTION_PLAYER_PLAY -> currentStation?.let { play(it.url) }
                PlayerNotificationFabric.ACTION_PLAYER_PAUSE -> pause()
                PlayerNotificationFabric.ACTION_PLAYER_STOP -> stopSelf()
            }
        }
    }

    private val playerStateListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            launch { playerLocalStorage.setPlayerIsPlaying(isPlaying) }
        }
    }

    private val mediaSessionCallback = object : MediaSession.Callback() {
        override fun onPlay() {
            currentStation?.let { play(it.url) }
        }

        override fun onStop() {
            pause()
        }
    }
}