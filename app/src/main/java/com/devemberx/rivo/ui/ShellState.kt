package com.devemberx.rivo.ui

enum class HomeSurface { PET, VEHICLE }

enum class DrawerDestination { CLOSED, MENU, PET_INFO, QUESTS, VEHICLE_INFO, APPEARANCE, SETTINGS }

data class ShellState(
    val home: HomeSurface = HomeSurface.PET,
    val drawer: DrawerDestination = DrawerDestination.CLOSED,
    val conversationUnavailable: Boolean = false,
) {
    fun back(): ShellState =
        when (drawer) {
            DrawerDestination.CLOSED -> copy(conversationUnavailable = false)
            DrawerDestination.MENU -> closeDrawer()
            else -> copy(drawer = DrawerDestination.MENU)
        }

    fun closeDrawer(): ShellState = copy(drawer = DrawerDestination.CLOSED)

    fun openDrawer(destination: DrawerDestination): ShellState =
        copy(drawer = destination, conversationUnavailable = false)

    fun switchHome(): ShellState =
        ShellState(
            home = if (home == HomeSurface.PET) HomeSurface.VEHICLE else HomeSurface.PET,
        )
}
