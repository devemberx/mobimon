package com.monsters.mobimon.runtime

import com.monsters.mobimon.core.domain.SettingsRepository
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.SignalSourceProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class DebugAwareSignalSourceProvider(
    private val defaultSource: SignalSource,
    settings: SettingsRepository,
    scope: CoroutineScope,
) : SignalSourceProvider {
    @Volatile
    private var debugModeEnabled = false

    init {
        scope.launch {
            settings.settings
                .map { it.debugModeEnabled }
                .distinctUntilChanged()
                .collect { debugModeEnabled = it }
        }
    }

    override fun source(): SignalSource =
        if (debugModeEnabled) {
            SignalSource.SIMULATED
        } else {
            defaultSource
        }
}
