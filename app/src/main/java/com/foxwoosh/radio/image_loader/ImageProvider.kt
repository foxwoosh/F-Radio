package com.foxwoosh.radio.image_loader

import android.graphics.Bitmap

interface ImageProvider {
    suspend fun load(url: String): Bitmap
}