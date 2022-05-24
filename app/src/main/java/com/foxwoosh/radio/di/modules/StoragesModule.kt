package com.foxwoosh.radio.di.modules

import com.foxwoosh.radio.storage.local.player.IPlayerLocalStorage
import com.foxwoosh.radio.storage.local.player.PlayerLocalStorage
import com.foxwoosh.radio.storage.remote.ultra.UltraDataRemoteStorage
import com.foxwoosh.radio.storage.remote.ultra.IUltraDataRemoteStorage
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class, ServiceComponent::class)
abstract class StoragesModule {
    @Binds
    abstract fun bindUltraDataRemoteStorage(
        storage: UltraDataRemoteStorage
    ): IUltraDataRemoteStorage

    @Binds
    abstract fun bindPlayerLocalStorage(
        storage: PlayerLocalStorage
    ): IPlayerLocalStorage
}