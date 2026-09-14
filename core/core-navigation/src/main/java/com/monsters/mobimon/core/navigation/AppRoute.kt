package com.monsters.mobimon.core.navigation

/** Stable saved destination identity; each team extends its own route file. */
sealed interface AppRoute {
    val name: String
    val parent: AppRoute get() = CompanionRoute.HOME

    companion object {
        val entries: List<AppRoute> =
            CompanionRoute.entries + QuestRoute.entries + VehicleRoute.entries + AiRoute.entries
    }
}
