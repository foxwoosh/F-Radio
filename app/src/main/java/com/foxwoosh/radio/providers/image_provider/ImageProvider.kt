package com.foxwoosh.radio.providers.image_provider

import android.graphics.Bitmap

interface ImageProvider {
    suspend fun load(url: String): Bitmap
}