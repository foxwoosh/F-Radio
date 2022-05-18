package com.foxwoosh.radio.player

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import com.foxwoosh.radio.R
import com.foxwoosh.radio.notifications.NotificationPublisher

class PlayerHelper(
    context: Context,
    playerActionPlay: String,
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
                Intent(playerActionPlay),
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
                Intent(playerActionPlay),
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
        mediaSession: MediaSession?,
        isPlaying: Boolean
    ): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, NotificationPublisher.playerChannelID)
        } else {
            Notification.Builder(context)
        }

        val controller = mediaSession?.controller

        return builder
//            .setColor(trackData.surfaceColor.value.toInt())
            .setContentTitle(controller?.metadata?.getString(MediaMetadata.METADATA_KEY_TITLE))
            .setContentText(controller?.metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST))
            .setLargeIcon(controller?.metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART))
            .addAction(
                when (isPlaying) {
                    true -> stopAction
                    false -> playAction
                }
            )
            .setSmallIcon(R.drawable.ic_notification_play)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setAutoCancel(false)
            .setOngoing(isPlaying)
            .setShowWhen(false)
            .setStyle(
                Notification.MediaStyle().also {
                    it.setMediaSession(mediaSession?.sessionToken)
                }
            )
            .build()
    }

    fun extractColors(bitmap: Bitmap): Triple<Color, Color, Color> {
        val palette = Palette.Builder(bitmap).generate()

        return palette.darkVibrantSwatch?.let { getColorsFromSwatch(it) }
                ?: palette.mutedSwatch?.let { getColorsFromSwatch(it) }
                ?: palette.dominantSwatch?.let { getColorsFromSwatch(it) }
                ?: Triple(Color.Black, Color.White, Color.White)
    }

    private fun getColorsFromSwatch(swatch: Palette.Swatch) = Triple(
        Color(swatch.rgb),
        Color(swatch.bodyTextColor),
        Color(swatch.titleTextColor)
    )
}