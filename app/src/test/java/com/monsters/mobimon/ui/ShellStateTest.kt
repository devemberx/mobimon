package com.monsters.mobimon.ui

import com.monsters.mobimon.core.navigation.AiRoute
import com.monsters.mobimon.core.navigation.CompanionRoute
import com.monsters.mobimon.core.navigation.QuestRoute
import org.junit.Assert.assertEquals
import org.junit.Test

class ShellStateTest {
    @Test
    fun chatRequiresAccountAndConnectionBackKeepsEntryOrigin() {
        listOf(CompanionRoute.HOME, CompanionRoute.SETTINGS).forEach { origin ->
            val disconnected = ShellState(route = origin).navigate(AiRoute.CONVERSATION)
            assertEquals(AiRoute.COPILOT, disconnected.route)
            assertEquals(origin, disconnected.back().route)
            assertEquals(AiRoute.CONVERSATION, ShellState(route = origin).navigate(AiRoute.CONVERSATION, true).route)
        }
    }

    @Test
    fun disconnectedRestoredChatAndConnectionOriginCannotLoopBackToChat() {
        val restored = ShellSaver.restore(listOf("CONVERSATION", "HOME"))!!
        assertEquals(AiRoute.COPILOT, restored.requireConversationAccount(false).route)
        assertEquals(CompanionRoute.HOME, restored.requireConversationAccount(false).back().route)
        val connection = ShellState(route = AiRoute.CONVERSATION).openCopilot().requireConversationAccount(false)
        assertEquals(CompanionRoute.HOME, connection.back().route)
    }

    @Test
    fun copilotBackReturnsToItsOriginIncludingAfterRestoration() {
        listOf(CompanionRoute.HOME, CompanionRoute.SETTINGS, AiRoute.CONVERSATION).forEach { origin ->
            val state = ShellState(route = origin).openCopilot()
            assertEquals(AiRoute.COPILOT, state.route)
            assertEquals(origin, state.back().route)
            assertEquals(origin, ShellSaver.restore(listOf("PET", "COPILOT", origin.name))?.back()?.route)
        }
    }

    @Test
    fun invalidConnectionOriginCannotRestoreAnArbitraryDestination() {
        assertEquals(CompanionRoute.HOME, ShellSaver.restore(listOf("PET", "COPILOT", "QUESTS"))?.back()?.route)
    }

    @Test
    fun menuDestinationUsesFullScreenAndBackReturnsHome() {
        val home = ShellState()
        val destination = home.openMenu().navigate(QuestRoute.QUESTS)
        assertEquals(false, destination.menuOpen)
        assertEquals(QuestRoute.QUESTS, destination.route)
        assertEquals(home, destination.back())
    }

    @Test
    fun backDismissesMenuBeforeChangingRoute() {
        val state = ShellState(route = CompanionRoute.SETTINGS).openMenu()
        assertEquals(ShellState(route = CompanionRoute.SETTINGS), state.back())
        assertEquals(ShellState(), state.back().back())
    }

    @Test
    fun oldDrawerSavedStateRestoresSafelyAfterNavigationUpgrade() {
        assertEquals(
            ShellState(),
            ShellSaver.restore(listOf("VEHICLE", "CLOSED")),
        )
        assertEquals(
            ShellState(route = QuestRoute.QUESTS),
            ShellSaver.restore(listOf("PET", "QUESTS")),
        )
    }
}
