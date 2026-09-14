package com.monsters.mobimon.feature.auth

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.monsters.mobimon.core.domain.Clock
import com.monsters.mobimon.core.domain.CompanionSettings
import com.monsters.mobimon.core.domain.CosmeticInventory
import com.monsters.mobimon.core.domain.CosmeticItem
import com.monsters.mobimon.core.domain.CosmeticSlot
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.EquipResult
import com.monsters.mobimon.core.domain.PetAppearance
import com.monsters.mobimon.core.domain.PetProfile
import com.monsters.mobimon.core.domain.PetRepository
import com.monsters.mobimon.core.domain.PointAwardResult
import com.monsters.mobimon.core.domain.PointEconomy
import com.monsters.mobimon.core.domain.PointWallet
import com.monsters.mobimon.core.domain.ProgressionIdentity
import com.monsters.mobimon.core.domain.PurchaseResult
import com.monsters.mobimon.core.domain.SettingsRepository
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.VehicleFreshnessPolicy
import com.monsters.mobimon.core.domain.VehicleRepository
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.core.domain.WriteResult
import com.monsters.mobimon.core.navigation.AiRoute
import com.monsters.mobimon.core.navigation.FeatureNavigator
import com.monsters.mobimon.core.navigation.HomeSurface
import com.monsters.mobimon.core.presentation.VehiclePresentation
import com.monsters.mobimon.core.ui.MobiMonTheme
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ko-rKR-w1000dp-h800dp")
class AiFeatureTest {
    @get:Rule val compose = createComposeRule()

    private val pets = FakePets()
    private val settings = FakeSettings()
    private val points = FakePoints()
    private var route by mutableStateOf(AiRoute.COPILOT)

