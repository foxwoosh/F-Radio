package com.foxwoosh.radio.player

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.media.session.MediaSession
import android.os.Build
import com.foxwoosh.radio.R
import com.foxwoosh.radio.notifications.NotificationPublisher

class PlayerNotificationCreator(
    context: Context,
    playerActionStart: String,
    playerActionStop: String
) {
    companion object {
        const val notificationID = 777
    }

    private val playAction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Notification.Action.Builder(
            Icon.createWithResource(context, R.drawable.ic_player_play),
            context.getString(R.string.notification_action_play),
            PendingIntent.getBroadcast(
                context,
                128459,
                Intent(playerActionStart),
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
                Intent(playerActionStart),
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
                98741,
                Intent(playerActionStop),
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    } else {
        Notification.Action.Builder(
            R.drawable.ic_player_stop,
            context.getString(R.string.notification_action_stop),
            PendingIntent.getBroadcast(
                context,
                98741,
                Intent(playerActionStop),
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }.build()

    fun getNotification(
        context: Context,
        mediaSessionToken: MediaSession.Token?,
        trackData: PlayerTrackData,
        isPlaying: Boolean
    ): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, NotificationPublisher.playerChannelID)
        } else {
            Notification.Builder(context)
        }

        return builder
            .setColor(trackData.surfaceColor.value.toInt())
            .setContentTitle(trackData.title)
            .setSmallIcon(R.drawable.ic_notification_play)
            .setLargeIcon(trackData.cover)
            .setContentText(trackData.artist)
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