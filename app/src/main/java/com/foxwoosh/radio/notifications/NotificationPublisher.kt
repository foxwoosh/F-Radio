package com.foxwoosh.radio.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import com.foxwoosh.radio.R

object NotificationPublisher {
    const val playerChannelID = "player_channel_id"

    @RequiresApi(Build.VERSION_CODES.O)
    fun createChannels(context: Context) {
        with(NotificationManagerCompat.from(context)) {
            createNotificationChannel(
                NotificationChannel(
                    playerChannelID,
                    context.getString(R.string.notification_channel_audio_player),
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
    }

    fun notify(
        context: Context,
        notificationID: Int,
        notification: Notification
    ) {
        NotificationManagerCompat.from(context)
            .notify(notificationID, notification)
    }
}