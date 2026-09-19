package com.monsters.mobimon.di

import com.monsters.mobimon.core.domain.Clock
import com.monsters.mobimon.core.domain.IdGenerator
import com.monsters.mobimon.core.domain.ProgressionIdentity
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.VehicleRepository
import com.monsters.mobimon.core.vss.DefaultParkedVssRawVehicleSource
import com.monsters.mobimon.core.vss.VssRawVehicleSource
import com.monsters.mobimon.vehicle.DemoVehicleRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VehicleProviderModule {
    @Provides
    fun environment(): AppEnvironment =
        AppEnvironment("mobimon-demo.db", ProgressionIdentity("demo-profile", SignalSource.SIMULATED))

    @Provides
    @Singleton
    fun vssRawVehicleSource(): VssRawVehicleSource = DefaultParkedVssRawVehicleSource()

    @Provides
    @Singleton
    fun vehicle(
        clock: Clock,
        ids: IdGenerator,
        settingsRepository: com.monsters.mobimon.core.domain.SettingsRepository,
        debugStore: com.monsters.mobimon.debug.DebugStore,
        vssRawSource: VssRawVehicleSource,
    ): VehicleRepository =
        DemoVehicleRepository(
            clock,
            ids,
            settingsRepository,
            debugStore,
            vssRawSource,
            CoroutineScope(SupervisorJob() + Dispatchers.Default),
        )
}
