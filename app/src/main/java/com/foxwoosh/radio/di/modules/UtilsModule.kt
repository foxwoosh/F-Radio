package com.foxwoosh.radio.di.modules

import com.foxwoosh.radio.image_loader.ImageLoader
import com.foxwoosh.radio.image_loader.ImageLoaderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class UtilsModule {
    @Binds
    abstract fun bindImageLoader(imageLoaderImpl: ImageLoaderImpl): ImageLoader
}