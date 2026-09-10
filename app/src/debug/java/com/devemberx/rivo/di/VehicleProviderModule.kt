package com.devemberx.rivo.di

import com.devemberx.rivo.core.domain.Clock
import com.devemberx.rivo.core.domain.IdGenerator
import com.devemberx.rivo.core.domain.ProgressionIdentity
import com.devemberx.rivo.core.domain.SignalSource
import com.devemberx.rivo.core.domain.VehicleRepository
import com.devemberx.rivo.vehicle.DemoVehicleRepository
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
        AppEnvironment("rivo-demo.db", ProgressionIdentity("demo-profile", SignalSource.SIMULATED))

    @Provides
    @Singleton
    fun vehicle(
        clock: Clock,
        ids: IdGenerator,
    ): VehicleRepository = DemoVehicleRepository(clock, ids, CoroutineScope(SupervisorJob() + Dispatchers.Default))
}
