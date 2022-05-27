package com.foxwoosh.radio.domain

import com.foxwoosh.radio.player.models.PlayerState
import com.foxwoosh.radio.storage.local.player.IPlayerLocalStorage
import com.foxwoosh.radio.storage.models.Track
import com.foxwoosh.radio.storage.remote.ultra.IUltraRemoteStorage
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class PlayerServiceInteractor @Inject constructor(
    private val playerLocalStorage: IPlayerLocalStorage,
    private val ultraRemoteStorage: IUltraRemoteStorage
) : IPlayerServiceInteractor {
    override val trackData: MutableStateFlow<Track>
        get() = TODO("Not yet implemented")
    override val playerState: MutableStateFlow<PlayerState>
        get() = TODO("Not yet implemented")

    override suspend fun fetchTrackDataIfNeeded(currentUniqueID: String?): String {
        TODO("Not yet implemented")
    }
    //    override val trackData = playerLocalStorage.trackData
//    override val playerState = playerLocalStorage.playerState
//
//    override suspend fun fetchTrackDataIfNeeded(currentUniqueID: String?): String {
//        val fetchedUniqueID = ultraRemoteStorage.getUniqueID()
//
//        return if (fetchedUniqueID != currentUniqueID) {
//            trackData.emit(ultraRemoteStorage.loadCurrentData())
//
//            fetchedUniqueID
//        } else {
//            currentUniqueID
//        }
//    }
}