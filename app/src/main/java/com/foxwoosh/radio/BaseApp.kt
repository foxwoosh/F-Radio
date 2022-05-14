package com.foxwoosh.radio

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BaseApp : Application() {

    override fun onCreate() {
        super.onCreate()

        context = applicationContext
    }

    companion object {
        lateinit var context: Context
    }
}