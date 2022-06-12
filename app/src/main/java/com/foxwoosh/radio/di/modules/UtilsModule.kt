package com.foxwoosh.radio.di.modules

import com.foxwoosh.radio.providers.image_provider.CoilImageProvider
import com.foxwoosh.radio.providers.image_provider.ImageProvider
import com.foxwoosh.radio.providers.network_state_provider.INetworkStateProvider
import com.foxwoosh.radio.providers.network_state_provider.NetworkStateProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class, ServiceComponent::class)
abstract class UtilsModule {
    @Binds
    abstract fun bindImageLoader(imageLoaderImpl: CoilImageProvider): ImageProvider
}