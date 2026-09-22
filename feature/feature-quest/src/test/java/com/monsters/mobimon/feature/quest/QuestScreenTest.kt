package com.monsters.mobimon.feature.quest

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.monsters.mobimon.core.domain.CosmeticInventory
import com.monsters.mobimon.core.domain.CosmeticSlot
import com.monsters.mobimon.core.domain.DefaultPointQuestCatalog
import com.monsters.mobimon.core.domain.DrivingQuestIds
import com.monsters.mobimon.core.navigation.AppRoute
import com.monsters.mobimon.core.navigation.VehicleRoute
import com.monsters.mobimon.core.presentation.CompanionAppearanceState
import com.monsters.mobimon.core.presentation.PointBalanceState
import com.monsters.mobimon.core.ui.MobiMonTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ko-rKR-w2560dp-h1332dp-mdpi")
class QuestScreenTest {
    @get:Rule val compose = createComposeRule()
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val catalog = QuestCatalog(DefaultPointQuestCatalog())

    @Test
    fun claimDoesNotInventCompletionOrSuccessBeforeCommittedResult() {
        var claimedId: String? = null
        render(presentation(friend = "friend:luna"), onClaim = { claimedId = it })
        compose.onNodeWithTag("quest-hidden-btn-claim").performClick()
        assertEquals(DrivingQuestIds.HIDDEN_NEW_FRIEND, claimedId)
        compose.onNodeWithTag("quest-reward-success-modal").assertDoesNotExist()
        compose.onNodeWithTag("quest-hidden-claim-modal").assertIsDisplayed()
    }

    @Test
    fun pendingDisablesHiddenClaim() {
        render(
            presentation(
                QuestUiState(isLoading = false, pendingQuestId = DrivingQuestIds.HIDDEN_NEW_FRIEND),
                friend = "friend:luna",
            ),
        )
        compose.onNodeWithTag("quest-hidden-btn-claim").assertIsNotEnabled()
        compose.onNodeWithTag("quest-hidden-btn-dismiss").assertIsNotEnabled()
        compose.onNodeWithTag("quest-reward-success-modal").assertDoesNotExist()
    }

