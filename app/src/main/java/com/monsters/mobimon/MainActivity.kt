package com.monsters.mobimon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.monsters.mobimon.core.navigation.FeatureEntry
import com.monsters.mobimon.runtime.AppUseStateSource
import com.monsters.mobimon.ui.MobiMonApp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var entries: Set<@JvmSuppressWildcards FeatureEntry>

    @Inject lateinit var appUse: AppUseStateSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MobiMonApp(entries, appUse)
        }
    }
}
