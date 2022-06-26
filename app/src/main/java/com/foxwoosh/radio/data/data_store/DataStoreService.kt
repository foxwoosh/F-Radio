package com.foxwoosh.radio.data.data_store

import android.content.Context
import androidx.datastore.preferences.core.edit
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
        appContext.dataStore.edit { data ->
            data[stringPreferencesKey(key)] = value
        }
    }

    suspend fun getString(key: String): String? {
        return appContext.dataStore.data.map { data ->
            data[stringPreferencesKey(key)]
        }.firstOrNull()
    }
}