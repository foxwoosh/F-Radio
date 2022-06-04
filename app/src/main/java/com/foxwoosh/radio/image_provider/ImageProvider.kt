package com.foxwoosh.radio.image_provider

import android.graphics.Bitmap

interface ImageProvider {
    suspend fun load(url: String): Bitmap
}