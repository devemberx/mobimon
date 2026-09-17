package com.monsters.mobimon.di.features

import com.monsters.mobimon.core.domain.Clock
import com.monsters.mobimon.core.domain.PointEconomy
import com.monsters.mobimon.core.domain.ProgressionIdentity
import com.monsters.mobimon.core.domain.QuestEvaluator
import com.monsters.mobimon.core.domain.QuestRepository
import com.monsters.mobimon.core.domain.RewardRepository
import com.monsters.mobimon.core.domain.VehicleRepository
import com.monsters.mobimon.core.navigation.FeatureEntry
import com.monsters.mobimon.core.presentation.PointPresentation
import com.monsters.mobimon.core.presentation.VehiclePresentation
import com.monsters.mobimon.feature.quest.QuestFeature
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object QuestFeatureModule {
    @Provides @Singleton
    fun feature(
        quests: QuestRepository,
        rewards: RewardRepository,
        source: VehicleRepository,
        identity: ProgressionIdentity,
        clock: Clock,
        evaluator: QuestEvaluator,
        vehicle: VehiclePresentation,
        wallet: PointPresentation,
        economy: PointEconomy,
    ): QuestFeature = QuestFeature(quests, rewards, source, identity, clock, evaluator, vehicle, wallet, economy)

    @Provides @IntoSet
    fun entry(feature: QuestFeature): FeatureEntry = feature
}
