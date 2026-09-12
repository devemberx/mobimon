package com.monsters.mobimon.di

import android.content.Context
import com.monsters.mobimon.runtime.AppUseStateSource
import com.monsters.mobimon.runtime.CarAppUseMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppUseModule {
    @Provides
    @Singleton
    fun appUseStateSource(
        @ApplicationContext context: Context,
    ): AppUseStateSource = CarAppUseMonitor(context)
}
