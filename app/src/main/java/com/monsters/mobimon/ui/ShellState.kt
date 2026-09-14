package com.monsters.mobimon.ui

enum class HomeSurface { PET, VEHICLE }

enum class AppRoute { HOME, QUESTS, VEHICLE_INFO, APPEARANCE, SETTINGS, CONVERSATION, COPILOT }

data class ShellState(
    val home: HomeSurface = HomeSurface.PET,
    val route: AppRoute = AppRoute.HOME,
    val menuOpen: Boolean = false,
    val connectionOrigin: AppRoute = AppRoute.HOME,
) {
    fun back(): ShellState =
        when {
            menuOpen -> copy(menuOpen = false)
            route == AppRoute.COPILOT -> copy(route = connectionOrigin, connectionOrigin = AppRoute.HOME)
            route != AppRoute.HOME -> copy(route = AppRoute.HOME)
            else -> this
        }

    fun openMenu(): ShellState = copy(menuOpen = true)

    fun openCopilot(): ShellState =
        copy(
            route = AppRoute.COPILOT,
            menuOpen = false,
            connectionOrigin = if (route == AppRoute.SETTINGS) route else AppRoute.HOME,
        )

    fun navigate(destination: AppRoute): ShellState = copy(route = destination, menuOpen = false)

    fun returnHome(): ShellState = copy(route = AppRoute.HOME, menuOpen = false, connectionOrigin = AppRoute.HOME)

    fun switchHome(): ShellState =
        copy(
            home =
                if (home ==
                    HomeSurface.PET
                ) {
                    HomeSurface.VEHICLE
                } else {
                    HomeSurface.PET
                },
            route = AppRoute.HOME,
            menuOpen = false,
        )
}
