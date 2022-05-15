package com.foxwoosh.radio.ui.player

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

@Composable
fun ExtractColors(
    imageUrl: String,
    onFinish: (bitmap: Bitmap, surface: Int, primary: Int, secondary: Int) -> Unit
) {
    Glide.with(LocalContext.current)
        .asBitmap()
        .load(imageUrl)
        .into(
            object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    Palette.Builder(resource)
                        .generate { p ->
                            p?.darkVibrantSwatch?.let {
                                onFinish(resource, it.rgb, it.bodyTextColor, it.titleTextColor)
                                return@generate
                            }
                            p?.mutedSwatch?.let {
                                onFinish(resource, it.rgb, it.bodyTextColor, it.titleTextColor)
                                return@generate
                            }
                            p?.dominantSwatch?.let {
                                onFinish(resource, it.rgb, it.bodyTextColor, it.titleTextColor)
                                return@generate
                            }
                        }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            }
        )
}