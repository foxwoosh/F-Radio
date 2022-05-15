package com.foxwoosh.radio

import android.app.Application
import android.content.Context
import android.os.Build
import com.foxwoosh.radio.notifications.NotificationPublisher
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BaseApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationPublisher.createChannels(this)
        }
    }
}