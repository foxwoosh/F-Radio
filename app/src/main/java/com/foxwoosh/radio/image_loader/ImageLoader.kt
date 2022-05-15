package com.foxwoosh.radio.image_loader

import android.graphics.Bitmap

interface ImageLoader {
    fun load(url: String): Bitmap
}