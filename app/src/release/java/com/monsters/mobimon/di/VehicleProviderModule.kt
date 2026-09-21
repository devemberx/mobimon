package com.monsters.mobimon.di

import android.content.Context
import com.monsters.mobimon.core.domain.Clock
import com.monsters.mobimon.core.domain.IdGenerator
import com.monsters.mobimon.core.domain.ProgressionIdentity
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.VehicleRepository
import com.monsters.mobimon.core.vss.UnavailableVehicleRepository
import com.monsters.mobimon.core.vss.VssAdapterLocator
import com.monsters.mobimon.core.vss.VssSourceVehicleRepository
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
    fun vehicle(
        @ApplicationContext context: Context,
        clock: Clock,
        ids: IdGenerator,
    ): VehicleRepository {
        val source = VssAdapterLocator.createOrNull(context) ?: return UnavailableVehicleRepository()
        return VssSourceVehicleRepository(
            clock,
            ids,
            source,
            CoroutineScope(SupervisorJob() + Dispatchers.Default),
        )
    }
}
