package com.monsters.mobimon.core.domain

import kotlinx.coroutines.flow.Flow

interface PetRepository {
    val profile: Flow<PetProfile>

    suspend fun initialize()

    suspend fun setAppearance(appearance: PetAppearance): WriteResult
}
