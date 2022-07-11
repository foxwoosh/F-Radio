package com.foxwoosh.radio.providers.network_state_provider

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkStateProvider @Inject constructor(@ApplicationContext private val context: Context) {

    private val mutableNetworkState = MutableStateFlow(NetworkState.NOT_CONNECTED)
    val networkState = mutableNetworkState.asStateFlow()

    private val service = context.getSystemService<ConnectivityManager>()!!

    init {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                mutableNetworkState.value = NetworkState.CONNECTED
            }

            override fun onLost(network: Network) {
                mutableNetworkState.value = NetworkState.NOT_CONNECTED
            }
        }

        service.registerNetworkCallback(networkRequest, callback)
    }
}