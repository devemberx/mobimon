package com.monsters.mobimon.feature.pet

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.monsters.mobimon.core.domain.CompanionSettings
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.PetProfile
import com.monsters.mobimon.core.domain.QuestProgress
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.core.domain.VehicleWarning
import com.monsters.mobimon.core.domain.WarningSeverity
import com.monsters.mobimon.core.ui.MobiMonTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ko-rKR-w1000dp-h700dp")
class PetHomeScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun friendNameIsAccessibleWithoutAnExtraVisibleCaption() {
        render(snapshot = parkedSnapshot())
        compose.onNodeWithContentDescription("Mobi 강아지").assertExists()
        compose.onNodeWithText("Mobi · 강아지").assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko-rKR-w2560dp-h1440dp")
    fun landscapeCompositionKeepsSummaryCompactAndCompanionCentral() {
        assertLandscapeComposition()
    }

    @Test
    @Config(qualifiers = "ko-rKR-w1792dp-h888dp")
    fun headUnitContentWindowKeepsTheProportionalComposition() {
        assertLandscapeComposition()
    }

    @Test
    @Config(qualifiers = "ko-rKR-w1600dp-h1200dp")
    fun tallerWindowLetterboxesWithoutStretchingTheContent() {
        assertLandscapeComposition()
    }

    @Test
    @Config(qualifiers = "ko-rKR-w2560dp-h1440dp")
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun moderatelyEnlargedParkingTextFitsInsideTheBadge() {
        render(snapshot = parkedSnapshot(), pointBalance = 0, fontScale = 1.2f)
        compose.onNodeWithTag("home-composition").assertExists()
        compose
            .onNodeWithText("P · 주차 중", useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) {
                val results = mutableListOf<TextLayoutResult>()
                it(results)
                val result = results.single()
                assertEquals(1, result.lineCount)
                assertTrue(result.multiParagraph.height <= result.layoutInput.constraints.maxHeight)
                assertTrue(result.multiParagraph.width <= result.layoutInput.constraints.maxWidth)
            }
    }

    private fun assertLandscapeComposition() {
        val calls = mutableListOf<String>()
        render(
            snapshot = parkedSnapshot(),
            pointBalance = 0,
            onMenu = { calls += "menu" },
            onAppearance = { calls += "appearance" },
            onDetails = { calls += "details" },
        )
        val window = compose.onRoot().fetchSemanticsNode().boundsInRoot
        val summary = compose.onNodeWithTag("home-vehicle-summary").fetchSemanticsNode().boundsInRoot
        val friend = compose.onNodeWithContentDescription("Mobi 강아지").fetchSemanticsNode().boundsInRoot
        val parking = compose.onNodeWithContentDescription("주차 확인됨").fetchSemanticsNode().boundsInRoot
        val content = compose.onNodeWithTag("home-composition").fetchSemanticsNode().boundsInRoot
        val customization = compose.onNodeWithText("꾸미기").fetchSemanticsNode().boundsInRoot
        assertTrue(summary.width < window.width * 0.56f)
        assertTrue(summary.height < window.height * 0.12f)
        assertEquals(window.center.x, friend.center.x, 2f)
        assertEquals(2560f / 1268f, content.width / content.height, 0.01f)
        assertEquals(friend.width, friend.height, 1f)
        assertTrue(friend.bottom < summary.top)
        assertTrue(parking.height < customization.height)
        compose
            .onNodeWithText(
                "P · 주차 중",
                useUnmergedTree = true,
            ).performSemanticsAction(SemanticsActions.GetTextLayoutResult) {
                val results = mutableListOf<TextLayoutResult>()
                it(results)
                val text = results.single().layoutInput
                assertEquals(parking.width * 28f / 344f, text.style.fontSize.value * text.density.density, 1f)
            }
        compose.onNodeWithContentDescription("메뉴 열기").assertHeightIsAtLeast(76.dp).performClick()
        compose.onNodeWithText("꾸미기").assertHeightIsAtLeast(76.dp).performClick()
        compose.onNodeWithText("상태 보기").assertHeightIsAtLeast(76.dp).performClick()
        compose.onNodeWithText("대화하기").assertIsDisplayed().assertIsNotEnabled()
        compose.onNodeWithText("AI 연결 불가").assertIsDisplayed()
        assertEquals(listOf("menu", "appearance", "details"), calls)
    }

