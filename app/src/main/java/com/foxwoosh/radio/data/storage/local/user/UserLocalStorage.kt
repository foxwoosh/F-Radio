package com.foxwoosh.radio.data.storage.local.user

import com.foxwoosh.radio.data.data_store.DataStoreKeys
import com.foxwoosh.radio.data.data_store.DataStoreService
import com.foxwoosh.radio.data.database.DatabaseService
import com.foxwoosh.radio.data.database.models.UserDb
import com.foxwoosh.radio.domain.models.CurrentUser
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserLocalStorage @Inject constructor(
    private val database: DatabaseService,
    private val dataStore: DataStoreService
): IUserLocalStorage {
    override val user = database.user.get().map {
            db -> db?.let { CurrentUser(it.id, it.login, it.name, it.email) }
    }

    override suspend fun saveToken(token: String) {
        dataStore.saveString(DataStoreKeys.AUTH_TOKEN, token)
    }

    override suspend fun saveUser(id: Long, login: String, name: String, email: String) {
        database.write { user().insert(UserDb(id, login, name, email)) }
    }
}