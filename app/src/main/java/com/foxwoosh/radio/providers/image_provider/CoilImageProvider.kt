package com.foxwoosh.radio.providers.image_provider

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoilImageProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : ImageProvider {

    private val imageLoader = ImageLoader(context)
    private val requestBuilder = ImageRequest.Builder(context)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .allowHardware(false)

    override suspend fun load(url: String): Bitmap? {
        return try {
            val request = requestBuilder.data(url).build()
            imageLoader.execute(request).drawable?.toBitmap()
        } catch (e: Exception) {
            null
        }
    }
}