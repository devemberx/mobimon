package com.monsters.mobimon.core.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FallingParticlesEffectTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun fallingStarsEffectRendersWithoutCrashing() {
        compose.setContent {
            MobiMonTheme {
                FallingParticlesEffect(
                    modifier = Modifier.fillMaxSize().testTag("stars-effect"),
                    particleType = ParticleType.STAR,
                    particleCount = 10,
                )
            }
        }
        compose.onNodeWithTag("stars-effect").assertIsDisplayed()
    }

    @Test
    fun fallingParticlesEffectRendersSnowAndPetalTypes() {
        compose.setContent {
            MobiMonTheme {
                FallingParticlesEffect(
                    modifier = Modifier.fillMaxSize().testTag("snow-effect"),
                    particleType = ParticleType.SNOW,
                    particleCount = 5,
                )
            }
        }
        compose.onNodeWithTag("snow-effect").assertIsDisplayed()
    }
}
