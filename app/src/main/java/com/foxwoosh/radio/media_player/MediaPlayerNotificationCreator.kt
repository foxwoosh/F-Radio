package com.foxwoosh.radio.media_player

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.media.session.MediaSession
import android.os.Build
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
                Intent(MediaPlayerService.mediaPlayerActionPlay),
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
                Intent(MediaPlayerService.mediaPlayerActionPlay),
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }.build()

    private val stopAction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Notification.Action.Builder(
            Icon.createWithResource(context, R.drawable.ic_player_action_stop),
            "Stop",
            PendingIntent.getBroadcast(
                context,
                MediaPlayerService.mediaPlayerRequestCodeStop,
                Intent(MediaPlayerService.mediaPlayerActionStop),
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    } else {
        Notification.Action.Builder(
            R.drawable.ic_player_action_stop,
            "Stop",
            PendingIntent.getBroadcast(
                context,
                MediaPlayerService.mediaPlayerRequestCodeStop,
                Intent(MediaPlayerService.mediaPlayerActionStop),
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }.build()

    fun getNotification(
        context: Context,
        mediaSessionToken: MediaSession.Token?,
        playerState: MediaPlayerTrackData,
        isPlaying: Boolean
    ): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, NotificationPublisher.playerChannelID)
        } else {
            Notification.Builder(context)
        }

        return builder
            .setColor(playerState.surfaceColor.value.toInt())
            .setContentTitle(playerState.title)
            .setSmallIcon(R.drawable.ic_notification_play)
            .setLargeIcon(playerState.cover)
            .setContentText(playerState.artist)
            .addAction(
                when (isPlaying) {
                    true -> stopAction
                    false -> playAction
                }
            )
            .setAutoCancel(false)
            .setOngoing(isPlaying)
            .setShowWhen(false)
            .setStyle(
                Notification.MediaStyle().also {
                    it.setMediaSession(mediaSessionToken)
                }
            )
            .build()
    }
}