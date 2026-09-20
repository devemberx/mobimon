package com.monsters.mobimon.di.features

import com.monsters.mobimon.core.domain.PointEconomy
import com.monsters.mobimon.core.domain.PointQuestCatalog
import com.monsters.mobimon.core.navigation.FeatureEntry
import com.monsters.mobimon.core.presentation.CompanionAppearancePresentation
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
    @Provides @IntoSet @Singleton
    fun entry(
        vehicle: VehiclePresentation,
        wallet: PointPresentation,
        appearance: CompanionAppearancePresentation,
        economy: PointEconomy,
        catalog: PointQuestCatalog,
    ): FeatureEntry = QuestFeature(vehicle, wallet, appearance, economy, catalog)
}
