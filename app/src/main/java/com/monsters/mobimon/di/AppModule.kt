package com.monsters.mobimon.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.monsters.mobimon.core.database.AppDatabase
import com.monsters.mobimon.core.database.DataStoreSettingsRepository
import com.monsters.mobimon.core.database.PointEconomyRepository
import com.monsters.mobimon.core.database.RoomCompanionRepository
import com.monsters.mobimon.core.domain.Clock
import com.monsters.mobimon.core.domain.CurrentAppUse
import com.monsters.mobimon.core.domain.CurrentVehicleEvidence
import com.monsters.mobimon.core.domain.IdGenerator
import com.monsters.mobimon.core.domain.PetRepository
import com.monsters.mobimon.core.domain.PointEconomy
import com.monsters.mobimon.core.domain.PointQuestCatalog
import com.monsters.mobimon.core.domain.ProgressionIdentity
import com.monsters.mobimon.core.domain.QuestEvaluator
import com.monsters.mobimon.core.domain.QuestRepository
import com.monsters.mobimon.core.domain.RewardRepository
import com.monsters.mobimon.core.domain.SettingsRepository
import com.monsters.mobimon.core.domain.UtcClock
import com.monsters.mobimon.core.domain.VehicleFreshnessPolicy
import com.monsters.mobimon.core.domain.VehicleRepository
import com.monsters.mobimon.runtime.AppUseStateSource
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
    fun freshness(): VehicleFreshnessPolicy = VehicleFreshnessPolicy(maxAgeMillis = 15_000)

    @Provides
    fun currentVehicleEvidence(vehicle: VehicleRepository): CurrentVehicleEvidence =
        CurrentVehicleEvidence { vehicle.snapshots.value }

    @Provides
    fun currentAppUse(appUse: AppUseStateSource): CurrentAppUse = appUse

    @Provides
    fun pointQuestCatalog(): PointQuestCatalog = PointQuestCatalog { null }

    @Provides
    @Singleton
    fun localRepository(
        database: AppDatabase,
        identity: ProgressionIdentity,
        clock: Clock,
        ids: IdGenerator,
        evaluator: QuestEvaluator,
        currentVehicle: CurrentVehicleEvidence,
        currentAppUse: CurrentAppUse,
    ): RoomCompanionRepository =
        RoomCompanionRepository(database, identity, clock, ids, evaluator, currentVehicle, currentAppUse)

    @Provides
    fun pets(repository: RoomCompanionRepository): PetRepository = repository

    @Provides
    fun quests(repository: RoomCompanionRepository): QuestRepository = repository

    @Provides
    fun rewards(repository: RoomCompanionRepository): RewardRepository = repository

    @Provides
    @Singleton
    fun points(
        database: AppDatabase,
        identity: ProgressionIdentity,
        utcClock: UtcClock,
        ids: IdGenerator,
        currentVehicle: CurrentVehicleEvidence,
        currentAppUse: CurrentAppUse,
        clock: Clock,
        evaluator: QuestEvaluator,
        catalog: PointQuestCatalog,
    ): PointEconomy =
        PointEconomyRepository(
            database,
            identity.profileId,
            utcClock,
            ids,
            identity.source,
            currentVehicle,
            currentAppUse,
            clock,
            evaluator,
            catalog,
        )

    @Provides
    @Singleton
    fun settings(dataStore: DataStore<Preferences>): SettingsRepository = DataStoreSettingsRepository(dataStore)

    @Provides
    @Singleton
    fun runtime(
        vehicle: VehicleRepository,
        appUse: AppUseStateSource,
    ): CompanionRuntime = CompanionRuntime(vehicle, appUse)
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
        val freshness: VehicleFreshnessPolicy,
        val points: PointEconomy,
        val appUse: AppUseStateSource,
    )
