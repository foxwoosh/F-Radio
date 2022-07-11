package com.foxwoosh.radio.player.helpers

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.os.Build
import com.foxwoosh.radio.MainActivity
import com.foxwoosh.radio.R
import com.foxwoosh.radio.domain.models.Track
import com.foxwoosh.radio.player.models.PlayerColors
import com.foxwoosh.radio.player.models.PlayerState

class PlayerNotificationFabric(private val context: Context) {

    companion object {
        const val notificationChannelID = "player_channel_id"

        const val ACTION_PLAYER_PLAY = "b100252a-5bc4-4232-825b-634e36725423"
        const val ACTION_PLAYER_PAUSE = "e3ce5c9f-80a1-4059-a0a8-c7dd71a22fec"
        const val ACTION_PLAYER_STOP = "55104f5f-768c-4365-908b-4e3f97cf99e6"
    }

    private val playAction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Notification.Action.Builder(
            Icon.createWithResource(context, R.drawable.ic_player_play),
            context.getString(R.string.notification_action_play),
            PendingIntent.getBroadcast(
                context,
                128459,
                Intent(ACTION_PLAYER_PLAY),
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    } else {
        Notification.Action.Builder(
            R.drawable.ic_player_play,
            context.getString(R.string.notification_action_play),
            PendingIntent.getBroadcast(
                context,
                128459,
                Intent(ACTION_PLAYER_PLAY),
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }.build()

    private val pauseAction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Notification.Action.Builder(
            Icon.createWithResource(context, R.drawable.ic_player_pause),
            context.getString(R.string.notification_action_stop),
            PendingIntent.getBroadcast(
                context,
                98741,
                Intent(ACTION_PLAYER_PAUSE),
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    } else {
        Notification.Action.Builder(
            R.drawable.ic_player_pause,
            context.getString(R.string.notification_action_stop),
            PendingIntent.getBroadcast(
                context,
                98741,
                Intent(ACTION_PLAYER_PAUSE),
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }.build()

    private val stopAction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Notification.Action.Builder(
            Icon.createWithResource(context, R.drawable.ic_player_stop),
            context.getString(R.string.notification_action_stop),
            PendingIntent.getBroadcast(
                context,
                1488228,
                Intent(ACTION_PLAYER_STOP),
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    } else {
        Notification.Action.Builder(
            R.drawable.ic_player_stop,
            context.getString(R.string.notification_action_stop),
            PendingIntent.getBroadcast(
                context,
                1488228,
                Intent(ACTION_PLAYER_STOP),
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }.build()

    fun getNotification(
        track: Track?,
        mediaSession: MediaSession?,
        playerState: PlayerState
    ): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, notificationChannelID)
        } else {
            Notification.Builder(context)
        }

        var image: Bitmap? = null
        var artist = ""
        var album: String? = null
        var color = PlayerColors.default.surfaceColor

        val title: String

        when (playerState) {
            PlayerState.IDLE -> {
                title = context.getString(R.string.player_title_idle)
            }
            PlayerState.BUFFERING -> {
                title = context.getString(R.string.player_title_loading)
            }
            PlayerState.PAUSED,
            PlayerState.PLAYING -> {
                if (track != null) {
                    image = track.cover
                    artist = track.artist
                    title = track.title
                    album = track.album
                    color = track.colors.surfaceColor
                } else {
                    title = context.getString(R.string.player_title_playing)
                }
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

        builder
            .setColor(color.value.toInt())
            .setContentTitle(title)
            .setContentText(artist)
            .setLargeIcon(image)
            .setSmallIcon(R.drawable.ic_foxy_radio_notification)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setAutoCancel(false)
            .setOngoing(playerState != PlayerState.IDLE)
            .setShowWhen(false)
            .setStyle(
                Notification.MediaStyle().also {
                    it.setMediaSession(mediaSession?.sessionToken)
                    when (playerState) {
                        PlayerState.PLAYING,
                        PlayerState.PAUSED -> it.setShowActionsInCompactView(0, 1)
                        else -> it.setShowActionsInCompactView(0)
                    }
                }
            )
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    11111,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )

        when (playerState) {
            PlayerState.PLAYING -> builder.addAction(pauseAction)
            PlayerState.PAUSED -> builder.addAction(playAction)
            PlayerState.IDLE,
            PlayerState.BUFFERING -> { /* nothing to add */ }
        }
        builder.addAction(stopAction)

        return builder.build()
    }
}