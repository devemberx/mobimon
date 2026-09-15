package com.monsters.mobimon.core.domain

enum class PetAppearance { GOLDEN, CREAM }

data class PetProfile(
    val id: String,
    val appearance: PetAppearance = PetAppearance.GOLDEN,
    val totalXp: Int = 0,
)

data class CompanionSettings(
    val reducedMotion: Boolean = false,
    val launcherCharacterEnabled: Boolean = false,
    val debugModeEnabled: Boolean = false,
)

sealed interface WriteResult {
    data object Success : WriteResult

    data object Failure : WriteResult
}
