package com.monsters.mobimon.di.features

import com.monsters.mobimon.core.domain.PointEconomy
import com.monsters.mobimon.core.navigation.FeatureEntry
import com.monsters.mobimon.core.presentation.CompanionAppearancePresentation
import com.monsters.mobimon.core.presentation.PointPresentation
import com.monsters.mobimon.core.presentation.VehiclePresentation
import com.monsters.mobimon.feature.customization.CustomizationFeature
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CustomizationFeatureModule {
    @Provides @IntoSet @Singleton
    fun entry(
        points: PointEconomy,
        wallet: PointPresentation,
        appearance: CompanionAppearancePresentation,
        vehicle: VehiclePresentation,
    ): FeatureEntry = CustomizationFeature(points, wallet, appearance, vehicle)
}
