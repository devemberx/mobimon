package com.monsters.mobimon.feature.vehicle

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.core.domain.VehicleWarning
import com.monsters.mobimon.core.domain.WarningSeverity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ko-rKR-w2560dp-h1248dp-mdpi")
class VehicleInfoScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun unavailableSnapshotDoesNotInventParkedStateOrBattery() {
        render(snapshot(quality = SignalQuality.UNAVAILABLE, drivingState = DrivingState.UNKNOWN, battery = null))

        val unconfirmedParkingBadges =
            compose.onAllNodes(hasContentDescription("주차 후 이용"), useUnmergedTree = true).fetchSemanticsNodes()

        assertTrue(
            "Unverified parking should remain visible in the header",
            unconfirmedParkingBadges.any { it.boundsInRoot.top < 160f },
        )
        compose.onNodeWithText("배터리 정보 없음").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun missingReadingsDoNotClaimHealthyVehicleOrCheckedAssistance() {
        render(snapshot(battery = null))

        compose.onNodeWithText("차량의 상태가 좋아요").assertDoesNotExist()
        compose.onNodeWithText("저압 경고 없음").assertDoesNotExist()
        compose.onNodeWithText("주의 경고 없음").assertDoesNotExist()
        compose.onNodeWithText("타이어 정보 없음").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("운전자 보조 정보 없음").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun missingDefaultCardSignalsDoNotClaimGlobalHealth() {
        render(
            snapshot().copy(
                tirePressureStatus = "OK",
                isEmergencyBraking = false,
                isDrowsy = false,
                isDistracted = false,
                attentionLevel = 85,
            ),
        )

        compose.onNodeWithText("차량의 상태가 좋아요").assertDoesNotExist()
        compose.onNodeWithText("일부 정보만 확인했어요").assertIsDisplayed()
    }

    @Test
    fun partialAssistReadingsDoNotEstablishNoWarnings() {
        render(snapshot().copy(isEmergencyBraking = false, isDrowsy = false))

        compose.onNodeWithText("주의 경고 없음").assertDoesNotExist()
        compose.onNodeWithText("운전자 보조 정보 없음").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun uninterpretedTireStatusDoesNotEstablishNoWarnings() {
        render(snapshot().copy(tirePressureStatus = "NG"))

        compose.onNodeWithText("저압 경고").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("저압 경고 없음").assertDoesNotExist()
    }

    @Test
    fun staleAuxiliaryReadingsDoNotRemainCurrent() {
        render(
            snapshot(quality = SignalQuality.STALE).copy(
                tirePressureStatus = "정상",
                isEmergencyBraking = false,
                isDrowsy = false,
                isDistracted = false,
                attentionLevel = 85,
                outsideTemperature = 18,
                isRaining = false,
            ),
        )

        compose.onNodeWithText("정상").assertDoesNotExist()
        compose.onNodeWithText("85").assertDoesNotExist()
        compose.onNodeWithText("18°").assertDoesNotExist()
        compose.onNodeWithText("저압 경고 없음").assertDoesNotExist()
        compose.onNodeWithText("주의 경고 없음").assertDoesNotExist()
    }

    @Test
    fun explicitlyCheckedReadingsRemainVisible() {
        render(
            snapshot().copy(
                tirePressureStatus = "정상",
                isEmergencyBraking = false,
                isDrowsy = false,
                isDistracted = false,
                attentionLevel = 85,
                outsideTemperature = 18,
                isRaining = false,
            ),
        )

        compose.onNodeWithText("정상").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("주의 경고 없음").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("18°").performScrollTo().assertIsDisplayed()
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
        compose.onNodeWithText("주의").assertDoesNotExist()
    }

    @Test
    fun headerShowsParkedConfirmedBadgeWhenParkingIsVerified() {
        render(snapshot())

        val confirmedParkingBadges =
            compose.onAllNodes(hasContentDescription("주차 확인됨"), useUnmergedTree = true).fetchSemanticsNodes()

        assertTrue(
            "Verified parking badge should be visible in the header",
            confirmedParkingBadges.any { it.boundsInRoot.top < 160f },
        )
    }

    @Test
    fun headerShowsDrivingBadgeWhenVehicleIsMoving() {
        render(snapshot(drivingState = DrivingState.MOVING))

        val movingBadges =
            compose.onAllNodes(hasContentDescription("주차 후 이용"), useUnmergedTree = true).fetchSemanticsNodes()

        assertTrue(
            "Moving vehicle state should be visible in the header",
            movingBadges.any { it.boundsInRoot.top < 160f },
        )
        assertEquals(36f, movingBadges.single().boundsInRoot.top, 1f)
        assertEquals(2488f, movingBadges.single().boundsInRoot.right, 1f)
    }

    @Test
    fun headerSubtitleTracksSelectedCompanion() {
        var friendId by mutableStateOf("friend:mobi")
        compose.setContent {
            MaterialTheme {
                VehicleInfoScreen(snapshot = snapshot(), friendId = friendId)
            }
        }

        compose.onNodeWithText("모비의 상태로 내 차를 한눈에").assertIsDisplayed()

        compose.runOnIdle { friendId = "friend:luna" }

        compose.onNodeWithText("루나의 상태로 내 차를 한눈에").assertIsDisplayed()
        compose.onNodeWithText("모비의 상태로 내 차를 한눈에").assertDoesNotExist()
    }

    @Test
    fun headerBackButtonCallsOnBackAndHomeButtonDoesNotExist() {
        var backClicked = false
        compose.setContent {
            MaterialTheme {
                VehicleInfoScreen(
                    snapshot = snapshot(),
                    onBack = { backClicked = true },
                )
            }
        }

        compose.onNodeWithTag("vehicle-header-back-button").performClick()
        assertTrue("Back callback invoked", backClicked)
        compose.onNodeWithTag("vehicle-header-home-button").assertDoesNotExist()
    }

    @Test
    fun longPressOpensFullCardChoicesAndConfirmChangesOnlyTheSelectedSlot() {
        var selections by mutableStateOf(VehicleCardCatalog.defaultSlots.map { it.id })
        compose.setContent {
            MaterialTheme {
                VehicleInfoScreen(
                    snapshot = snapshot(),
                    selectedCards = selections,
                    onCardSelectionConfirmed = { selections = it },
                )
            }
        }

        compose.onNodeWithTag("vehicle-card-slot-1").performTouchInput { longClick() }
        compose.onNodeWithText("차량상태 카드 변경").assertIsDisplayed()
        compose.onNodeWithTag("vehicle-dialog-option-battery").assertDoesNotExist()
        compose.onNodeWithTag("vehicle-dialog-confirm").assertIsNotEnabled()
        compose.onNodeWithTag("vehicle-dialog-option-battery-health").performScrollTo().performClick()
        compose.onNodeWithTag("vehicle-dialog-confirm").performClick()

        compose.runOnIdle {
            assertEquals("battery-health", selections[0])
            assertEquals("charging", selections[1])
        }
        compose.onNodeWithText("배터리 건강도").assertExists()
        assertTrue(compose.onAllNodesWithText("정보 없음").fetchSemanticsNodes().isNotEmpty())

        compose.onNodeWithTag("vehicle-card-slot-1").performTouchInput { longClick() }
        compose.onNodeWithTag("vehicle-dialog-option-battery-health").assertDoesNotExist()
        compose.onNodeWithTag("vehicle-dialog-option-battery").assertIsDisplayed()
    }

    @Test
    fun cancelKeepsPreviousCard() {
        var selections by mutableStateOf(VehicleCardCatalog.defaultSlots.map { it.id })
        compose.setContent {
            MaterialTheme {
                VehicleInfoScreen(snapshot = snapshot(), selectedCards = selections, onCardSelectionConfirmed = {
                    selections =
                        it
                })
            }
        }
        compose.onNodeWithTag("vehicle-card-slot-1").performTouchInput { longClick() }
        compose.onNodeWithTag("vehicle-dialog-option-battery-health").performScrollTo().performClick()
        compose.onNodeWithTag("vehicle-dialog-cancel").performClick()
        compose.runOnIdle { assertEquals("battery", selections[0]) }
    }

    @Test
    fun galleryExcludesAllAssignedCardsAndClearsDraftWhenSlotChanges() {
        val assigned = listOf("battery", "battery-health", "tire", "washer", "environment", "assist")
        compose.setContent {
            MaterialTheme {
                VehicleInfoScreen(snapshot = snapshot(), selectedCards = assigned)
            }
        }

        compose.onNodeWithTag("vehicle-card-slot-1").performTouchInput { longClick() }
        compose.onNodeWithTag("vehicle-dialog-option-battery").assertDoesNotExist()
        compose.onNodeWithTag("vehicle-dialog-option-battery-health").assertDoesNotExist()
        compose.onNodeWithText("28개 · 실제 크기 · 아래로 스크롤 ↓").assertIsDisplayed()
        compose.onNodeWithTag("vehicle-dialog-option-battery-range").performScrollTo().performClick()
        compose.onNodeWithTag("vehicle-dialog-slot-2").performClick()
        compose.onNodeWithTag("vehicle-dialog-confirm").assertIsNotEnabled()
    }

    @Test
    fun lastGalleryCardCanReplaceTheSixthSlotAfterScrolling() {
        var selections by mutableStateOf(VehicleCardCatalog.defaultSlots.map { it.id })
        compose.setContent {
            MaterialTheme {
                VehicleInfoScreen(
                    snapshot = snapshot(),
                    selectedCards = selections,
                    onCardSelectionConfirmed = { selections = it },
                )
            }
        }

        compose.onNodeWithTag("vehicle-card-slot-6").performTouchInput { longClick() }
        compose.onNodeWithTag("vehicle-dialog-list").performScrollToIndex(28)
        compose.onNodeWithTag("vehicle-dialog-option-breakdown").assertIsDisplayed().performClick()
        compose.onNodeWithTag("vehicle-dialog-confirm").performClick()

        compose.runOnIdle {
            assertEquals("battery", selections[0])
            assertEquals("breakdown", selections[5])
        }
    }

    @Test
    fun selectorOpensAtFirstCardWithinTheContentArea() {
        render(snapshot())
        compose.onNodeWithTag("vehicle-card-slot-1").performTouchInput { longClick() }

        val dialog = compose.onNodeWithTag("vehicle-card-selector").getUnclippedBoundsInRoot()
        assertTrue(dialog.left.value >= 0f)
        assertTrue(dialog.right.value <= 2560f)
        assertTrue(dialog.top.value >= 0f)
        assertTrue(dialog.bottom.value <= 1248f)
        compose.onNodeWithTag("vehicle-dialog-option-battery").assertDoesNotExist()
        compose.onNodeWithTag("vehicle-dialog-option-battery-health").assertIsDisplayed()
        compose.onNodeWithText("29개 · 실제 크기 · 아래로 스크롤 ↓").assertIsDisplayed()
        compose.onNodeWithTag("vehicle-dialog-confirm").assertIsNotEnabled()
    }

    @Test
    fun verifiedTireWarningUsesSickCharacterCopy() {
        render(
            snapshot().copy(
                tirePressureStatus = "NG",
                warnings =
                    listOf(
                        VehicleWarning(
                            item = "타이어",
                            severity = WarningSeverity.CAUTION,
                            description = "타이어 공기압 확인이 필요해요.",
                            nextAction = "점검",
                            observedAtMillis = 200,
                        ),
                    ),
            ),
        )

        compose.onNodeWithText("아픔").assertIsDisplayed()
        compose.onNodeWithText("모비가 아파요").assertIsDisplayed()
        assertTrue(compose.onAllNodesWithText("타이어 공기압 확인이 필요해요.").fetchSemanticsNodes().isNotEmpty())
    }

    @Test
    fun verifiedLowBatteryUsesHungryCharacterCopy() {
        render(snapshot(battery = 18))

        compose.onNodeWithText("배고픔").assertIsDisplayed()
        compose.onNodeWithText("모비가 배고파요").assertIsDisplayed()
    }

    @Test
    fun lunaDisplaysHungryAndSickState() {
        compose.setContent {
            MaterialTheme {
                VehicleInfoScreen(
                    snapshot = snapshot(battery = 15),
                    friendId = "friend:luna",
                )
            }
        }
        compose.onNodeWithText("배고픔").assertIsDisplayed()
        compose.onNodeWithText("루나가 배고파요").assertIsDisplayed()
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
        speed = 0,
        gear = "P",
    )
}
