package com.monsters.mobimon.di.features

import com.monsters.mobimon.BuildConfig
import com.monsters.mobimon.core.domain.SettingsRepository
import com.monsters.mobimon.core.navigation.FeatureEntry
import com.monsters.mobimon.core.presentation.VehiclePresentation
import com.monsters.mobimon.feature.pet.SettingsFeature
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SettingsFeatureModule {
    @Provides @IntoSet @Singleton
    fun entry(
        settings: SettingsRepository,
        vehicle: VehiclePresentation,
    ): FeatureEntry = SettingsFeature(settings, vehicle, BuildConfig.DEBUG)
}
