package com.monsters.mobimon.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

enum class ParticleType {
    STAR,
    SNOW,
    PETAL,
}

private class Particle(
    var x: Float,
    var y: Float,
    var sizePx: Float,
    var speedY: Float,
    var swaySpeed: Float,
    var swayAmount: Float,
    var rotation: Float,
    var rotationSpeed: Float,
    var alpha: Float,
    var phase: Float,
    var color: Color,
) {
    fun resetToTop(
        random: Random,
        color: Color,
    ) {
        x = random.nextFloat()
        y = -0.05f - random.nextFloat() * 0.15f
        speedY = 0.08f + random.nextFloat() * 0.12f
        swaySpeed = 1.0f + random.nextFloat() * 2.0f
        swayAmount = 0.02f + random.nextFloat() * 0.04f
        rotation = random.nextFloat() * 360f
        rotationSpeed = -40f + random.nextFloat() * 80f
        alpha = 0.3f + random.nextFloat() * 0.6f
        phase = random.nextFloat() * (2f * PI.toFloat())
        this.color = color
    }
}

/**
 * Animated background particle effect for falling stars, snow, or petals.
 */
@Composable
fun FallingParticlesEffect(
    modifier: Modifier = Modifier,
    particleType: ParticleType = ParticleType.STAR,
    particleCount: Int = 30,
    minSize: Dp = 8.dp,
    maxSize: Dp = 18.dp,
    particleColor: Color? = null,
) {
    val density = LocalDensity.current
    val minSizePx = with(density) { minSize.toPx() }
    val maxSizePx = with(density) { maxSize.toPx() }

    val defaultColor =
        when (particleType) {
            ParticleType.STAR -> Color(0xFFFFD54F)
            ParticleType.SNOW -> Color(0xFFE3F2FD)
            ParticleType.PETAL -> Color(0xFFF8BBD0)
        }
    val colorToUse = particleColor ?: defaultColor

    val particles =
        remember(particleType, particleCount) {
            val random = Random(1337)
            List(particleCount) {
                Particle(
                    x = random.nextFloat(),
                    y = random.nextFloat(),
                    sizePx = minSizePx + random.nextFloat() * (maxSizePx - minSizePx),
                    speedY = 0.08f + random.nextFloat() * 0.12f,
                    swaySpeed = 1.0f + random.nextFloat() * 2.0f,
                    swayAmount = 0.02f + random.nextFloat() * 0.04f,
                    rotation = random.nextFloat() * 360f,
                    rotationSpeed = -40f + random.nextFloat() * 80f,
                    alpha = 0.3f + random.nextFloat() * 0.6f,
                    phase = random.nextFloat() * (2f * PI.toFloat()),
                    color = colorToUse,
                )
            }
        }

    val starPath = remember { createStarPath() }
    val petalPath = remember { createPetalPath() }

    var frameTimeNanos by remember { mutableLongStateOf(0L) }

    LaunchedEffect(particleType, particleCount) {
        val random = Random(System.currentTimeMillis())
        var lastMs = System.currentTimeMillis()
        while (isActive) {
            delay(16.milliseconds)
            val currentMs = System.currentTimeMillis()
            val dt = ((currentMs - lastMs) / 1000f).coerceIn(0.001f, 0.1f)
            lastMs = currentMs

            particles.forEach { p ->
                p.y += p.speedY * dt
                p.phase += p.swaySpeed * dt
                p.x += sin(p.phase.toDouble()).toFloat() * p.swayAmount * dt
                p.rotation = (p.rotation + p.rotationSpeed * dt) % 360f

                if (p.y > 1.05f) {
                    p.resetToTop(random, colorToUse)
                }
            }
            frameTimeNanos = currentMs
        }
    }

    Canvas(modifier = modifier.clipToBounds()) {
        val renderFrame = frameTimeNanos
        if (renderFrame < 0) return@Canvas

        clipRect {
            particles.forEach { p ->
                val drawX = p.x * size.width
                val drawY = p.y * size.height

                when (particleType) {
                    ParticleType.STAR -> {
                        val scaleFactor = p.sizePx / 24f
                        withTransform({
                            translate(drawX, drawY)
                            rotate(p.rotation, pivot = Offset.Zero)
                            scale(scaleFactor, scaleFactor, pivot = Offset.Zero)
                        }) {
                            drawPath(starPath, color = p.color.copy(alpha = p.alpha))
                        }
                    }
                    ParticleType.PETAL -> {
                        val scaleFactor = p.sizePx / 24f
                        withTransform({
                            translate(drawX, drawY)
                            rotate(p.rotation, pivot = Offset.Zero)
                            scale(scaleFactor, scaleFactor, pivot = Offset.Zero)
                        }) {
                            drawPath(petalPath, color = p.color.copy(alpha = p.alpha))
                        }
                    }
                    ParticleType.SNOW -> {
                        drawCircle(
                            color = p.color.copy(alpha = p.alpha),
                            radius = p.sizePx / 2f,
                            center = Offset(drawX, drawY),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Convenience wrapper for falling star background decoration.
 */
@Composable
fun FallingStarsEffect(
    modifier: Modifier = Modifier,
    particleCount: Int = 30,
    color: Color = Color(0xFFFFD54F),
) {
    FallingParticlesEffect(
        modifier = modifier,
        particleType = ParticleType.STAR,
        particleCount = particleCount,
        particleColor = color,
    )
}

/** Creates a 5-pointed star path centered at (0, 0) in a 24x24 bounding area. */
private fun createStarPath(): Path {
    val path = Path()
    val outerRadius = 12f
    val innerRadius = 5f
    val centerX = 0f
    val centerY = 0f
    val points = 5
    val angleStep = Math.PI / points

    var angle = -Math.PI / 2
    path.moveTo(
        (centerX + outerRadius * cos(angle)).toFloat(),
        (centerY + outerRadius * sin(angle)).toFloat(),
    )
    for (i in 1 until points * 2) {
        angle += angleStep
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        path.lineTo(
            (centerX + radius * cos(angle)).toFloat(),
            (centerY + radius * sin(angle)).toFloat(),
        )
    }
    path.close()
    return path
}

/** Creates a flower petal path centered at (0, 0) in a 24x24 bounding area. */
private fun createPetalPath(): Path {
    val path = Path()
    path.moveTo(0f, -12f)
    path.cubicTo(8f, -8f, 10f, 4f, 0f, 12f)
    path.cubicTo(-10f, 4f, -8f, -8f, 0f, -12f)
    path.close()
    return path
}
