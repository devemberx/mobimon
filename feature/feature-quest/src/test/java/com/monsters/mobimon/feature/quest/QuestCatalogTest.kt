package com.monsters.mobimon.feature.quest

import com.monsters.mobimon.core.domain.CosmeticInventory
import com.monsters.mobimon.core.domain.CosmeticSlot
import com.monsters.mobimon.core.domain.DefaultPointQuestCatalog
import com.monsters.mobimon.core.domain.DrivingQuestIds
import com.monsters.mobimon.core.domain.PointQuestCatalog
import com.monsters.mobimon.core.domain.PointQuestSchedule
import com.monsters.mobimon.core.presentation.CompanionAppearanceState
import com.monsters.mobimon.core.presentation.PointBalanceState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestCatalogTest {
    private val definitions = DefaultPointQuestCatalog()
    private val catalog = QuestCatalog(definitions)

    @Test
    fun rewardAndScheduleComeFromInjectedCatalog() {
        val overridden =
            QuestCatalog(
                PointQuestCatalog { id ->
                    definitions.find(id)?.let {
                        if (id ==
                            DrivingQuestIds.SEATBELT
                        ) {
                            it.copy(rewardPoints = 91, schedule = PointQuestSchedule.Weekly("UTC"))
                        } else {
                            it
                        }
                    }
                },
            )
        val item =
            overridden
                .present(
                    ready(),
                    CompanionAppearanceState(),
                    PointBalanceState.Ready(0),
                    true,
                    Int::toString,
                ).quests
                .first()
        assertEquals(DrivingQuestIds.SEATBELT, item.id)
        assertEquals(91L, item.rewardPoints)
        assertEquals(R.string.quest_schedule_weekly_short.toString(), item.scheduleText)
    }

    @Test
    fun drivingAndHiddenQuestsUseSeparatePresentationFromCommittedAppearance() {
        val appearance =
            CompanionAppearanceState(
                CosmeticInventory(
                    emptySet(),
                    mapOf(
                        CosmeticSlot.FRIEND to "friend:luna",
                        CosmeticSlot.ACCESSORY to "accessory:luna-cap",
                        CosmeticSlot.BACKGROUND to "background:star",
                    ),
                ),
            )
        val state = catalog.present(ready(), appearance, PointBalanceState.Ready(0), true, Int::toString)
        assertEquals(14, state.quests.size)
        assertEquals(
            setOf(
                DrivingQuestIds.HIDDEN_COSTUME,
                DrivingQuestIds.HIDDEN_BACKGROUND,
                DrivingQuestIds.HIDDEN_NEW_FRIEND,
            ),
            state.hiddenQuests
                .map {
                    it.id
                }.toSet(),
        )
        assertFalse(state.quests.any { it.id == "q01" })
        val completed =
            catalog.present(
                ready().copy(completedPointQuestIds = setOf(DrivingQuestIds.HIDDEN_NEW_FRIEND)),
                appearance,
                PointBalanceState.Ready(30),
                true,
                Int::toString,
            )
        assertFalse(completed.hiddenQuests.any { it.id == DrivingQuestIds.HIDDEN_NEW_FRIEND })
    }

    @Test
    fun eligibilityDoesNotAuthorizeClaimsWhenParkingOrObservationIsUnavailable() {
        val eligible = ready().copy(satisfiedDrivingQuestIds = setOf(DrivingQuestIds.SEATBELT))
        for (state in listOf(
            eligible.copy(isLoading = true),
            eligible.copy(observationFailed = true),
            eligible.copy(pendingQuestId = DrivingQuestIds.SAFE_DRIVE),
        )) {
            val screen =
                catalog.present(
                    state,
                    CompanionAppearanceState(),
                    PointBalanceState.Ready(0),
                    true,
                    Int::toString,
                )
            assertFalse(screen.canClaim)
        }
        val unparked =
            catalog.present(
                eligible,
                CompanionAppearanceState(),
                PointBalanceState.Ready(0),
                false,
                Int::toString,
            )
        assertFalse(unparked.canClaim)
        assertEquals(QuestItemStatus.CLAIMABLE, unparked.quests.first().status)
        val parked =
            catalog.present(
                eligible,
                CompanionAppearanceState(),
                PointBalanceState.Ready(0),
                true,
                Int::toString,
            )
        assertTrue(parked.canClaim)
    }

    @Test
    fun questsAreOrderedByClaimableThenInProgressThenCompleted() {
        val state =
            ready().copy(
                satisfiedDrivingQuestIds = setOf(DrivingQuestIds.TIRE_CHECK, DrivingQuestIds.MAINTENANCE),
                completedPointQuestIds = setOf(DrivingQuestIds.SEATBELT),
            )
        val screen =
            catalog.present(
                state,
                CompanionAppearanceState(),
                PointBalanceState.Ready(0),
                true,
                Int::toString,
            )
        val quests = screen.quests
        assertEquals(14, quests.size)

        // Claimable quests come first in their catalog order
        assertEquals(DrivingQuestIds.MAINTENANCE, quests[0].id)
        assertEquals(QuestItemStatus.CLAIMABLE, quests[0].status)
        assertEquals(DrivingQuestIds.TIRE_CHECK, quests[1].id)
        assertEquals(QuestItemStatus.CLAIMABLE, quests[1].status)

        // In-progress quests come next
        val inProgressQuests = quests.subList(2, 13)
        assertTrue(inProgressQuests.all { it.status == QuestItemStatus.IN_PROGRESS })
        assertEquals(DrivingQuestIds.SAFE_DRIVE, inProgressQuests.first().id)

        // Completed quests come last
        assertEquals(DrivingQuestIds.SEATBELT, quests.last().id)
        assertEquals(QuestItemStatus.COMPLETED, quests.last().status)
    }

    private fun ready() = QuestUiState(isLoading = false)
}
