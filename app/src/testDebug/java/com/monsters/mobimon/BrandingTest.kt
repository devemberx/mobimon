package com.monsters.mobimon

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class BrandingTest {
    @Test
    fun debugVariantUsesMobiMonApplicationId() {
        assertEquals("com.monsters.mobimon.demo", BuildConfig.APPLICATION_ID)
    }

    @Test
    fun debugLauncherDisplaysMobiMonName() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val label = context.packageManager.getApplicationLabel(context.applicationInfo)

        assertEquals("MobiMon Demo", label.toString())
    }
}
