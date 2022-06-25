package com.foxwoosh.radio.di.modules

import com.foxwoosh.radio.data.storage.local.player.IPlayerLocalStorage
import com.foxwoosh.radio.data.storage.local.player.PlayerLocalStorage
import com.foxwoosh.radio.data.storage.local.user.IUserLocalStorage
import com.foxwoosh.radio.data.storage.local.user.UserLocalStorage
import com.foxwoosh.radio.data.storage.remote.lyrics.ILyricsRemoteStorage
import com.foxwoosh.radio.data.storage.remote.lyrics.LyricsRemoteStorage
import com.foxwoosh.radio.data.storage.remote.ultra.IUltraRemoteStorage
import com.foxwoosh.radio.data.storage.remote.ultra.UltraRemoteStorage
import com.foxwoosh.radio.data.storage.remote.user.IUserRemoteStorage
import com.foxwoosh.radio.data.storage.remote.user.UserRemoteStorage
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class, ServiceComponent::class)
abstract class StoragesModule {
    @Binds
    abstract fun bindUltraDataRemoteStorage(
        storage: UltraRemoteStorage
    ): IUltraRemoteStorage

    @Binds
    abstract fun bindPlayerLocalStorage(
        storage: PlayerLocalStorage
    ): IPlayerLocalStorage

    @Binds
    abstract fun bindLyricsRemoteStorage(
        storage: LyricsRemoteStorage
    ): ILyricsRemoteStorage

    @Binds
    abstract fun bindUserRemoteStorage(
        storage: UserRemoteStorage
    ): IUserRemoteStorage

    @Binds
    abstract fun bindUserLocalStorage(
        storage: UserLocalStorage
    ): IUserLocalStorage
}