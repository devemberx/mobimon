package com.monsters.mobimon

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.di.AppDependencies
import com.monsters.mobimon.testing.JourneyStorage
import com.monsters.mobimon.testing.JourneyVehicle
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import com.monsters.mobimon.core.ui.R as CoreUiR
import com.monsters.mobimon.feature.pet.R as PetR
import com.monsters.mobimon.feature.quest.R as QuestR
import com.monsters.mobimon.feature.vehicle.R as VehicleR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class Q01AppJourneyTest {
    @get:Rule(order = 0)
    val hilt = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val compose = createEmptyComposeRule()

    @Inject lateinit var vehicle: JourneyVehicle

    @Inject lateinit var storage: JourneyStorage

    @Inject lateinit var dependencies: AppDependencies

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        hilt.inject()
    }

    @After
    fun tearDown() {
        if (::storage.isInitialized) storage.close()
    }

    @Test
    fun q01AwardsOnceThroughUiAndSurvivesActivityRecreation() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            openQuests()
            waitFor(hasText(text(QuestR.string.quest_start_q01)) and isEnabled())
            clickScrollable(QuestR.string.quest_start_q01)
            waitFor(hasText(text(QuestR.string.quest_open_vehicle)))
            clickScrollable(QuestR.string.quest_open_vehicle)
            waitFor(hasText(text(VehicleR.string.vehicle_q01_waiting)))
            compose.onNodeWithText(text(VehicleR.string.vehicle_acknowledge)).assertIsNotEnabled()

            vehicle.publish()
            val evidenceId = vehicle.snapshots.value.id
            waitFor(hasText(text(VehicleR.string.vehicle_acknowledge)) and isEnabled())
            clickScrollable(VehicleR.string.vehicle_acknowledge)
            waitFor(hasText(text(VehicleR.string.vehicle_q01_completed)))
            compose.onNodeWithText(text(VehicleR.string.vehicle_acknowledge)).assertIsNotEnabled()
            val completion =
                runBlocking(Dispatchers.IO) {
                    withTimeout(5_000) {
                        val progress = dependencies.quests.progress.first()
                        assertNull(progress.activeRun)
                        assertEquals(1, progress.completions.size)
                        assertEquals(
                            80,
                            dependencies.pets.profile
                                .first()
                                .totalXp,
                        )
                        progress.completions.single().also { assertEquals(evidenceId, it.snapshotId) }
                    }
                }

            compose.onNodeWithText(text(R.string.close)).performClick()
            assertHomeXp(80)
            scenario.recreate()
            assertHomeXp(80)
            openQuests()
            waitFor(hasText(text(QuestR.string.quest_reward_received, 80)))
            compose.onNodeWithText(text(QuestR.string.quest_start_q01)).assertDoesNotExist()
            compose.onNodeWithText(text(QuestR.string.quest_cancel)).assertDoesNotExist()
            runBlocking(Dispatchers.IO) {
                withTimeout(5_000) {
                    assertEquals(
                        listOf(completion),
                        dependencies.quests.progress
                            .first()
                            .completions,
                    )
                    assertEquals(
                        80,
                        dependencies.pets.profile
                            .first()
                            .totalXp,
                    )
                }
            }
        }
    }

    @Test
    fun unavailableVehicleBlocksStartUntilParked() {
        assertBlockedUntilParked(DrivingState.UNKNOWN, SignalQuality.UNAVAILABLE)
    }

    @Test
    fun unknownDrivingStateBlocksStartUntilParked() {
        assertBlockedUntilParked(DrivingState.UNKNOWN, SignalQuality.VALID)
    }

    @Test
    fun movingVehicleBlocksStartUntilParked() {
        assertBlockedUntilParked(DrivingState.MOVING, SignalQuality.VALID)
    }

    private fun assertBlockedUntilParked(
        state: DrivingState,
        quality: SignalQuality,
    ) {
        vehicle.publish(state, quality)
        ActivityScenario.launch(MainActivity::class.java).use {
            openQuests()
            waitFor(hasText(text(QuestR.string.quest_start_q01)) and !isEnabled())
            compose.onNodeWithText(text(QuestR.string.quest_start_q01)).performScrollTo().assertIsNotEnabled()
            compose.onNodeWithText(text(QuestR.string.quest_start_q01)).performClick()
            runBlocking(Dispatchers.IO) {
                withTimeout(5_000) {
                    assertNull(
                        dependencies.quests.progress
                            .first()
                            .activeRun,
                    )
                    assertTrue(
                        dependencies.quests.progress
                            .first()
                            .completions
                            .isEmpty(),
                    )
                    assertEquals(
                        0,
                        dependencies.pets.profile
                            .first()
                            .totalXp,
                    )
                }
            }
            vehicle.publish()
            waitFor(hasText(text(QuestR.string.quest_start_q01)) and isEnabled())
            clickScrollable(QuestR.string.quest_start_q01)
            waitFor(hasText(text(QuestR.string.quest_open_vehicle)))
            runBlocking(Dispatchers.IO) {
                withTimeout(5_000) {
                    assertEquals(
                        "journey-profile",
                        dependencies.quests.progress
                            .first()
                            .activeRun
                            ?.profileId,
                    )
                    assertEquals(
                        0,
                        dependencies.pets.profile
                            .first()
                            .totalXp,
                    )
                }
            }
        }
    }

    private fun openQuests() {
        waitFor(hasContentDescription(text(PetR.string.pet_open_menu)))
        compose.onNodeWithContentDescription(text(PetR.string.pet_open_menu)).performScrollTo().performClick()
        clickScrollable(R.string.drawer_quests)
    }

    private fun assertHomeXp(xp: Int) {
        val label = text(CoreUiR.string.mobimon_total_xp, xp)
        waitFor(hasText(label))
        compose.onNodeWithText(label).performScrollTo().assertIsDisplayed()
    }

    private fun clickScrollable(resource: Int) {
        compose.onNodeWithText(text(resource)).performScrollTo().performClick()
    }

    private fun waitFor(matcher: SemanticsMatcher) {
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodes(matcher).fetchSemanticsNodes().size == 1
        }
    }

    private fun text(
        resource: Int,
        vararg args: Any,
    ): String = context.getString(resource, *args)
}
