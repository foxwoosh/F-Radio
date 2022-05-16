package com.foxwoosh.radio.media_player

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.IBinder
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.core.app.NotificationManagerCompat
import androidx.palette.graphics.Palette
import com.foxwoosh.radio.image_loader.ImageLoader
import com.foxwoosh.radio.notifications.NotificationPublisher
import com.foxwoosh.radio.storage.remote.current_data.CurrentDataRemoteStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class MediaPlayerService : Service(), CoroutineScope {

    companion object {
        private const val mediaSessionTag = "foxy_radio_media_session_tag"
        private const val mediaNotificationID = 128459

        internal const val mediaPlayerActionPlay = "action_player_start"
        internal const val mediaPlayerActionStop = "action_player_stop"

        internal const val mediaPlayerExtraSource = "media_player_extra_source"

        internal const val mediaPlayerRequestCodeStart = 999
        internal const val mediaPlayerRequestCodeStop = 888

        fun start(context: Context, source: MediaPlayerSource) {
            val intent = Intent(context, MediaPlayerService::class.java)
                .putExtra(mediaPlayerExtraSource, source)
            context.startService(intent)
        }

        fun play(context: Context) {
            context.sendBroadcast(Intent(mediaPlayerActionPlay))
        }

        fun stop(context: Context) {
            context.sendBroadcast(Intent(mediaPlayerActionStop))
        }
    }

    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO

    @Inject
    lateinit var currentDataRemoteStorage: CurrentDataRemoteStorage

    @Inject
    lateinit var imageLoader: ImageLoader

    private val mutableTrackData = MutableStateFlow(MediaPlayerTrackData.buffering)
    val trackData = mutableTrackData.asStateFlow()

    private val mutablePlayerState = MutableStateFlow(false)
    val playerState = mutablePlayerState.asStateFlow()

    private val mediaPlayer = MediaPlayer().apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )
    }
    private var mediaSession: MediaSession? = null

    private val notificationCreator by lazy { MediaPlayerNotificationCreator(this) }

    override fun onCreate() {
        super.onCreate()

        mediaSession = MediaSession(this, mediaSessionTag).also {
            it.setCallback(mediaSessionCallback)
            it.isActive = true
        }

        registerReceiver(broadcastReceiver, broadcastReceiver.filter)

        playerState.combine(trackData) { isPlaying, trackData ->
            updateMediaSession(trackData, isPlaying)
        }.launchIn(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getSerializableExtra(mediaPlayerExtraSource)?.let {
            setupMediaPlayer(it as MediaPlayerSource)
        }

        startForeground(
            mediaNotificationID,
            notificationCreator.getNotification(
                this,
                mediaSession?.sessionToken,
                trackData.value,
                playerState.value
            )
        )

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(broadcastReceiver)

        mediaSession?.release()
        mediaSession = null
    }

    override fun onBind(p0: Intent?): IBinder? = null

    private fun setupMediaPlayer(source: MediaPlayerSource) = launch {
        mediaPlayer.setDataSource(source.url)

        val track = currentDataRemoteStorage.loadCurrentData()

        val bitmap = imageLoader.load(track.imageUrl)

        val palette = Palette.Builder(bitmap).generate()

        val (surfaceColor, primaryTextColor, secondaryTextColor) =
            palette.darkVibrantSwatch?.let { getColorsFromSwatch(it) }
                ?: palette.mutedSwatch?.let { getColorsFromSwatch(it) }
                ?: palette.dominantSwatch?.let { getColorsFromSwatch(it) }
                ?: Triple(Color.Black, Color.White, Color.White)

        val trackData = MediaPlayerTrackData(
            track.title,
            track.artist,
            track.album,
            bitmap,
            surfaceColor,
            primaryTextColor,
            secondaryTextColor,
            MusicServicesData(
                youtubeMusic = track.youtubeMusicUrl,
                youtube = track.youtubeUrl,
                spotify = track.spotifyUrl,
                iTunes = track.iTunesUrl,
                yandexMusic = track.yandexMusicUrl
            )
        )

        mutableTrackData.emit(trackData)
    }

    fun play() = launch {
        mediaPlayer.prepare()
        mediaPlayer.start()

        mutablePlayerState.emit(true)
    }

    fun stop() = launch {
        mediaPlayer.stop()

        mutablePlayerState.emit(false)
    }

    private fun updateMediaSession(
        trackData: MediaPlayerTrackData,
        isPlaying: Boolean
    ) {
        mediaSession?.setPlaybackState(
            PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY_PAUSE)
                .build()
        )
        mediaSession?.setMetadata(
            MediaMetadata.Builder()
                .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, trackData.cover)
                .putString(MediaMetadata.METADATA_KEY_TITLE, trackData.title)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, trackData.artist)
                .putString(MediaMetadata.METADATA_KEY_ALBUM, trackData.album)
                .build()
        )

        val notification = notificationCreator.getNotification(
            this@MediaPlayerService,
            mediaSession?.sessionToken,
            trackData,
            isPlaying
        )
        NotificationPublisher.notify(this, mediaNotificationID, notification)
    }

    private fun getColorsFromSwatch(swatch: Palette.Swatch) = Triple(
        Color(swatch.rgb),
        Color(swatch.bodyTextColor),
        Color(swatch.titleTextColor)
    )

    private val broadcastReceiver = object : BroadcastReceiver() {
        val filter = IntentFilter().apply {
            addAction(mediaPlayerActionPlay)
            addAction(mediaPlayerActionStop)
        }

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                mediaPlayerActionPlay -> play()
                mediaPlayerActionStop -> stop()
            }
        }
    }

    private val mediaSessionCallback =  object : MediaSession.Callback() {
        override fun onPlay() {
            play()
        }

        override fun onStop() {
            stop()
        }
    }
}