package com.monsters.mobimon.ui

import com.monsters.mobimon.core.navigation.AiRoute
import com.monsters.mobimon.core.navigation.AppRoute
import com.monsters.mobimon.core.navigation.CompanionRoute

data class ShellState(
    val route: AppRoute = CompanionRoute.HOME,
    val menuOpen: Boolean = false,
    val connectionOrigin: AppRoute = CompanionRoute.HOME,
) {
    fun back(): ShellState =
        when {
            menuOpen -> copy(menuOpen = false)
            route == AiRoute.COPILOT -> copy(route = connectionOrigin, connectionOrigin = CompanionRoute.HOME)
            route != CompanionRoute.HOME -> copy(route = route.parent)
            else -> this
        }

    fun openMenu(): ShellState = copy(menuOpen = true)

    fun openCopilot(): ShellState =
        copy(
            route = AiRoute.COPILOT,
            menuOpen = false,
            connectionOrigin = if (route == CompanionRoute.SETTINGS) route else CompanionRoute.HOME,
        )

    fun navigate(destination: AppRoute): ShellState = copy(route = destination, menuOpen = false)

    fun returnHome(): ShellState =
        copy(route = CompanionRoute.HOME, menuOpen = false, connectionOrigin = CompanionRoute.HOME)
}
