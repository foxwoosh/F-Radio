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
import android.media.AudioAttributes as AndroidAudioAttributes
import com.foxwoosh.radio.image_loader.ImageLoader
import com.foxwoosh.radio.notifications.NotificationPublisher
import com.foxwoosh.radio.storage.local.player.IPlayerLocalStorage
import com.foxwoosh.radio.storage.remote.ultra.IUltraDataRemoteStorage
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import javax.inject.Inject

@AndroidEntryPoint
class PlayerService : Service(), CoroutineScope {

    companion object {
        private const val ACTION_PLAYER_PLAY = "b100252a-5bc4-4232-825b-634e36725423"
        private const val ACTION_PLAYER_STOP = "55104f5f-768c-4365-908b-4e3f97cf99e6"

        fun createService(context: Context) {
            context.startService(Intent(context, PlayerService::class.java))
        }

        fun play(context: Context) {
            context.sendBroadcast(Intent(ACTION_PLAYER_PLAY))
        }

        fun stop(context: Context) {
            context.sendBroadcast(Intent(ACTION_PLAYER_STOP))
        }
    }

    override val coroutineContext = SupervisorJob() + Dispatchers.IO

    @Inject lateinit var playerLocalStorage: IPlayerLocalStorage
    @Inject lateinit var ultraDataRemoteStorage: IUltraDataRemoteStorage
    @Inject lateinit var imageLoader: ImageLoader

    private val helper by lazy {
        PlayerHelper(this, ACTION_PLAYER_PLAY, ACTION_PLAYER_STOP)
    }

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
                        if (isPlaying)
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

//            NotificationPublisher.notify(
//                this,
//                PlayerHelper.notificationID,
//                helper.getNotification(
//                    this,
//                    playerLocalStorage.trackData.value,
//                    mediaSession.sessionToken,
//                    isPlaying
//                )
//            )
        }.launchIn(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            PlayerHelper.notificationID,
            helper.getNotification(
                this,
//                playerLocalStorage.trackData.value,
                mediaSession,
                player.isPlaying
            )
        )

        Log.i("DDLOG", "polling started")
        playerPolling = startPlayerPolling()

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        Log.i("DDLOG", "destroying service")

        unregisterReceiver(broadcastReceiver)
        player.release()
        playerPolling?.cancel()
        playerPolling = null

        super.onDestroy()
    }

    private fun play(url: String) {
        Log.i("DDLOG", "play $url")
        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    private fun stop() {
        player.stop()
    }

    private fun startPlayerPolling() = launch {
        var currentUniqueID: String? = null

        while (true) {
            val fetchedUniqueID = ultraDataRemoteStorage.getUniqueID()
            if (fetchedUniqueID != currentUniqueID) {
                Log.i("DDLOG", "unique id is different, loading track info")
                val track = ultraDataRemoteStorage.loadCurrentData()
                val coverBitmap = imageLoader.load(track.imageUrl)
                val (surfaceColor, primaryTextColor, secondaryTextColor) =
                    helper.extractColors(coverBitmap)

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
            addAction(ACTION_PLAYER_PLAY)
            addAction(ACTION_PLAYER_STOP)
        }

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_PLAYER_PLAY -> play(PlayerSource.ULTRA_HD.url)
                ACTION_PLAYER_STOP -> stop()
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
            play(PlayerSource.ULTRA_HD.url)
        }

        override fun onStop() {
            stop()
        }
    }
}