    @Test
    fun failedObservationKeepsCompanionAndExposesRetryAfterRevisiting() {
        show()
        compose.onNodeWithText("모비와 이야기해요.").assertIsDisplayed()
        compose.runOnIdle { points.fail.trySend(Unit) }
        compose.onNodeWithText("친구 정보를 갱신하지 못했어요.", substring = true).assertIsDisplayed()
        compose.onNodeWithText("모비와 이야기해요.").assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(0, points.activeObservers)
            points.savedInventory.value = equippedFriend("friend:luna")
            settings.settings.value = CompanionSettings(reducedMotion = true)
            route = AiRoute.CONVERSATION
        }
        compose.waitForIdle()
        compose.runOnIdle { route = AiRoute.COPILOT }
        compose.onNodeWithText("친구 정보를 갱신하지 못했어요.", substring = true).assertIsDisplayed()
        compose.onNodeWithText("모비와 이야기해요.").assertIsDisplayed()
        compose.onNodeWithText("다시 시도").assertHeightIsAtLeast(76.dp).performClick()
        compose.onNodeWithText("루나와 이야기해요.").assertIsDisplayed()
        compose.onNodeWithText("친구 정보를 갱신하지 못했어요.", substring = true).assertDoesNotExist()
        compose.onNodeWithText("다시 시도").assertDoesNotExist()
        compose.onNodeWithText("QR로 연결하기").performScrollTo().assertIsNotEnabled()
        compose.runOnIdle { route = AiRoute.CONVERSATION }
        compose.waitForIdle()
        compose.runOnIdle { route = AiRoute.COPILOT }
        compose.onNodeWithText("루나와 이야기해요.").assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(2, points.subscriptions)
            assertEquals(1, points.activeObservers)
            assertEquals(1, points.maxActiveObservers)
            assertEquals(1, pets.profile.subscriptionCount.value)
            assertEquals(1, settings.settings.subscriptionCount.value)
        }
    }

    @Test
    fun initialFailureCanRetryWithEnlargedText() {
        pets.initializationFailure = IOException("temporary initialization failure")
        show(fontScale = 2f)
        compose.onNodeWithText("친구 정보를 불러오지 못했어요.").assertIsDisplayed()
        compose.runOnIdle { pets.initializationFailure = null }
        compose
            .onNodeWithText("다시 시도")
            .performScrollTo()
            .assertHeightIsAtLeast(76.dp)
            .performClick()
        compose.onNodeWithText("모비와 이야기해요.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("친구 정보를 불러오지 못했어요.").assertDoesNotExist()
        compose.runOnIdle { assertEquals(1, points.subscriptions) }
    }

    @OptIn(ExperimentalTestApi::class)
    @Config(qualifiers = "ko-rKR-w600dp-h600dp")
    @Test
    fun retainedContextRetryIsKeyboardReachableWithEnlargedText() {
        show(fontScale = 2f)
        compose.onNodeWithText("모비와 이야기해요.").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { points.fail.trySend(Unit) }
        compose.onNodeWithText("친구 정보를 갱신하지 못했어요.", substring = true).assertIsDisplayed()
        compose
            .onNodeWithText("다시 시도")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(76.dp)
            .performSemanticsAction(SemanticsActions.RequestFocus)
            .assertIsFocused()
            .performKeyInput { pressKey(Key.Enter) }
        compose.onNodeWithText("친구 정보를 갱신하지 못했어요.", substring = true).assertDoesNotExist()
        compose.onNodeWithText("모비와 이야기해요.").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { assertEquals(2, points.subscriptions) }
    }

    private fun show(fontScale: Float = 1f) {
        val vehicle =
            object : VehicleRepository {
                override val snapshots =
                    MutableStateFlow(
                        VehicleSnapshot(
                            "unavailable",
                            "test",
                            0,
                            0,
                            SignalSource.REAL,
                            DrivingState.UNKNOWN,
                            SignalQuality.UNAVAILABLE,
                        ),
                    )

                override fun start() = error("Feature must not start a provider")

                override fun stop() = error("Feature must not stop a provider")
            }
        val feature =
            AiFeature(
                pets,
                settings,
                points,
                VehiclePresentation(
                    vehicle,
                    ProgressionIdentity("saved", SignalSource.REAL),
                    Clock {
                        0
                    },
                    VehicleFreshnessPolicy(15_000),
                ),
            )
        val navigator = FeatureNavigator(HomeSurface.PET, {}, {}, {}, {}, {})
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale)) {
                MobiMonTheme { feature.Content(route, navigator, Modifier) }
            }
        }
    }

    private class FakePets : PetRepository {
        override val profile = MutableStateFlow(PetProfile("saved"))
        var initializationFailure: Exception? = null

        override suspend fun initialize() {
            initializationFailure?.let { throw it }
        }

        override suspend fun setAppearance(appearance: PetAppearance) = WriteResult.Failure
    }

    private class FakeSettings : SettingsRepository {
        override val settings = MutableStateFlow(CompanionSettings())

        override suspend fun setShowOnVehicleHome(enabled: Boolean) = WriteResult.Failure

        override suspend fun setReducedMotion(enabled: Boolean) = WriteResult.Failure

        override suspend fun setLauncherCharacterEnabled(enabled: Boolean) = WriteResult.Failure
    }

    private class FakePoints : PointEconomy {
        val fail = Channel<Unit>(Channel.CONFLATED)
        val savedInventory = MutableStateFlow(equippedFriend("friend:mobi"))
        var subscriptions = 0
        var activeObservers = 0
        var maxActiveObservers = 0
        override val inventory =
            flow {
                subscriptions++
                activeObservers++
                maxActiveObservers = maxOf(maxActiveObservers, activeObservers)
                try {
                    if (subscriptions == 1) {
                        emit(savedInventory.value)
                        fail.receive()
                        throw IOException("temporary inventory read failure")
                    }
                    emitAll(savedInventory)
                } finally {
                    activeObservers--
                }
            }
        override val wallet = flowOf(PointWallet(0))
        override val catalog = flowOf(emptyList<CosmeticItem>())

        override suspend fun purchase(
            itemId: String,
            expectedPrice: Long,
        ) = PurchaseResult.ItemUnavailable

        override suspend fun equip(itemId: String) = EquipResult.ItemUnavailable

        override suspend fun awardQuest(
            questId: String,
            displayedSnapshot: VehicleSnapshot,
        ) = PointAwardResult.QuestUnavailable
    }

    private companion object {
        fun equippedFriend(friend: String) = CosmeticInventory(setOf(friend), mapOf(CosmeticSlot.FRIEND to friend))
    }
}
