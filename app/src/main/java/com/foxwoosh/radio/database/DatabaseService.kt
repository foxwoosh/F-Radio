package com.foxwoosh.radio.database

import android.content.Context
import androidx.room.Room
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseService @Inject constructor(
    @ApplicationContext appContext: Context
) {
    val db = Room.databaseBuilder(
        appContext,
        DatabaseProvider::class.java,
        "database.db"
    ).build()


}