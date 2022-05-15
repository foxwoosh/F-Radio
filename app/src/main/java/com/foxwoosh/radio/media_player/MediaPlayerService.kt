package com.foxwoosh.radio.media_player

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.session.MediaSession
import android.os.IBinder
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.core.app.NotificationManagerCompat
import androidx.palette.graphics.Palette
import com.foxwoosh.radio.image_loader.ImageLoader
import com.foxwoosh.radio.storage.remote.current_data.CurrentDataRemoteStorage
import com.foxwoosh.radio.ui.player.PlayerState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class MediaPlayerService : Service(), CoroutineScope {

    companion object {
        private const val mediaSessionTag = "foxy_radio_media_session_tag"
        private const val mediaNotificationID = 128459

        internal const val mediaPlayerActionStart = "action_player_start"
        internal const val mediaPlayerActionPause = "action_player_pause"

        internal const val mediaPlayerRequestCodeStart = 999
        internal const val mediaPlayerRequestCodePause = 888

        fun start(context: Context) {
            val intent = Intent(context, MediaPlayerService::class.java)
            context.startService(intent)
        }
    }

    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO

    @Inject
    lateinit var currentDataRemoteStorage: CurrentDataRemoteStorage

    @Inject
    lateinit var imageLoader: ImageLoader

    private val mutableStateFlow = MutableStateFlow<MediaPlayerState>(MediaPlayerState.Buffering)
    val stateFlow = mutableStateFlow.asStateFlow()

    private val mediaPlayer = MediaPlayer().apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )
    }
    private val mediaPlayerBroadcastReceiver = MediaPlayerBroadcastReceiver(mediaPlayer)
    private var mediaSession: MediaSession? = null

    private val notificationCreator by lazy { MediaPlayerNotificationCreator(this) }

    override fun onCreate() {
        super.onCreate()

        mediaSession = MediaSession(this, mediaSessionTag).also {
            it.setCallback(
                object : MediaSession.Callback() {
                    override fun onPlay() {
                        Log.i("DDLOG", "MediaSessionCallback onPlay")
                    }

                    override fun onPause() {
                        Log.i("DDLOG", "MediaSessionCallback onPause")
                    }

                    override fun onStop() {
                        Log.i("DDLOG", "MediaSessionCallback onStop")
                    }
                }
            )
            it.isActive = true
        }

        registerReceiver(mediaPlayerBroadcastReceiver, mediaPlayerBroadcastReceiver.filter)

        stateFlow
            .onEach {
                val notification = notificationCreator.getNotification(
                    this,
                    mediaSession?.sessionToken,
                    it
                )

                if (it is MediaPlayerState.Playing) {
                    startForeground(mediaNotificationID, notification)
                } else {
                    stopForeground(false)

                    NotificationManagerCompat.from(this)
                        .notify(mediaNotificationID, notification)
                }
            }
            .launchIn(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startMediaSession(MediaPlayerSource.ULTRA_HD)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(mediaPlayerBroadcastReceiver)

        mediaSession?.release()
        mediaSession = null
    }

    override fun onBind(p0: Intent?): IBinder? = null

    private fun startMediaSession(source: MediaPlayerSource) = launch {
        mediaPlayer.setDataSource(source.url)
        mediaPlayer.prepare()
        mediaPlayer.start()

        val track = currentDataRemoteStorage.loadCurrentData()

        val bitmap = imageLoader.load(track.imageUrl)

        val palette = Palette.Builder(bitmap).generate()

        val (surfaceColor, primaryTextColor, secondaryTextColor) =
            palette.darkVibrantSwatch?.let { getColorsFromSwatch(it) }
                ?: palette.mutedSwatch?.let { getColorsFromSwatch(it) }
                ?: palette.dominantSwatch?.let { getColorsFromSwatch(it) }
                ?: Triple(Color.Black, Color.White, Color.White)

        mutableStateFlow.emit(
            if (mediaPlayer.isPlaying) {
                MediaPlayerState.Playing(
                    track.title,
                    track.artist,
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
            } else MediaPlayerState.Paused
        )
    }

    private fun getColorsFromSwatch(swatch: Palette.Swatch) = Triple(
        Color(swatch.rgb),
        Color(swatch.bodyTextColor),
        Color(swatch.titleTextColor)
    )
}