package com.devemberx.rivo.di

import com.devemberx.rivo.core.domain.ProgressionIdentity
import com.devemberx.rivo.core.domain.SignalSource
import com.devemberx.rivo.core.domain.VehicleRepository
import com.devemberx.rivo.core.vss.UnavailableVehicleRepository
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
        AppEnvironment("rivo.db", ProgressionIdentity("local-profile", SignalSource.REAL))

    @Provides
    @Singleton
    fun vehicle(): VehicleRepository = UnavailableVehicleRepository()
}
