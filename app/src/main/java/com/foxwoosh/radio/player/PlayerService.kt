package com.foxwoosh.radio.player

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.IBinder
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.foxwoosh.radio.R
import com.foxwoosh.radio.image_loader.ImageProvider
import android.media.AudioAttributes as AndroidAudioAttributes
import com.foxwoosh.radio.notifications.NotificationPublisher
import com.foxwoosh.radio.player.helpers.CoverColorExtractor
import com.foxwoosh.radio.player.helpers.PlayerNotificationFabric
import com.foxwoosh.radio.player.models.MusicServicesData
import com.foxwoosh.radio.player.models.PlayerState
import com.foxwoosh.radio.player.models.Station
import com.foxwoosh.radio.player.models.TrackDataState
import com.foxwoosh.radio.storage.local.player.IPlayerLocalStorage
import com.foxwoosh.radio.storage.remote.ultra.IUltraRemoteStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
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
    lateinit var ultraDataRemoteStorage: IUltraRemoteStorage

    @Inject
    lateinit var imageProvider: ImageProvider

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
                it.playWhenReady = true
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

        subscribeStateChanged()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val station = intent?.getSerializableExtra(KEY_STATION) as? Station

        if (station != null && currentStation != station) {
            startForeground(
                PlayerNotificationFabric.notificationID,
                notificationFabric.getNotification(
                    playerLocalStorage.trackData.value,
                    mediaSession?.sessionToken,
                    playerLocalStorage.playerState.value
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
        currentStation = null

        unregisterReceiver(broadcastReceiver)
        player.release()
        playerPolling?.cancel()
        playerPolling = null

        launch {
            playerLocalStorage.setPlayerTrackData(TrackDataState.Idle)
            playerLocalStorage.setPlayerState(PlayerState.IDLE)
        }

        coroutineContext.cancel()

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
        playerLocalStorage.setPlayerTrackData(TrackDataState.Loading)

        var currentUniqueID: String? = null

        while (isActive) {
            val fetchedUniqueID = ultraDataRemoteStorage.getUniqueID()
            if (fetchedUniqueID != currentUniqueID) {
                val track = ultraDataRemoteStorage.loadCurrentData()
                val coverBitmap = imageProvider.load(track.imageUrl)

                playerLocalStorage.setPlayerTrackData(
                    TrackDataState.Ready(
                        track.title,
                        track.artist,
                        track.album,
                        coverBitmap,
                        CoverColorExtractor.extractColors(coverBitmap),
                        MusicServicesData(
                            track.youtubeMusicUrl,
                            track.youtubeUrl,
                            track.spotifyUrl,
                            track.iTunesUrl,
                            track.yandexMusicUrl
                        ),
                        track.previousTracks,
                        track.lyrics
                    )
                )

                currentUniqueID = fetchedUniqueID
            }

            delay(10000)
        }
    }

    private fun subscribeStateChanged() {
        playerLocalStorage
            .trackData
            .combine(playerLocalStorage.playerState) { trackData, playerState ->
                mediaSession?.setPlaybackState(
                    PlaybackState.Builder()
                        .setActions(
                            when (playerState) {
                                PlayerState.PLAYING -> {
                                    PlaybackState.ACTION_PAUSE or PlaybackState.ACTION_STOP
                                }
                                PlayerState.IDLE -> {
                                    PlaybackState.ACTION_PLAY
                                }
                                else -> 0
                            }
                        )
                        .build()
                )

                val image: Bitmap?
                val album: String?
                val artist: String
                val title: String

                when (trackData) {
                    TrackDataState.Idle -> {
                        image = getDrawable(R.drawable.ic_no_music_playing)?.toBitmap()
                        album = ""
                        artist = ""
                        title = getString(R.string.player_title_idle)
                    }
                    TrackDataState.Loading -> {
                        image = getDrawable(R.drawable.ic_no_music_playing)?.toBitmap()
                        album = ""
                        artist = ""
                        title = getString(R.string.player_title_loading)
                    }
                    is TrackDataState.Ready -> {
                        image = trackData.cover
                        album = trackData.album
                        artist = trackData.artist
                        title = trackData.title
                    }
                }

                mediaSession?.setMetadata(
                    MediaMetadata.Builder()
                        .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, image)
                        .putString(MediaMetadata.METADATA_KEY_ALBUM, album)
                        .putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
                        .putString(MediaMetadata.METADATA_KEY_TITLE, title)
                        .build()
                )

                if (playerState != PlayerState.IDLE) {
                    NotificationPublisher.notify(
                        this,
                        PlayerNotificationFabric.notificationID,
                        notificationFabric.getNotification(
                            playerLocalStorage.trackData.value,
                            mediaSession?.sessionToken,
                            playerState
                        )
                    )
                }
            }
            .debounce(100)
            .launchIn(this)
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
        override fun onPlaybackStateChanged(playbackState: Int) {
            launch {
                playerLocalStorage.setPlayerState(
                    when (playbackState) {
                        Player.STATE_BUFFERING -> PlayerState.BUFFERING
                        Player.STATE_READY -> PlayerState.PLAYING
                        Player.STATE_IDLE -> if (currentStation == null)
                            PlayerState.IDLE
                        else
                            PlayerState.PAUSED
                        else -> {
                            Log.e("PlayerService", "Unsupported player state")
                            PlayerState.IDLE
                        }
                    }
                )
            }
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