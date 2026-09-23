package com.monsters.mobimon.di

import android.content.Context
import com.monsters.mobimon.core.domain.Clock
import com.monsters.mobimon.core.domain.IdGenerator
import com.monsters.mobimon.core.domain.ProgressionIdentity
import com.monsters.mobimon.core.domain.SettingsRepository
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.VehicleRepository
import com.monsters.mobimon.core.vss.DefaultParkedVssRawVehicleSource
import com.monsters.mobimon.core.vss.VssAdapterLocator
import com.monsters.mobimon.core.vss.VssRawVehicleSource
import com.monsters.mobimon.debug.DebugStore
import com.monsters.mobimon.vehicle.DemoVehicleRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
        AppEnvironment("mobimon.db", ProgressionIdentity("local-profile", SignalSource.REAL))

    @Provides
    @Singleton
    fun vssRawVehicleSource(
        @ApplicationContext context: Context,
    ): VssRawVehicleSource = VssAdapterLocator.createOrNull(context) ?: DefaultParkedVssRawVehicleSource()

    @Provides
    @Singleton
    fun vehicle(
        clock: Clock,
        ids: IdGenerator,
        settingsRepository: SettingsRepository,
        debugStore: DebugStore,
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
