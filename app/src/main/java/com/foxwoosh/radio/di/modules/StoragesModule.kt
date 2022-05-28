package com.foxwoosh.radio.di.modules

import com.foxwoosh.radio.storage.local.player.IPlayerLocalStorage
import com.foxwoosh.radio.storage.local.player.PlayerLocalStorage
import com.foxwoosh.radio.storage.remote.lyrics.ILyricsRemoteStorage
import com.foxwoosh.radio.storage.remote.lyrics.LyricsRemoteStorage
import com.foxwoosh.radio.storage.remote.ultra.IUltraRemoteStorage
import com.foxwoosh.radio.storage.remote.ultra.UltraRemoteStorage
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
}