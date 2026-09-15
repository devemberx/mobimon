package com.monsters.mobimon.di.features

import com.monsters.mobimon.BuildConfig
import com.monsters.mobimon.core.domain.PetRepository
import com.monsters.mobimon.core.domain.PointEconomy
import com.monsters.mobimon.core.domain.QuestRepository
import com.monsters.mobimon.core.domain.SettingsRepository
import com.monsters.mobimon.core.navigation.FeatureEntry
import com.monsters.mobimon.core.presentation.PointPresentation
import com.monsters.mobimon.core.presentation.VehiclePresentation
import com.monsters.mobimon.feature.pet.PetFeature
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CompanionFeatureModule {
    @Provides @Singleton
    fun wallet(points: PointEconomy): PointPresentation = PointPresentation(points)

    @Provides @IntoSet @Singleton
    fun entry(
        pets: PetRepository,
        settings: SettingsRepository,
        quests: QuestRepository,
        points: PointEconomy,
        wallet: PointPresentation,
        vehicle: VehiclePresentation,
    ): FeatureEntry = PetFeature(pets, settings, quests, points, wallet, vehicle, BuildConfig.DEBUG)
}
