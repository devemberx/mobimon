package com.monsters.mobimon.di

import android.content.Context
import com.monsters.mobimon.BuildConfig
import com.monsters.mobimon.core.auth.PersistentGitHubAuthentication
import com.monsters.mobimon.core.domain.AppUseState
import com.monsters.mobimon.core.domain.Clock
import com.monsters.mobimon.core.domain.ConversationProvider
import com.monsters.mobimon.core.domain.CurrentAppUse
import com.monsters.mobimon.core.domain.CurrentVehicleEvidence
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.GitHubAuthentication
import com.monsters.mobimon.core.domain.ProgressionIdentity
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.VehicleFreshnessPolicy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthenticationModule {
    @Provides
    @Singleton
    fun authentication(
        @ApplicationContext context: Context,
        appUse: CurrentAppUse,
        vehicle: CurrentVehicleEvidence,
        identity: ProgressionIdentity,
        clock: Clock,
        freshness: VehicleFreshnessPolicy,
    ): PersistentGitHubAuthentication =
        PersistentGitHubAuthentication.create(context, BuildConfig.GITHUB_CLIENT_ID) {
            val snapshot = freshness.displaySnapshot(vehicle.snapshot(), identity.source, clock.nowMillis())
            appUse.state() == AppUseState.ALLOWED &&
                snapshot.quality == SignalQuality.VALID &&
                snapshot.drivingState == DrivingState.PARKED
        }

    @Provides
    fun githubAuthentication(authentication: PersistentGitHubAuthentication): GitHubAuthentication = authentication

    @Provides
    @Singleton
    fun conversation(authentication: PersistentGitHubAuthentication): ConversationProvider =
        authentication.conversationProvider()
}
