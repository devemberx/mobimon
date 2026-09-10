package com.monsters.mobimon.core.domain

class RewardCalculator {
    fun stage(totalXp: Int): Int {
        require(totalXp >= 0) { "XP cannot be negative" }

        return when {
            totalXp < STAGE_TWO_XP -> 1
            totalXp < STAGE_THREE_XP -> 2
            else -> 3
        }
    }

    fun xpUntilNextStage(totalXp: Int): Int? {
        require(totalXp >= 0) { "XP cannot be negative" }

        return when {
            totalXp < STAGE_TWO_XP -> STAGE_TWO_XP - totalXp
            totalXp < STAGE_THREE_XP -> STAGE_THREE_XP - totalXp
            else -> null
        }
    }

    private companion object {
        const val STAGE_TWO_XP = 80
        const val STAGE_THREE_XP = 240
    }
}
