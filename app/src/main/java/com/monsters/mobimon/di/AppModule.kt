package com.monsters.mobimon.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.monsters.mobimon.core.database.AppDatabase
import com.monsters.mobimon.core.database.DataStoreSettingsRepository
import com.monsters.mobimon.core.database.RoomCompanionRepository
import com.monsters.mobimon.core.domain.Clock
import com.monsters.mobimon.core.domain.IdGenerator
import com.monsters.mobimon.core.domain.PetRepository
import com.monsters.mobimon.core.domain.ProgressionIdentity
import com.monsters.mobimon.core.domain.QuestEvaluator
import com.monsters.mobimon.core.domain.QuestRepository
import com.monsters.mobimon.core.domain.RewardRepository
import com.monsters.mobimon.core.domain.SettingsRepository
import com.monsters.mobimon.core.domain.VehicleRepository
import com.monsters.mobimon.runtime.CompanionRuntime
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
    fun ids(): IdGenerator = IdGenerator { UUID.randomUUID().toString() }

    @Provides
    fun identity(environment: AppEnvironment): ProgressionIdentity = environment.identity

    @Provides
    @Singleton
    fun evaluator(): QuestEvaluator = QuestEvaluator(maxAgeMillis = 15_000)

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
