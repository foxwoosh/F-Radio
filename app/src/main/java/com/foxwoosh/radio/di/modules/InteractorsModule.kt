package com.foxwoosh.radio.di.modules

import com.foxwoosh.radio.domain.player_service_interactor.IPlayerServiceInteractor
import com.foxwoosh.radio.domain.player_service_interactor.PlayerServiceInteractor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent

@Module
@InstallIn(ServiceComponent::class)
abstract class InteractorsModule {

    @Binds
    abstract fun providePlayerInteractor(
        playerServiceInteractor: PlayerServiceInteractor
    ): IPlayerServiceInteractor
}