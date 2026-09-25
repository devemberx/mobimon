package com.monsters.mobimon.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowNetworkCapabilities

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AndroidConversationNetworkStatusTest {
    @Test
    fun validatedInternetIsRequiredAndCallbackReportsConnectionLoss() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val connectivity = context.getSystemService(ConnectivityManager::class.java)
        val network = connectivity.activeNetwork!!
        val capabilities = ShadowNetworkCapabilities.newInstance()
        shadowOf(capabilities).addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        shadowOf(connectivity).setNetworkCapabilities(network, capabilities)
        val status = AndroidConversationNetworkStatus(context)

        assertFalse(status.isOnline())
        shadowOf(capabilities).addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        shadowOf(connectivity).setNetworkCapabilities(network, capabilities)
        shadowOf(connectivity).networkCallbacks.single().onCapabilitiesChanged(network, capabilities)
        assertTrue(status.online.value)

        shadowOf(connectivity).setDefaultNetworkActive(false)
        shadowOf(connectivity).networkCallbacks.single().onLost(network)
        assertFalse(status.online.value)
    }
}
