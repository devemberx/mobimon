package com.monsters.mobimon.testing

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.monsters.mobimon.core.database.AppDatabase
import com.monsters.mobimon.core.domain.Clock
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.ProgressionIdentity
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.UtcClock
import com.monsters.mobimon.core.domain.VehicleRepository
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.di.AppEnvironment
import com.monsters.mobimon.di.PlatformModule
import com.monsters.mobimon.di.VehicleProviderModule
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Files
import javax.inject.Inject
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [PlatformModule::class, VehicleProviderModule::class],
)
object JourneyTestModule {
    @Provides
    fun clock(): Clock = Clock { 10_000L }

    @Provides
    fun utcClock(): UtcClock = UtcClock { 1_800_000_000_000L }

    @Provides
    fun environment(): AppEnvironment =
        AppEnvironment("unused-journey.db", ProgressionIdentity("journey-profile", SignalSource.SIMULATED))

    @Provides
    fun vehicle(vehicle: JourneyVehicle): VehicleRepository = vehicle

    @Provides
    fun database(storage: JourneyStorage): AppDatabase = storage.database

    @Provides
    fun preferences(storage: JourneyStorage): DataStore<Preferences> = storage.preferences
}

@Singleton
class JourneyVehicle
    @Inject
    constructor() : VehicleRepository {
        private val state =
            MutableStateFlow(
                VehicleSnapshot(
                    id = "journey-1",
                    epoch = "journey-epoch",
                    sequence = 1,
                    receivedAtMillis = 10_000L,
                    source = SignalSource.SIMULATED,
                    drivingState = DrivingState.PARKED,
                    quality = SignalQuality.VALID,
                    batteryPercent = 72,
                ),
            )
        override val snapshots = state.asStateFlow()

        fun publish(
            drivingState: DrivingState = DrivingState.PARKED,
            quality: SignalQuality = SignalQuality.VALID,
        ) {
            val next = state.value.sequence + 1
            state.value =
                state.value.copy(id = "journey-$next", sequence = next, drivingState = drivingState, quality = quality)
        }

        override fun start() = Unit

        override fun stop() = Unit
    }

@Singleton
class JourneyStorage
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : AutoCloseable {
        private val directory = Files.createTempDirectory(context.cacheDir.toPath(), "journey-").toFile()
        private val job = SupervisorJob()
        val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val preferences =
            PreferenceDataStoreFactory.create(scope = CoroutineScope(job + Dispatchers.IO)) {
                File(directory, "settings.preferences_pb")
            }

        override fun close() {
            runBlocking { job.cancelAndJoin() }
            database.close()
            check(directory.deleteRecursively()) { "Could not remove journey test preferences" }
        }
    }
