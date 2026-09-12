package com.monsters.mobimon.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class ShellStateTest {
    @Test
    fun menuDestinationUsesFullScreenAndBackReturnsHome() {
        val home = ShellState(home = HomeSurface.VEHICLE)
        val destination = home.openMenu().navigate(AppRoute.QUESTS)
        assertEquals(false, destination.menuOpen)
        assertEquals(AppRoute.QUESTS, destination.route)
        assertEquals(home, destination.back())
    }

    @Test
    fun backDismissesMenuBeforeChangingRoute() {
        val state = ShellState(route = AppRoute.SETTINGS).openMenu()
        assertEquals(ShellState(route = AppRoute.SETTINGS), state.back())
        assertEquals(ShellState(), state.back().back())
    }

    @Test
    fun homeSwitchClearsTransientNavigation() {
        assertEquals(ShellState(home = HomeSurface.VEHICLE), ShellState(route = AppRoute.CONVERSATION).switchHome())
    }

    @Test
    fun oldDrawerSavedStateRestoresSafelyAfterNavigationUpgrade() {
        assertEquals(
            ShellState(home = HomeSurface.VEHICLE),
            ShellSaver.restore(listOf("VEHICLE", "CLOSED")),
        )
        assertEquals(
            ShellState(route = AppRoute.QUESTS),
            ShellSaver.restore(listOf("PET", "QUESTS")),
        )
    }
}