    @Test
    fun legacyQ01IsNotOfferedAsAPointQuest() {
        render(presentation())
        compose.onNodeWithTag("quest-card-q01").assertDoesNotExist()
        compose.onNodeWithText("놓지마 생명줄!").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("네 바퀴의 균형").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun pendingDisablesDrivingClaim() {
        val state =
            QuestUiState(
                isLoading = false,
                satisfiedDrivingQuestIds = setOf(DrivingQuestIds.SEATBELT),
                pendingQuestId = DrivingQuestIds.SAFE_DRIVE,
            )
        render(presentation(state))
        compose.onNodeWithTag("quest-btn-claim-${DrivingQuestIds.SEATBELT}").performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun filterSurvivesDetailBackAndSavedStateRestoration() {
        val state =
            presentation(QuestUiState(isLoading = false, completedPointQuestIds = setOf(DrivingQuestIds.SEATBELT)))
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            MobiMonTheme { QuestScreen(state, {}, {}, {}, {}, {}, {}, {}) }
        }
        compose.onNodeWithTag("quest-tab-completed").assertIsDisplayed().performClick()
        compose.onNodeWithTag("quest-card-${DrivingQuestIds.SEATBELT}").performScrollTo().performClick()
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithTag("quest-detail-back-button").assertIsDisplayed().performClick()
        compose.onNodeWithTag("quest-tab-completed").assertIsDisplayed().assertIsSelected()
        compose.onNodeWithTag("quest-card-${DrivingQuestIds.SAFE_DRIVE}").assertDoesNotExist()
        compose.onNodeWithText("2026.09.14", substring = true).assertDoesNotExist()
    }

    @Test
    fun drivingDetailExecutesRealNavigationCallback() {
        var route: AppRoute? = null
        render(presentation(), onNavigate = { route = it })
        compose.onNodeWithTag("quest-btn-detail-${DrivingQuestIds.SEATBELT}").performScrollTo().performClick()
        compose.onNodeWithTag("quest-btn-detail-execute").assertIsDisplayed().performClick()
        assertEquals(VehicleRoute.VEHICLE_INFO, route)
    }

    @Test
    fun failedClaimRemainsVisibleAndCanBeRetried() {
        var claims = 0
        val state =
            presentation(
                QuestUiState(isLoading = false, message = QuestMessage.STORAGE_FAILURE),
                friend = "friend:luna",
            )
        render(state, onClaim = { claims++ })
        compose.onNodeWithTag("quest-hidden-claim-modal").assertIsDisplayed()
        compose.onNodeWithTag("quest-reward-success-modal").assertDoesNotExist()
        compose.onNodeWithTag("quest-hidden-btn-claim").performClick()
        assertEquals(1, claims)
    }

    @Test
    fun committedSuccessDisplaysActualPointsAndDismissesThroughOwner() {
        var state by mutableStateOf(
            presentation(
                QuestUiState(isLoading = false, rewardSuccess = QuestRewardSuccess(DrivingQuestIds.SEATBELT, 17)),
            ),
        )
        compose.setContent {
            MobiMonTheme {
                QuestScreen(state, {}, {}, { state = state.copy(rewardSuccess = null) }, {}, {}, {}, {})
            }
        }
        compose.onNodeWithText("17포인트를 획득했어요!!").assertIsDisplayed()
        compose.onNodeWithTag("quest-modal-btn-confirm").performClick()
        compose.onNodeWithTag("quest-reward-success-modal").assertDoesNotExist()
    }

    @Test
    fun hiddenDismissalUsesOwnerStateWithoutClaimingReward() {
        var state by mutableStateOf(presentation(friend = "friend:luna"))
        var dismissed: String? = null
        var claims = 0
        compose.setContent {
            MobiMonTheme {
                QuestScreen(state, { claims++ }, {
                    dismissed = it
                    state = state.copy(hiddenQuests = emptyList())
                }, {}, {}, {}, {}, {})
            }
        }
        compose.onNodeWithTag("quest-hidden-btn-dismiss").performClick()
        assertEquals(DrivingQuestIds.HIDDEN_NEW_FRIEND, dismissed)
        assertEquals(0, claims)
        compose.onNodeWithTag("quest-hidden-claim-modal").assertDoesNotExist()
    }

    @Test
    fun observationAndWalletFailuresExposeTheirOwnRetries() {
        var questRetries = 0
        var walletRetries = 0
        val state =
            presentation(
                QuestUiState(isLoading = false, observationFailed = true),
            ).copy(pointBalance = PointBalanceState.Failed)
        compose.setContent {
            MobiMonTheme { QuestScreen(state, {}, {}, {}, { questRetries++ }, { walletRetries++ }, {}, {}) }
        }
        compose.onNodeWithText("퀘스트 기록 다시 확인").performClick()
        compose.onNodeWithText("포인트 다시 확인").performClick()
        assertEquals(1, questRetries)
        assertEquals(1, walletRetries)
        compose.onNodeWithText("포인트를 확인할 수 없어요").assertIsDisplayed()
    }

    @Test
    fun walletLoadingAndZeroRemainDistinct() {
        var state by mutableStateOf(presentation().copy(pointBalance = PointBalanceState.Loading))
        compose.setContent { MobiMonTheme { QuestScreen(state, {}, {}, {}, {}, {}, {}, {}) } }
        compose.onNodeWithText("포인트 확인 중").assertIsDisplayed()
        compose.runOnIdle { state = state.copy(pointBalance = PointBalanceState.Ready(0)) }
        compose.onNodeWithText("포인트 0 P").assertIsDisplayed()
        compose.onNodeWithText("0P를 받았어요").assertDoesNotExist()
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun renderClaimPendingAndFailureForVisualReview() {
        var state by mutableStateOf(presentation())
        lateinit var view: View
        compose.setContent {
            val currentView = LocalView.current
            SideEffect { view = currentView }
            MobiMonTheme { QuestScreen(state, {}, {}, {}, {}, {}, {}, {}) }
        }
        capture(viewProvider = { view }, name = "quest-list")
        compose.runOnIdle {
            state = presentation(QuestUiState(isLoading = false, pendingQuestId = DrivingQuestIds.SEATBELT))
        }
        capture(viewProvider = { view }, name = "quest-claim-pending")
        compose.runOnIdle {
            state = presentation(QuestUiState(isLoading = false, message = QuestMessage.STORAGE_FAILURE))
        }
        capture(viewProvider = { view }, name = "quest-claim-failure")
    }

    @Test
    fun headerBackButtonCallsOnBackWhenAtRootList() {
        var backCalled = false
        val state = presentation()
        compose.setContent {
            MobiMonTheme {
                QuestScreen(
                    state = state,
                    onClaimReward = {},
                    onDismissHiddenQuest = {},
                    onDismissRewardSuccess = {},
                    onRetryQuests = {},
                    onRetryWallet = {},
                    onRetryAppearance = {},
                    onNavigateRoute = {},
                    onBack = { backCalled = true },
                )
            }
        }
        compose.onNodeWithTag("quest-header-back-button").performClick()
        assertTrue(backCalled)
    }

    @Test
    fun headerHomeButtonDoesNotExist() {
        val state = presentation()
        compose.setContent {
            MobiMonTheme {
                QuestScreen(
                    state = state,
                    onClaimReward = {},
                    onDismissHiddenQuest = {},
                    onDismissRewardSuccess = {},
                    onRetryQuests = {},
                    onRetryWallet = {},
                    onRetryAppearance = {},
                    onNavigateRoute = {},
                )
            }
        }
        compose.onNodeWithTag("quest-header-home-button").assertDoesNotExist()
    }

    @Test
    fun headerBackButtonPopsDetailToListWhenInDetail() {
        var backCalled = false
        val state = presentation()
        compose.setContent {
            MobiMonTheme {
                QuestScreen(
                    state = state,
                    onClaimReward = {},
                    onDismissHiddenQuest = {},
                    onDismissRewardSuccess = {},
                    onRetryQuests = {},
                    onRetryWallet = {},
                    onRetryAppearance = {},
                    onNavigateRoute = {},
                    onBack = { backCalled = true },
                )
            }
        }
        compose.onNodeWithTag("quest-card-${DrivingQuestIds.SEATBELT}").performScrollTo().performClick()
        compose.onNodeWithTag("quest-detail-back-button").assertIsDisplayed()
        compose.onNodeWithTag("quest-header-back-button").performClick()
        compose.onNodeWithTag("quest-card-${DrivingQuestIds.SEATBELT}").assertIsDisplayed()
        assertFalse(backCalled)
    }

    @Test
    fun claimableQuestAppearsAtTopInList() {
        val state =
            presentation(
                QuestUiState(
                    isLoading = false,
                    satisfiedDrivingQuestIds = setOf(DrivingQuestIds.TIRE_CHECK),
                ),
            )
        render(state)
        assertEquals(DrivingQuestIds.TIRE_CHECK, state.quests.first().id)
        assertEquals(QuestItemStatus.CLAIMABLE, state.quests.first().status)
        compose.onNodeWithTag("quest-card-${DrivingQuestIds.TIRE_CHECK}").assertIsDisplayed()
        compose.onNodeWithTag("quest-btn-claim-${DrivingQuestIds.TIRE_CHECK}").assertIsDisplayed()
    }

    private fun render(
        state: QuestScreenState,
        onClaim: (String) -> Unit = {},
        onNavigate: (AppRoute) -> Unit = {},
    ) {
        compose.setContent { MobiMonTheme { QuestScreen(state, onClaim, {}, {}, {}, {}, {}, onNavigate) } }
    }

    private fun presentation(
        state: QuestUiState = QuestUiState(isLoading = false),
        friend: String = "friend:mobi",
    ): QuestScreenState =
        catalog.present(
            state,
            CompanionAppearanceState(CosmeticInventory(emptySet(), mapOf(CosmeticSlot.FRIEND to friend))),
            PointBalanceState.Ready(120),
            parkedVerified = true,
            context::getString,
        )

    private fun capture(
        viewProvider: () -> View,
        name: String,
    ) {
        compose.runOnIdle {
            val view = viewProvider()
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            val directory = File("build/reports/quest-ui").apply { mkdirs() }
            File(directory, "$name.png").outputStream().use {
                assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
            }
            bitmap.recycle()
        }
    }
}
