package com.monsters.mobimon.di

import com.monsters.mobimon.core.domain.ProgressionIdentity
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.VehicleRepository
import com.monsters.mobimon.core.vss.UnavailableVehicleRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VehicleProviderModule {
    @Provides
    fun environment(): AppEnvironment =
        AppEnvironment("mobimon.db", ProgressionIdentity("local-profile", SignalSource.REAL))

    @Provides
    @Singleton
    fun vehicle(): VehicleRepository = UnavailableVehicleRepository()
}
