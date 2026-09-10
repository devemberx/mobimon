package com.monsters.mobimon.di

import android.content.Context
import android.os.SystemClock
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.monsters.mobimon.core.database.AppDatabase
import com.monsters.mobimon.core.domain.Clock
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlatformModule {
    @Provides
    @Singleton
    fun clock(): Clock = Clock { SystemClock.elapsedRealtime() }

    @Provides
    @Singleton
    fun database(
        @ApplicationContext context: Context,
        environment: AppEnvironment,
    ): AppDatabase = Room.databaseBuilder(context, AppDatabase::class.java, environment.databaseName).build()

    @Provides
    @Singleton
    fun preferences(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> =
        PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("companion-settings") }
}
