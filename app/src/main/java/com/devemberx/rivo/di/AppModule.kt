package com.devemberx.rivo.di

import android.content.Context
import android.os.SystemClock
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.devemberx.rivo.core.database.AppDatabase
import com.devemberx.rivo.core.database.DataStoreSettingsRepository
import com.devemberx.rivo.core.database.RoomCompanionRepository
import com.devemberx.rivo.core.domain.Clock
import com.devemberx.rivo.core.domain.IdGenerator
import com.devemberx.rivo.core.domain.PetRepository
import com.devemberx.rivo.core.domain.ProgressionIdentity
import com.devemberx.rivo.core.domain.QuestEvaluator
import com.devemberx.rivo.core.domain.QuestRepository
import com.devemberx.rivo.core.domain.RewardRepository
import com.devemberx.rivo.core.domain.SettingsRepository
import com.devemberx.rivo.core.domain.VehicleRepository
import com.devemberx.rivo.runtime.CompanionRuntime
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class AppEnvironment(
    val databaseName: String,
    val identity: ProgressionIdentity,
)

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun clock(): Clock = Clock { SystemClock.elapsedRealtime() }

    @Provides
    @Singleton
    fun ids(): IdGenerator = IdGenerator { UUID.randomUUID().toString() }

    @Provides
    fun identity(environment: AppEnvironment): ProgressionIdentity = environment.identity

    @Provides
    @Singleton
    fun evaluator(): QuestEvaluator = QuestEvaluator(maxAgeMillis = 15_000)

    @Provides
    @Singleton
    fun database(
        @ApplicationContext context: Context,
        environment: AppEnvironment,
    ): AppDatabase = Room.databaseBuilder(context, AppDatabase::class.java, environment.databaseName).build()

    @Provides
    @Singleton
    fun localRepository(
        database: AppDatabase,
        identity: ProgressionIdentity,
        clock: Clock,
        ids: IdGenerator,
        evaluator: QuestEvaluator,
    ): RoomCompanionRepository = RoomCompanionRepository(database, identity, clock, ids, evaluator)

    @Provides
    fun pets(repository: RoomCompanionRepository): PetRepository = repository

    @Provides
    fun quests(repository: RoomCompanionRepository): QuestRepository = repository

    @Provides
    fun rewards(repository: RoomCompanionRepository): RewardRepository = repository

    @Provides
    @Singleton
    fun preferences(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> =
        PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("companion-settings") }

    @Provides
    @Singleton
    fun settings(dataStore: DataStore<Preferences>): SettingsRepository = DataStoreSettingsRepository(dataStore)

    @Provides
    @Singleton
    fun runtime(vehicle: VehicleRepository): CompanionRuntime = CompanionRuntime(vehicle)
}

/** App-owned assembly; feature constructors remain usable without a DI framework. */
class AppDependencies
    @Inject
    constructor(
        val pets: PetRepository,
        val settings: SettingsRepository,
        val quests: QuestRepository,
        val rewards: RewardRepository,
        val vehicle: VehicleRepository,
        val identity: ProgressionIdentity,
        val clock: Clock,
        val evaluator: QuestEvaluator,
    )
