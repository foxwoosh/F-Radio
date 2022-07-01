package com.foxwoosh.radio.utils

import android.content.Context
import coil.ImageLoader

private var imageLoader: ImageLoader? = null

val Context.crossfadeImageLoader: ImageLoader
    get() = imageLoader ?: ImageLoader.Builder(this)
        .crossfade(durationMillis = 1_000)
        .build().also { imageLoader = it }