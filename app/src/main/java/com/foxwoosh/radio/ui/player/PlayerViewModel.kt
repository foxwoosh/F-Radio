package com.foxwoosh.radio.ui.player

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.foxwoosh.radio.image_loader.ImageLoader
import com.foxwoosh.radio.media_player.MusicServicesData
import com.foxwoosh.radio.storage.remote.current_data.CurrentDataRemoteStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val storage: CurrentDataRemoteStorage,
    private val imageLoader: ImageLoader
) : ViewModel() {

    private val mutableStateflow = MutableStateFlow<PlayerState>(PlayerState.Loading)
    val stateFlow = mutableStateflow.asStateFlow()

    init {
        reload()
    }

    fun reload() = viewModelScope.launch {
        val track = storage.loadCurrentData()

        withContext(Dispatchers.IO) {
            val bitmap = imageLoader.load(track.imageUrl)

            val palette = Palette.Builder(bitmap).generate()

            val (surfaceColor, primaryTextColor, secondaryTextColor) =
                palette.darkVibrantSwatch?.let { getColorsFromSwatch(it) }
                    ?: palette.mutedSwatch?.let { getColorsFromSwatch(it) }
                    ?: palette.dominantSwatch?.let { getColorsFromSwatch(it) }
                    ?: Triple(Color.Black, Color.White, Color.White)

            mutableStateflow.emit(
                PlayerState.Done(
                    track.title,
                    track.artist,
                    bitmap,
                    surfaceColor,
                    primaryTextColor,
                    secondaryTextColor,
                    MusicServicesData(
                        youtubeMusic = track.youtubeMusicUrl,
                        youtube = track.youtubeUrl,
                        spotify = track.spotifyUrl,
                        iTunes = track.iTunesUrl,
                        yandexMusic = track.yandexMusicUrl
                    )
                )
            )
        }
    }

    private fun getColorsFromSwatch(swatch: Palette.Swatch) = Triple(
        Color(swatch.rgb),
        Color(swatch.bodyTextColor),
        Color(swatch.titleTextColor)
    )
}