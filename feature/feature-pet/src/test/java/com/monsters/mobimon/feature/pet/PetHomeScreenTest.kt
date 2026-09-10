package com.monsters.mobimon.feature.pet

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import com.monsters.mobimon.core.domain.CompanionSettings
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.PetProfile
import com.monsters.mobimon.core.domain.QuestProgress
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.VehicleSnapshot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ko-rKR")
class PetHomeScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun realUnavailableHomeDoesNotClaimParkedState() {
        render(vehiclePreview = false, showOnVehicleHome = true)

        compose.onNodeWithText("주차 여부 확인 불가").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("주차 확인됨").assertDoesNotExist()
    }

    @Test
    fun hiddenPetPreferenceRemovesConversationTargetOnlyOnVehiclePreview() {
        render(vehiclePreview = true, showOnVehicleHome = false)

        compose.onNodeWithContentDescription("강아지와 대화하기").assertDoesNotExist()
        compose.onNodeWithText("차량 홈에서 강아지를 숨겼어요").performScrollTo().assertIsDisplayed()
    }

    private fun render(
        vehiclePreview: Boolean,
        showOnVehicleHome: Boolean,
    ) {
        compose.setContent {
            PetHomeScreen(
                profile = PetProfile("profile"),
                stage = 1,
                xpUntilNextStage = 80,
                snapshot =
                    VehicleSnapshot(
                        id = "unavailable",
                        epoch = "epoch",
                        sequence = 0,
                        receivedAtMillis = 0,
                        source = SignalSource.REAL,
                        drivingState = DrivingState.UNKNOWN,
                        quality = SignalQuality.UNAVAILABLE,
                    ),
                progress = QuestProgress(),
                settings = CompanionSettings(showOnVehicleHome = showOnVehicleHome),
                onOpenMenu = {},
                onOpenVehicleInfo = {},
                onOpenQuests = {},
                onSwitchHome = {},
                onPetClick = {},
                vehiclePreview = vehiclePreview,
            )
        }
    }
}
