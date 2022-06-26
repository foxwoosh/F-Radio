package com.foxwoosh.radio.data.database

import android.content.Context
import androidx.room.Room
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseService @Inject constructor(
    @ApplicationContext appContext: Context
) {
    private val db = Room.databaseBuilder(
        appContext,
        DatabaseProvider::class.java,
        "database.db"
    ).build()

    suspend fun write(block: suspend DatabaseProvider.() -> Unit) {
        withContext(Dispatchers.IO) {
            block(db)
        }
    }

    fun <T> read(block: DatabaseProvider.() -> T): T {
        return block(db)
    }
}