    @Test
    @Config(qualifiers = "ko-rKR-w1792dp-h888dp")
    fun losingAndRecoveringVehicleDataKeepsHomeGeometryStable() {
        val snapshot = mutableStateOf(parkedSnapshot())
        render(pointBalance = 0, snapshotSource = { snapshot.value })
        val friend = compose.onNodeWithContentDescription("Mobi 강아지").fetchSemanticsNode().boundsInRoot
        val summary = compose.onNodeWithTag("home-vehicle-summary").fetchSemanticsNode().boundsInRoot
        compose.runOnIdle {
            snapshot.value =
                snapshot.value.copy(
                    quality = SignalQuality.UNAVAILABLE,
                    batteryQuality = SignalQuality.UNAVAILABLE,
                    drivingState = DrivingState.UNKNOWN,
                )
        }
        assertEquals(friend, compose.onNodeWithContentDescription("Mobi 강아지").fetchSemanticsNode().boundsInRoot)
        assertEquals(summary, compose.onNodeWithTag("home-vehicle-summary").fetchSemanticsNode().boundsInRoot)
        compose.onNodeWithText("확인할 수 있는 배터리 정보가 없어요.").assertIsDisplayed()
        compose.onNodeWithText("주차 여부 확인 불가").assertIsDisplayed()
        compose.onNodeWithText("배터리 72%").assertDoesNotExist()
        compose.onNodeWithText("대화하기").assertIsNotEnabled()
        compose.runOnIdle { snapshot.value = parkedSnapshot() }
        assertEquals(summary, compose.onNodeWithTag("home-vehicle-summary").fetchSemanticsNode().boundsInRoot)
        compose.onNodeWithText("배터리 72%").assertIsDisplayed()
    }

    @Test
    fun realUnavailableHomeDoesNotClaimParkedState() {
        render(vehiclePreview = false, showOnVehicleHome = true)

        compose.onNodeWithText("주차 여부 확인 불가").performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription("주차 확인됨").assertDoesNotExist()
    }

