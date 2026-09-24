package com.monsters.mobimon.di.features

import com.monsters.mobimon.core.domain.Clock
import com.monsters.mobimon.core.domain.ProgressionIdentity
import com.monsters.mobimon.core.domain.SettingsRepository
import com.monsters.mobimon.core.domain.SignalSourceProvider
import com.monsters.mobimon.core.domain.UtcClock
import com.monsters.mobimon.core.domain.VehicleFreshnessPolicy
import com.monsters.mobimon.core.domain.VehicleRepository
import com.monsters.mobimon.core.navigation.FeatureEntry
import com.monsters.mobimon.core.presentation.CompanionAppearancePresentation
import com.monsters.mobimon.core.presentation.VehiclePresentation
import com.monsters.mobimon.debug.DebugBackgroundTimeProvider
import com.monsters.mobimon.feature.vehicle.VehicleFeature
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VehicleFeatureModule {
    @Provides @Singleton
    fun backgroundTimeOverride(
        settings: SettingsRepository,
        debug: DebugBackgroundTimeProvider,
        scope: CoroutineScope,
    ): StateFlow<String?> =
        combine(settings.settings, debug.backgroundTimeOverride) { preferences, override ->
            if (preferences.debugModeEnabled) override else null
        }.stateIn(scope, SharingStarted.Eagerly, null)

    @Provides @Singleton
    fun presentation(
        source: VehicleRepository,
        identity: ProgressionIdentity,
        clock: Clock,
        freshness: VehicleFreshnessPolicy,
        utcClock: UtcClock,
        sourceProvider: SignalSourceProvider,
        backgroundOverride: StateFlow<String?>,
    ): VehiclePresentation =
        VehiclePresentation(source, identity, clock, freshness, utcClock, sourceProvider, backgroundOverride)

    @Provides @IntoSet @Singleton
    fun entry(
        vehicle: VehiclePresentation,
        appearance: CompanionAppearancePresentation,
    ): FeatureEntry = VehicleFeature(vehicle, appearance)
}
