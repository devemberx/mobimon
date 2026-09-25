package com.monsters.mobimon.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.monsters.mobimon.feature.auth.ConversationNetworkStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidConversationNetworkStatus
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : ConversationNetworkStatus {
        private val connectivity = context.getSystemService(ConnectivityManager::class.java)
        private val mutableOnline = MutableStateFlow(hasValidatedInternet())
        override val online = mutableOnline.asStateFlow()

        private val callback =
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) = refresh()

                override fun onLost(network: Network) = refresh()

                override fun onCapabilitiesChanged(
                    network: Network,
                    capabilities: NetworkCapabilities,
                ) = refresh()
            }

        init {
            connectivity.registerDefaultNetworkCallback(callback)
            refresh()
        }

        override fun isOnline(): Boolean = hasValidatedInternet().also { mutableOnline.value = it }

        private fun refresh() {
            mutableOnline.value = hasValidatedInternet()
        }

        private fun hasValidatedInternet(): Boolean =
            connectivity.getNetworkCapabilities(connectivity.activeNetwork)?.let {
                it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    it.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            } == true
    }
