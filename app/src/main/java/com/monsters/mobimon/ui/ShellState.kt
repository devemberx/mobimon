package com.monsters.mobimon.ui

enum class HomeSurface { PET, VEHICLE }

enum class AppRoute { HOME, QUESTS, VEHICLE_INFO, APPEARANCE, SETTINGS, CONVERSATION }

data class ShellState(
    val home: HomeSurface = HomeSurface.PET,
    val route: AppRoute = AppRoute.HOME,
    val menuOpen: Boolean = false,
) {
    fun back(): ShellState =
        when {
            menuOpen -> copy(menuOpen = false)
            route != AppRoute.HOME -> copy(route = AppRoute.HOME)
            else -> this
        }

    fun openMenu(): ShellState = copy(menuOpen = true)

    fun navigate(destination: AppRoute): ShellState = copy(route = destination, menuOpen = false)

    fun returnHome(): ShellState = copy(route = AppRoute.HOME, menuOpen = false)

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
