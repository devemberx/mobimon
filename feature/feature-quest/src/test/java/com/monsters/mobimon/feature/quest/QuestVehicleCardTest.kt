package com.monsters.mobimon.feature.quest

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.VehicleSnapshot
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ko-rKR")
class QuestVehicleCardTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun unavailableSnapshotDoesNotInventParkedStateOrBattery() {
        render(snapshot(quality = SignalQuality.UNAVAILABLE, drivingState = DrivingState.UNKNOWN, battery = null))

        compose.onNodeWithText("상태 확인 완료").assertIsNotEnabled()
    }

    @Test
    fun movingVehicleCannotBeAcknowledgedEvenWhenCallerEnablesIt() {
        render(snapshot(drivingState = DrivingState.MOVING))

        compose.onNodeWithText("상태 확인 완료").assertIsNotEnabled()
    }

    @Test
    fun staleSnapshotCannotBeAcknowledged() {
        render(
            snapshot(quality = SignalQuality.STALE).copy(
                parkingAgeMillis = 19_000,
                batteryAgeMillis = 61_000,
            ),
        )

        compose.onNodeWithText("상태 확인 완료").assertIsNotEnabled()
    }

    @Test
    fun callerCanRejectOtherwiseValidSnapshotAtQuestBoundary() {
        render(snapshot(), canAcknowledge = false)

        compose.onNodeWithText("상태 확인 완료").assertIsNotEnabled()
    }

    @Test
    fun acknowledgmentUsesCurrentlyDisplayedSnapshotAfterUpdate() {
        val current = mutableStateOf(snapshot().copy(id = "first"))
        var acknowledged: String? = null
        compose.setContent {
            MaterialTheme {
                QuestVehicleCard(
                    snapshot = current.value,
                    questActive = true,
                    questCompleted = false,
                    canAcknowledge = true,
                    onAcknowledge = { acknowledged = it },
                )
            }
        }

        compose.runOnIdle { current.value = current.value.copy(id = "displayed", sequence = 3) }
        compose.onNodeWithText("상태 확인 완료").performClick()

        assertEquals("displayed", acknowledged)
    }

    private fun render(
        snapshot: VehicleSnapshot,
        canAcknowledge: Boolean = true,
    ) {
        compose.setContent {
            MaterialTheme {
                QuestVehicleCard(
                    snapshot = snapshot,
                    questActive = true,
                    questCompleted = false,
                    canAcknowledge = canAcknowledge,
                    onAcknowledge = {},
                )
            }
        }
    }

    private fun snapshot(
        quality: SignalQuality = SignalQuality.VALID,
        drivingState: DrivingState = DrivingState.PARKED,
        battery: Int? = 67,
    ) = VehicleSnapshot(
        id = "snapshot-2",
        epoch = "epoch-1",
        sequence = 2,
        receivedAtMillis = 200,
        source = SignalSource.SIMULATED,
        drivingState = drivingState,
        quality = quality,
        batteryPercent = battery,
    )
}