    @Test
    fun hiddenPetPreferenceRemovesConversationTargetOnlyOnVehiclePreview() {
        render(vehiclePreview = true, showOnVehicleHome = false)

        compose.onNodeWithContentDescription("친구와 대화하기").assertDoesNotExist()
        compose.onNodeWithText("차량 홈 미리보기에서 친구를 숨겼어요").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun conversationRemainsUnavailableEvenWhenParked() {
        render(snapshot = parkedSnapshot(), interactionAllowed = true)

        compose.onNodeWithText("대화하기 · 연결 불가").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithContentDescription("친구와 대화하기").assertDoesNotExist()
    }

    @Test
    fun homeActionsForwardToShell() {
        val calls = mutableListOf<String>()
        render(
            onMenu = { calls += "menu" },
            onDetails = { calls += "details" },
            onAppearance = { calls += "appearance" },
        )

        compose.onNodeWithContentDescription("메뉴 열기").performClick()
        compose.onNodeWithText("꾸미기").performScrollTo().performClick()
        compose.onNodeWithContentDescription("차량 정보 자세히 보기").performScrollTo().performClick()
        assertEquals(listOf("menu", "appearance", "details"), calls)
    }

    @Test
    fun staleParkingKeepsFreshBatteryAndDoesNotClaimParked() {
        render(snapshot = parkedSnapshot().copy(quality = SignalQuality.STALE, batteryQuality = SignalQuality.VALID))

        compose.onNodeWithText("주차 여부 확인 불가").assertExists()
        compose.onNodeWithText("배터리 72%").performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription("주차 확인됨").assertDoesNotExist()
        compose.onNodeWithText("주차 정보가 오래되어 현재 상태를 알 수 없어요.").assertExists()
    }

    @Test
    fun loadingBalanceIsNotRenderedAsSavedZero() {
        render()
        compose.onNodeWithText("포인트 확인 중").assertExists()
        compose.onNodeWithText("0 P", substring = true).assertDoesNotExist()
    }

    @Test
    fun failedBalanceIsNotRenderedAsSavedZero() {
        render(pointLoadFailed = true)
        compose.onNodeWithText("포인트를 확인할 수 없어요").assertExists()
        compose.onNodeWithText("0 P", substring = true).assertDoesNotExist()
    }

    @Test
    fun unavailableBatteryNeverClaimsAllCheckedItemsAreHealthy() {
        render(snapshot = parkedSnapshot().copy(batteryQuality = SignalQuality.UNAVAILABLE))
        compose.onNodeWithText("확인할 수 있는 배터리 정보가 없어요.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("배터리 72%").assertDoesNotExist()
        compose.onNodeWithText("확인한 차량 항목에 이상이 없어요").assertDoesNotExist()
    }

    @Test
    fun currentWarningIsPrioritizedAlongsideFreshBattery() {
        render(
            snapshot =
                parkedSnapshot().copy(
                    warnings =
                        listOf(
                            VehicleWarning("타이어", "앞 왼쪽", WarningSeverity.CRITICAL, "공기압이 낮아요", "안전하게 점검해 주세요", 1_000),
                        ),
                ),
        )
        compose.onNodeWithText("확인이 필요해요 · 긴급 · 타이어").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("앞 왼쪽").assertExists()
        compose.onNodeWithText("공기압이 낮아요").assertExists()
        compose.onNodeWithText("배터리 72%").assertExists()
    }

    @Test
    fun historicalWarningDoesNotAppearAsCurrentWarning() {
        render(
            snapshot =
                parkedSnapshot().copy(
                    warnings =
                        listOf(
                            VehicleWarning("타이어", null, WarningSeverity.CAUTION, "이전 경고", "점검", 0, SignalQuality.STALE),
                        ),
                ),
        )
        compose.onNodeWithText("이전 경고가 있어요", substring = true).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("확인이 필요해요", substring = true).assertDoesNotExist()
    }

    @Test
    fun simulatedSourceAndCommittedZeroRemainExplicit() {
        render(snapshot = parkedSnapshot(), pointBalance = 0)
        compose.onNodeWithText("시뮬레이션").assertExists()
        compose.onNodeWithText("포인트 0 P").assertExists()
    }

    @Test
    fun emptySelectionDoesNotDisplayAnUnownedDefaultFriend() {
        render(friendId = null, inventoryLoaded = true)
        compose.onNodeWithText("선택한 친구가 없어요", substring = true).performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription("Mobi 강아지").assertDoesNotExist()
    }

    @Test
    fun inventoryLoadingIsDistinctFromEmptySelection() {
        render(friendId = null, inventoryLoaded = false)
        compose.onNodeWithText("소유한 아이템을 확인하고 있어요.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("선택한 친구가 없어요", substring = true).assertDoesNotExist()
    }

    @Test
    fun failedInventoryOffersRetryAndRetainsLastFriend() {
        var retries = 0
        render(inventoryLoadFailed = true, onRetry = { retries++ })
        compose.onNodeWithText("소유한 아이템을 확인할 수 없어요.").assertExists()
        compose.onNodeWithContentDescription("Mobi 강아지").assertExists()
        compose.onNodeWithText("다시 시도").performScrollTo().performClick()
        assertEquals(1, retries)
    }

    @Test
    fun enlargedTextKeepsPrimaryTargetsReachableAndLargeEnough() {
        render(fontScale = 2f)
        compose
            .onNodeWithContentDescription("메뉴 열기")
            .performScrollTo()
            .assertIsDisplayed()
            .assertHeightIsAtLeast(76.dp)
            .assertWidthIsAtLeast(76.dp)
        compose
            .onNodeWithText("꾸미기")
            .performScrollTo()
            .assertIsDisplayed()
            .assertHeightIsAtLeast(76.dp)
        compose
            .onNodeWithContentDescription("차량 정보 자세히 보기")
            .performScrollTo()
            .assertIsDisplayed()
            .assertHeightIsAtLeast(76.dp)
    }

    @OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)
    @Test
    fun keyboardFocusMovesFromMenuToCustomization() {
        render()
        compose.onNodeWithContentDescription("메뉴 열기").performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        compose.onNodeWithContentDescription("메뉴 열기").performKeyInput { pressKey(Key.Tab) }
        compose.onNodeWithText("꾸미기").assertIsFocused()
    }

    @Test
    fun initialProfileFailureOffersAnAccessibleRetry() {
        var retries = 0
        compose.setContent { MobiMonTheme { PetHomeLoadingScreen(true, { retries++ }) } }
        compose.onNodeWithText("친구를 불러오지 못했어요", substring = true).assertExists()
        compose.onNodeWithText("다시 시도").assertHeightIsAtLeast(76.dp).performClick()
        assertEquals(1, retries)
    }

    @Test
    fun initialLoadingDoesNotOfferRetryOrSavedValues() {
        compose.setContent { MobiMonTheme { PetHomeLoadingScreen(false, {}) } }
        compose.onNodeWithText("친구를 불러오고 있어요.").assertIsDisplayed()
        compose.onNodeWithText("다시 시도").assertDoesNotExist()
        compose.onNodeWithText("포인트 0 P").assertDoesNotExist()
    }

    private fun parkedSnapshot() =
        VehicleSnapshot(
            "parked",
            "epoch",
            1,
            1_000,
            SignalSource.SIMULATED,
            DrivingState.PARKED,
            SignalQuality.VALID,
            72,
        )

    private fun render(
        vehiclePreview: Boolean = false,
        showOnVehicleHome: Boolean = true,
        snapshot: VehicleSnapshot =
            parkedSnapshot().copy(
                source = SignalSource.REAL,
                drivingState = DrivingState.UNKNOWN,
                quality = SignalQuality.UNAVAILABLE,
            ),
        interactionAllowed: Boolean = false,
        pointLoadFailed: Boolean = false,
        pointBalance: Long? = null,
        friendId: String? = "friend:mobi",
        inventoryLoaded: Boolean = true,
        inventoryLoadFailed: Boolean = false,
        fontScale: Float = 1f,
        snapshotSource: (() -> VehicleSnapshot)? = null,
        onRetry: () -> Unit = {},
        onMenu: () -> Unit = {},
        onDetails: () -> Unit = {},
        onAppearance: () -> Unit = {},
    ) {
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale)) {
                MobiMonTheme {
                    PetHomeScreen(
                        profile = PetProfile("profile"),
                        snapshot = snapshotSource?.invoke() ?: snapshot,
                        progress = QuestProgress(),
                        settings = CompanionSettings(showOnVehicleHome = showOnVehicleHome),
                        onOpenMenu = onMenu,
                        onOpenVehicleInfo = onDetails,
                        onOpenQuests = {},
                        onSwitchHome = {},
                        onPetClick = {},
                        onOpenAppearance = onAppearance,
                        vehiclePreview = vehiclePreview,
                        interactionAllowed = interactionAllowed,
                        pointLoadFailed = pointLoadFailed,
                        pointBalance = pointBalance,
                        friendId = friendId,
                        inventoryLoaded = inventoryLoaded,
                        inventoryLoadFailed = inventoryLoadFailed,
                        onRetryProfile = onRetry,
                    )
                }
            }
        }
    }
}
