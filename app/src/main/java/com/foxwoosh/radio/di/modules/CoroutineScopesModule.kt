package com.foxwoosh.radio.di.modules

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier

@Module
@InstallIn(ServiceComponent::class)
class CoroutineScopesModule {

    @Provides
    @PlayerServiceCoroutineScope
    @ServiceScoped
    fun providePlayerServiceCoroutineScope() =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Provides
    @SettingsInteractorScope
    @ViewModelScoped
    fun provideSettingsInteractorScope() =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PlayerServiceCoroutineScope

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SettingsInteractorScope