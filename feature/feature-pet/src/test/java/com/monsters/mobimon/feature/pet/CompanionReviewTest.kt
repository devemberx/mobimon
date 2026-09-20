package com.monsters.mobimon.feature.pet

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import com.monsters.mobimon.core.domain.CompanionSettings
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.PetProfile
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.core.ui.LocalMobiMonMotionEnabled
import com.monsters.mobimon.core.ui.MobiMonTheme
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
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CompanionReviewTest {
    @get:Rule val compose = createComposeRule()

    @Test fun homeReferenceRender() {
        render("home") {
            PetHomeScreen(
                profile = PetProfile("review"),
                snapshot =
                    VehicleSnapshot(
                        "review",
                        "epoch",
                        1,
                        1000,
                        SignalSource.SIMULATED,
                        DrivingState.PARKED,
                        SignalQuality.VALID,
                        72,
                        timeOfDay = "Night",
                    ),
                onOpenMenu = {},
                onPetClick = {},
                pointBalance = 1200,
                interactionAllowed = true,
                connectionAvailable = true,
            )
        }
    }

    @Test fun settingsReferenceRender() {
        render("settings") {
            SettingsScreen(CompanionSettings(reducedMotion = true), {}, parkedVerified = true, onOpenCopilot = {})
        }
        compose.onNodeWithTag("settings-done").assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ko-rKR-w800dp-h600dp-mdpi")
    fun compactSettingsKeepsDoneReachableAtEnlargedText() {
        render("settings-compact") {
            CompositionLocalProvider(LocalDensity provides Density(1f, 1.5f)) {
                SettingsScreen(CompanionSettings(reducedMotion = true), {}, parkedVerified = true, onOpenCopilot = {})
            }
        }
        compose.onNodeWithTag("settings-done").assertIsDisplayed()
    }

    private fun render(
        name: String,
        content: @Composable () -> Unit,
    ) {
        lateinit var view: View
        compose.setContent {
            val current = LocalView.current
            SideEffect { view = current }
            CompositionLocalProvider(LocalMobiMonMotionEnabled provides false) { MobiMonTheme(content = content) }
        }
        compose.runOnIdle {
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            val directory = File("build/reports/companion-ui").apply { mkdirs() }
            File(directory, "$name.png").outputStream().use {
                assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
            }
            bitmap.recycle()
        }
    }
}
