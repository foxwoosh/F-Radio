package com.foxwoosh.radio.ui.player

import androidx.lifecycle.ViewModel
import com.foxwoosh.radio.storage.local.player.IPlayerLocalStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    playerLocalStorage: IPlayerLocalStorage
) : ViewModel() {

    val trackDataFlow = playerLocalStorage.trackData
    val isPlayingFlow = playerLocalStorage.isPlaying
}