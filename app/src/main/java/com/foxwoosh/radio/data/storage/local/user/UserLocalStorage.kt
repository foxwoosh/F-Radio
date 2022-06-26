package com.foxwoosh.radio.data.storage.local.user

import com.foxwoosh.radio.data.data_store.DataStoreKeys
import com.foxwoosh.radio.data.data_store.DataStoreService
import com.foxwoosh.radio.data.database.DatabaseService
import com.foxwoosh.radio.data.database.map
import com.foxwoosh.radio.domain.models.CurrentUser
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserLocalStorage @Inject constructor(
    private val database: DatabaseService,
    private val dataStore: DataStoreService
): IUserLocalStorage {
    override val currentUser = database.read { currentUser().get().map { it?.map() } }

    override suspend fun saveToken(token: String) {
        dataStore.saveString(DataStoreKeys.AUTH_TOKEN, token)
    }

    override suspend fun saveCurrentUser(user: CurrentUser) {
        database.write {
            currentUser()
                .save(user.map())
        }
    }
}