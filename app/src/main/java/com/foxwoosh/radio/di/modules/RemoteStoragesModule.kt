package com.foxwoosh.radio.di.modules

import com.foxwoosh.radio.storage.remote.current_data.CurrentDataRemoteStorageImpl
import com.foxwoosh.radio.storage.remote.current_data.CurrentDataRemoteStorage
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class RemoteStoragesModule {
    @Binds
    abstract fun bindCurrentDataRemoteStorage(
        storage: CurrentDataRemoteStorageImpl
    ): CurrentDataRemoteStorage
}