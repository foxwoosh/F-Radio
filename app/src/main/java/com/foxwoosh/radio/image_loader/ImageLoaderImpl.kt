package com.foxwoosh.radio.image_loader

import android.content.Context
import com.bumptech.glide.Glide
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ImageLoaderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ImageLoader {

    override fun load(url: String) = Glide
        .with(context)
        .asBitmap()
        .load(url)
        .submit()
        .get()
}