package com.foxwoosh.radio.media_player

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.media.session.MediaSession
import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.drawable.toBitmap
import com.foxwoosh.radio.R
import com.foxwoosh.radio.notifications.NotificationPublisher

class MediaPlayerNotificationCreator(context: Context) {
    private val playAction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Notification.Action.Builder(
            Icon.createWithResource(context, R.drawable.ic_player_action_play),
            "Play",
            PendingIntent.getBroadcast(
                context,
                MediaPlayerService.mediaPlayerRequestCodeStart,
                Intent(MediaPlayerService.mediaPlayerActionStart),
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    } else {
        Notification.Action.Builder(
            R.drawable.ic_player_action_play,
            "Play",
            PendingIntent.getBroadcast(
                context,
                MediaPlayerService.mediaPlayerRequestCodeStart,
                Intent(MediaPlayerService.mediaPlayerActionStart),
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }.build()

    private val pauseAction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Notification.Action.Builder(
            Icon.createWithResource(context, R.drawable.ic_player_action_pause),
            "Pause",
            PendingIntent.getBroadcast(
                context,
                MediaPlayerService.mediaPlayerRequestCodePause,
                Intent(MediaPlayerService.mediaPlayerActionPause),
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    } else {
        Notification.Action.Builder(
            R.drawable.ic_player_action_pause,
            "Play",
            PendingIntent.getBroadcast(
                context,
                MediaPlayerService.mediaPlayerRequestCodePause,
                Intent(MediaPlayerService.mediaPlayerActionPause),
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }.build()

    fun getNotification(
        context: Context,
        mediaSessionToken: MediaSession.Token?,
        playerState: MediaPlayerState
    ): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, NotificationPublisher.playerChannelID)
        } else {
            Notification.Builder(context)
        }

        return builder
            .setColor(
                when (playerState) {
                    MediaPlayerState.Buffering,
                    MediaPlayerState.Paused -> Color.Black
                    is MediaPlayerState.Playing -> playerState.surfaceColor
                }.value.toInt()
            )
            .setContentTitle(
                when (playerState) {
                    MediaPlayerState.Buffering -> "Buffering..."
                    MediaPlayerState.Paused -> "Paused..."
                    is MediaPlayerState.Playing -> playerState.trackTitle
                }
            )
            .setSmallIcon(R.drawable.ic_notification_play)
            .setLargeIcon(
                when (playerState) {
                    MediaPlayerState.Buffering,
                    MediaPlayerState.Paused -> context.getDrawable(R.drawable.ic_snooze)
                        ?.toBitmap()
                    is MediaPlayerState.Playing -> playerState.cover
                }
            )
            .setContentText(
                when (playerState) {
                    MediaPlayerState.Buffering,
                    MediaPlayerState.Paused -> ""
                    is MediaPlayerState.Playing -> playerState.artist
                }
            )
            .addAction(
                when (playerState) {
                    MediaPlayerState.Buffering,
                    is MediaPlayerState.Playing -> pauseAction
                    MediaPlayerState.Paused -> playAction
                }
            )
            .setAutoCancel(false)
            .setOngoing(playerState is MediaPlayerState.Playing)
            .setShowWhen(false)
            .setStyle(
                Notification.MediaStyle().also {
                    it.setMediaSession(mediaSessionToken)
                }
            )
            .build()
    }
}