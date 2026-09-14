package com.monsters.mobimon.feature.vehicle

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.core.domain.VehicleWarning
import com.monsters.mobimon.core.domain.WarningSeverity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ko-rKR")
class VehicleInfoScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun unavailableSnapshotDoesNotInventParkedStateOrBattery() {
        render(snapshot(quality = SignalQuality.UNAVAILABLE, drivingState = DrivingState.UNKNOWN, battery = null))

        compose.onNodeWithText("주차 여부 확인 불가").assertIsDisplayed()
        compose.onNodeWithText("배터리 정보 없음").assertIsDisplayed()
    }

    @Test
    fun staleSnapshotShowsIndependentSignalAges() {
        render(
            snapshot(quality = SignalQuality.STALE).copy(
                parkingAgeMillis = 19_000,
                batteryAgeMillis = 61_000,
            ),
        )

        compose.onNodeWithText("배터리 정보가 오래되었어요").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("마지막 확인: 19초 전").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("마지막 확인: 1분 전").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun freshBatteryRemainsVisibleWhenParkingIsStale() {
        render(snapshot(quality = SignalQuality.STALE).copy(batteryQuality = SignalQuality.VALID))

        compose.onNodeWithText("배터리 67%").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun staleWarningIsMarkedHistoricalAndDoesNotOfferCurrentAction() {
        val warning =
            VehicleWarning(
                item = "앞바퀴",
                location = "왼쪽",
                severity = WarningSeverity.CAUTION,
                description = "압력 기록",
                nextAction = "지금 점검",
                observedAtMillis = 100,
                quality = SignalQuality.STALE,
            )
        render(snapshot().copy(warnings = listOf(warning)))
        compose.onNodeWithText("이전 경고 · 주의 · 앞바퀴").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("지금 점검").assertDoesNotExist()
    }

    private fun render(snapshot: VehicleSnapshot) {
        compose.setContent {
            MaterialTheme {
                VehicleInfoScreen(
                    snapshot = snapshot,
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
