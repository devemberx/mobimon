package com.monsters.mobimon.di

import com.monsters.mobimon.feature.auth.ConversationNetworkStatus
import com.monsters.mobimon.network.AndroidConversationNetworkStatus
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {
    @Binds
    abstract fun networkStatus(status: AndroidConversationNetworkStatus): ConversationNetworkStatus
}
