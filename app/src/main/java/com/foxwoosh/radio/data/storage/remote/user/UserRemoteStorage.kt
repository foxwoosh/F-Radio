package com.foxwoosh.radio.data.storage.remote.user

import com.foxwoosh.radio.data.api.ApiService
import javax.inject.Inject

class UserRemoteStorage @Inject constructor(
    private val apiService: ApiService
) : IUserRemoteStorage {
}