package com.monsters.mobimon.di.features

import com.monsters.mobimon.core.domain.PetRepository
import com.monsters.mobimon.core.domain.PointEconomy
import com.monsters.mobimon.core.navigation.FeatureEntry
import com.monsters.mobimon.core.presentation.VehiclePresentation
import com.monsters.mobimon.feature.auth.AiFeature
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiFeatureModule {
    @Provides @IntoSet @Singleton
    fun entry(
        pets: PetRepository,
        points: PointEconomy,
        vehicle: VehiclePresentation,
    ): FeatureEntry = AiFeature(pets, points, vehicle)
}
