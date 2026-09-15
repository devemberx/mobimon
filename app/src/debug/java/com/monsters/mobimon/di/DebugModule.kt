package com.monsters.mobimon.di

import com.monsters.mobimon.core.database.AppDatabase
import com.monsters.mobimon.core.database.DebugPointRepository
import com.monsters.mobimon.core.domain.CurrentAppUse
import com.monsters.mobimon.core.domain.IdGenerator
import com.monsters.mobimon.core.domain.ProgressionIdentity
import com.monsters.mobimon.core.domain.UtcClock
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DebugModule {
    @Provides
    @Singleton
    fun points(
        database: AppDatabase,
        identity: ProgressionIdentity,
        utcClock: UtcClock,
        ids: IdGenerator,
        appUse: CurrentAppUse,
    ): DebugPointRepository =
        DebugPointRepository(
            database = database,
            profileId = identity.profileId,
            utcClock = utcClock,
            ids = ids,
            appUse = appUse,
        )
}
