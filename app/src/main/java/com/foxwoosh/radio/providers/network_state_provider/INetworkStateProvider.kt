package com.foxwoosh.radio.providers.network_state_provider

import kotlinx.coroutines.flow.StateFlow

interface INetworkStateProvider {
    val networkState: StateFlow<NetworkState>
}