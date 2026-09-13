package com.monsters.mobimon.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale

enum class CompanionIcon { BACK, CLOSE, VEHICLE, QUEST, SETTINGS }

@Composable
fun CompanionIcon(
    icon: CompanionIcon,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier) {
        scale(size.width / 48f, size.height / 48f, pivot = Offset.Zero) {
            val stroke = Stroke(3f, cap = StrokeCap.Round)

            fun line(
                x: Float,
                y: Float,
                endX: Float,
                endY: Float,
            ) = drawLine(tint, Offset(x, y), Offset(endX, endY), 3f, StrokeCap.Round)
            when (icon) {
                CompanionIcon.BACK -> {
                    line(29f, 10f, 15f, 24f)
                    line(15f, 24f, 29f, 38f)
                }
                CompanionIcon.CLOSE -> {
                    line(12f, 12f, 36f, 36f)
                    line(36f, 12f, 12f, 36f)
                }
                CompanionIcon.VEHICLE -> {
                    drawPath(
                        Path().apply {
                            moveTo(8f, 34f)
                            lineTo(8f, 21f)
                            lineTo(13f, 10f)
                            lineTo(35f, 10f)
                            lineTo(40f, 21f)
                            lineTo(40f, 34f)
                            close()
                        },
                        tint,
                        style = stroke,
                    )
                    line(9f, 21f, 39f, 21f)
                    line(11f, 34f, 11f, 40f)
                    line(37f, 34f, 37f, 40f)
                    line(14f, 27f, 18f, 27f)
                    line(30f, 27f, 34f, 27f)
                }
                CompanionIcon.QUEST ->
                    drawPath(
                        Path().apply {
                            moveTo(24f, 40f)
                            cubicTo(5f, 28f, 2f, 16f, 10f, 10f)
                            cubicTo(16f, 6f, 22f, 10f, 24f, 14f)
                            cubicTo(26f, 10f, 32f, 6f, 38f, 10f)
                            cubicTo(46f, 16f, 43f, 28f, 24f, 40f)
                            close()
                        },
                        tint,
                        style = stroke,
                    )
                CompanionIcon.SETTINGS -> {
                    drawCircle(tint, 14f, Offset(24f, 24f), style = stroke)
                    drawCircle(tint, 6f, Offset(24f, 24f), style = stroke)
                    repeat(8) { i ->
                        val angle = i * kotlin.math.PI / 4
                        val x = kotlin.math.cos(angle).toFloat()
                        val y = kotlin.math.sin(angle).toFloat()
                        line(24f + x * 14, 24f + y * 14, 24f + x * 20, 24f + y * 20)
                    }
                }
            }
        }
    }
}
