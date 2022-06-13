package com.foxwoosh.radio.di.modules

import com.foxwoosh.radio.providers.image_provider.CoilImageProvider
import com.foxwoosh.radio.providers.image_provider.ImageProvider
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