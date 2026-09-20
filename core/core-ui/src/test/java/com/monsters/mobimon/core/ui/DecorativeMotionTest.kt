package com.monsters.mobimon.core.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w600dp-h400dp-mdpi")
class DecorativeMotionTest {
    @get:Rule val compose = createComposeRule()

    private lateinit var view: View

    @Test
    fun petAvatarsStopWhenMotionPreferenceChanges() {
        var motionEnabled by mutableStateOf(true)
        show {
            CompositionLocalProvider(LocalMobiMonMotionEnabled provides motionEnabled) {
                Row {
                    PetAvatar(Modifier.size(180.dp).testTag("mobi"))
                    PetAvatar(Modifier.size(180.dp).testTag("luna"), friendId = "friend:luna")
                }
            }
        }
        val firstMobi = pixels("mobi")
        val firstLuna = pixels("luna")
        compose.mainClock.advanceTimeBy(320)
        assertTrue("Mobi advances with decorative motion enabled", firstMobi != pixels("mobi"))
        assertTrue("Luna advances with decorative motion enabled", firstLuna != pixels("luna"))

        updateStateAndDraw { motionEnabled = false }
        val staticMobi = pixels("mobi")
        val staticLuna = pixels("luna")
        compose.mainClock.advanceTimeBy(480)

        assertTrue("Mobi remains static after the preference changes", staticMobi == pixels("mobi"))
        assertTrue("Luna remains static after the preference changes", staticLuna == pixels("luna"))
    }

    @Test
    fun directBreathingRenderersHonorDisabledMotion() {
        show {
            CompositionLocalProvider(LocalMobiMonMotionEnabled provides false) {
                Row {
                    MobiIdleBreathAnimation(Modifier.size(180.dp).testTag("mobi"))
                    LunaIdleBreathAnimation(Modifier.size(180.dp).testTag("luna"))
                }
            }
        }
        val mobi = pixels("mobi")
        val luna = pixels("luna")
        compose.mainClock.advanceTimeBy(480)

        assertTrue("Direct Mobi renderer obeys the preference", mobi == pixels("mobi"))
        assertTrue("Direct Luna renderer obeys the preference", luna == pixels("luna"))
    }

    @Test
    fun explicitAvatarOptOutKeepsArtStaticWithMotionEnabled() {
        show { PetAvatar(Modifier.size(180.dp).testTag("mobi"), isAnimated = false) }
        val mobi = pixels("mobi")
        compose.mainClock.advanceTimeBy(480)

        assertTrue("Explicit avatar opt-out remains static", mobi == pixels("mobi"))
    }

    @Test
    fun particlesStopWhenMotionPreferenceChanges() {
        var motionEnabled by mutableStateOf(true)
        show {
            CompositionLocalProvider(LocalMobiMonMotionEnabled provides motionEnabled) {
                FallingParticlesEffect(Modifier.size(300.dp).testTag("particles"))
            }
        }
        val first = pixels("particles")
        compose.mainClock.advanceTimeBy(320)
        assertTrue("Enabled particles advance", first != pixels("particles"))

        updateStateAndDraw { motionEnabled = false }
        val stopped = pixels("particles")
        compose.mainClock.advanceTimeBy(480)

        assertTrue("Particles stop after the preference changes", stopped == pixels("particles"))
    }

    @Test
    fun explicitParticleOptOutKeepsDecorationStatic() {
        show { FallingParticlesEffect(Modifier.size(300.dp).testTag("particles"), isAnimated = false) }
        val first = pixels("particles")
        compose.mainClock.advanceTimeBy(480)

        assertTrue("Explicit particle opt-out remains static", first == pixels("particles"))
    }

    @Test
    fun particleColorChangesWithoutChangingType() {
        var color by mutableStateOf(Color.Red)
        show {
            CompositionLocalProvider(LocalMobiMonMotionEnabled provides false) {
                FallingParticlesEffect(
                    Modifier.size(300.dp).testTag("particles"),
                    particleType = ParticleType.SNOW,
                    particleColor = color,
                )
            }
        }
        assertTrue(pixels("particles").any { android.graphics.Color.red(it) > android.graphics.Color.blue(it) + 40 })
        updateStateAndDraw { color = Color.Blue }

        assertTrue(pixels("particles").any { android.graphics.Color.blue(it) > android.graphics.Color.red(it) + 40 })
    }

    @Test
    fun particleSizeChangesWithoutChangingType() {
        var particleSize by mutableStateOf(4.dp)
        show {
            CompositionLocalProvider(LocalMobiMonMotionEnabled provides false) {
                FallingParticlesEffect(
                    Modifier.size(300.dp).testTag("particles"),
                    particleType = ParticleType.SNOW,
                    minSize = particleSize,
                    maxSize = particleSize,
                    particleColor = Color.Red,
                )
            }
        }
        val initialArea =
            pixels("particles").count {
                android.graphics.Color.red(it) >
                    android.graphics.Color.blue(it) + 40
            }
        updateStateAndDraw { particleSize = 20.dp }
        val enlargedArea =
            pixels("particles").count {
                android.graphics.Color.red(it) >
                    android.graphics.Color.blue(it) + 40
            }

        assertTrue("Updated particle size must affect the rendered area", enlargedArea > initialArea * 4)
    }

    private fun show(content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            val currentView = LocalView.current
            SideEffect { view = currentView }
            MobiMonTheme(content = content)
        }
        compose.mainClock.advanceTimeByFrame()
    }

    private fun updateStateAndDraw(update: () -> Unit) {
        compose.runOnIdle {
            update()
            // Compose 1.6's manual clock does not flush Android's posted snapshot notifications.
            Snapshot.sendApplyNotifications()
        }
        compose.mainClock.advanceTimeByFrame()
        compose.waitForIdle()
    }

    private fun pixels(tag: String): List<Int> {
        val area = compose.onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot
        lateinit var result: List<Int>
        compose.runOnIdle {
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            val width = area.width.toInt()
            val height = area.height.toInt()
            result =
                IntArray(width * height)
                    .also { bitmap.getPixels(it, 0, width, area.left.toInt(), area.top.toInt(), width, height) }
                    .toList()
            bitmap.recycle()
        }
        return result
    }
}
