package com.foxwoosh.radio.di.modules

import com.foxwoosh.radio.image_loader.CoilImageProvider
import com.foxwoosh.radio.image_loader.ImageProvider
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