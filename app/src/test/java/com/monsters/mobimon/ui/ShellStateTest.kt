package com.monsters.mobimon.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ShellStateTest {
    @Test
    fun detailBackReturnsToMenuThenSameHome() {
        val detail = ShellState(home = HomeSurface.VEHICLE, drawer = DrawerDestination.QUESTS)
        assertEquals(DrawerDestination.MENU, detail.back().drawer)
        assertEquals(ShellState(home = HomeSurface.VEHICLE), detail.back().back())
    }

    @Test
    fun closeDismissesWholeDrawer() {
        assertEquals(DrawerDestination.CLOSED, ShellState(drawer = DrawerDestination.SETTINGS).closeDrawer().drawer)
    }

    @Test
    fun openingDrawerAndSwitchingHomeCloseConversation() {
        val talking = ShellState(conversationUnavailable = true)
        assertFalse(talking.openDrawer(DrawerDestination.MENU).conversationUnavailable)
        assertEquals(ShellState(home = HomeSurface.VEHICLE), talking.switchHome())
    }

    @Test
    fun backClosesConversationBeforeLeavingHome() {
        assertEquals(ShellState(), ShellState(conversationUnavailable = true).back())
    }
}
