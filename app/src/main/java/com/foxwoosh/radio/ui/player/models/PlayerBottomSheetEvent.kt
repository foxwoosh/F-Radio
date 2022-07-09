package com.foxwoosh.radio.ui.player.models

sealed class PlayerBottomSheetEvent {
    object TrackChanged : PlayerBottomSheetEvent()
}