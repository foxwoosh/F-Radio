package com.foxwoosh.radio.providers.image_provider

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoilImageProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : ImageProvider {

    override suspend fun load(url: String): Bitmap? {
        return try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)
                .build()

            val result = (loader.execute(request) as SuccessResult).drawable
            return (result as BitmapDrawable).bitmap
        } catch (e: Exception) {
            null
        }
    }
}