package com.foxwoosh.radio.data.data_store

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreService @Inject constructor(
    @ApplicationContext private val appContext: Context
) {

    private val Context.dataStore by preferencesDataStore(DataStoreKeys.SETTINGS)

    suspend fun saveString(key: String, value: String) {
        saveValue(stringPreferencesKey(key), value)
    }

    suspend fun getString(key: String): String? {
        return getValue(stringPreferencesKey(key))
    }

    suspend fun saveLong(key: String, value: Long) {
        saveValue(longPreferencesKey(key), value)
    }

    suspend fun getLong(key: String): Long? {
        return getValue(longPreferencesKey(key))
    }

    private suspend fun <T> saveValue(key: Preferences.Key<T>, value: T) {
        appContext.dataStore.edit { data ->
            data[key] = value
        }
    }

    private suspend fun <T> getValue(key: Preferences.Key<T>): T? {
        return appContext.dataStore.data.map { data -> data[key] }.firstOrNull()
    }
}