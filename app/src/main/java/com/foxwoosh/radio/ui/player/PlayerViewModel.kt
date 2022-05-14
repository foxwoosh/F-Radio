package com.foxwoosh.radio.ui.player

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foxwoosh.radio.api.ApiService
import com.foxwoosh.radio.storage.remote.current_data.CurrentDataRemoteStorage
import com.foxwoosh.radio.storage.remote.current_data.ICurrentDataRemoteStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val storage: ICurrentDataRemoteStorage
): ViewModel() {

    private val mutableStateflow = MutableStateFlow<PlayerState>(PlayerState.Loading)
    val stateFlow = mutableStateflow.asStateFlow()

    init {
        viewModelScope.launch {
            val track = storage.loadCurrentData()

            mutableStateflow.emit(
                PlayerState.Done(
                    track.title,
                    track.artist,
                    track.album,
                    track.image
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("DDLOG", "PlayerViewModel onCleared")
    }
}