package com.monsters.mobimon.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Renders the approved default Mobi artwork and temporary fallbacks for variants without final assets.
 * [appearanceKey] accepts GOLDEN or CREAM without importing a domain model.
 */
@Composable
fun PetAvatar(
    modifier: Modifier = Modifier,
    appearanceKey: String = "GOLDEN",
    friendId: String = "friend:mobi",
    accessoryId: String? = null,
) {
    val cat = friendId == "friend:luna"
    val cream = appearanceKey == "CREAM"
    val description = stringResource(if (cat) R.string.mobimon_luna_description else R.string.mobimon_mobi_description)
    if (!cat && !cream) {
        Box(modifier = modifier.size(120.dp).semantics { contentDescription = description }) {
            Image(
                painter = painterResource(R.drawable.mobimon_mobi_v4),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
            if (accessoryId != null) {
                MobiAccessory(accessoryId, Modifier.fillMaxSize())
            }
        }
        return
    }
    val fur =
        if (cat) {
            Color(0xFFB9A8C4)
        } else if (cream) {
            Color(0xFFF2E4C8)
        } else {
            Color(0xFFD6AB6D)
        }
    val ear =
        if (cat) {
            Color(0xFF8D799C)
        } else if (cream) {
            Color(0xFFD4C29D)
        } else {
            Color(0xFFB18751)
        }
    Canvas(modifier = modifier.size(120.dp).semantics { contentDescription = description }) {
        val radius = size.minDimension * 0.34f
        val center = Offset(size.width / 2, size.height / 2)
        if (cat) {
            listOf(-1f, 1f).forEach { side ->
                val tip = center + Offset(side * radius * 0.8f, -radius * 1.4f)
                val left = center + Offset(side * radius * 1.2f, -radius * 0.3f)
                val right = center + Offset(side * radius * 0.15f, -radius * 0.75f)
                drawPath(
                    Path().apply {
                        moveTo(tip.x, tip.y)
                        lineTo(left.x, left.y)
                        lineTo(right.x, right.y)
                        close()
                    },
                    ear,
                )
            }
        } else {
            drawCircle(ear, radius * 0.48f, center + Offset(-radius * 0.85f, -radius * 0.4f))
            drawCircle(ear, radius * 0.48f, center + Offset(radius * 0.85f, -radius * 0.4f))
        }
        drawCircle(fur, radius, center)

        if (accessoryId == "accessory:necklace") {
            drawCircle(
                Color(0xFF8B5A2B),
                radius * 0.75f,
                center + Offset(0f, radius * 0.4f),
                style =
                    androidx.compose.ui.graphics.drawscope
                        .Stroke(width = 6f),
            )
            drawCircle(Color(0xFFFFD700), radius * 0.15f, center + Offset(0f, radius * 1.1f))
        } else if (accessoryId == "accessory:mint_scarf") {
            val scarfPath =
                Path().apply {
                    moveTo(center.x - radius * 0.7f, center.y + radius * 0.6f)
                    quadraticBezierTo(
                        center.x,
                        center.y + radius * 1.3f,
                        center.x + radius * 0.7f,
                        center.y + radius * 0.6f,
                    )
                    lineTo(center.x + radius * 0.4f, center.y + radius * 1.1f)
                    lineTo(center.x - radius * 0.4f, center.y + radius * 1.1f)
                    close()
                }
            drawPath(scarfPath, Color(0xFF7FC1A5))
        }

        drawCircle(Color(0xFF51402C), radius * 0.1f, center + Offset(0f, radius * 0.25f))
    }
}

@Composable
private fun MobiAccessory(
    accessoryId: String,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier) {
        val center = Offset(size.width / 2, size.height * 0.82f)
        when (accessoryId) {
            "accessory:necklace" -> {
                val stroke = size.minDimension * 0.012f
                drawLine(
                    Color(0xFF8B5A2B),
                    Offset(size.width * 0.4f, size.height * 0.77f),
                    center,
                    stroke,
                )
                drawLine(
                    Color(0xFF8B5A2B),
                    center,
                    Offset(size.width * 0.6f, size.height * 0.77f),
                    stroke,
                )
                drawCircle(Color(0xFFFFD700), size.minDimension * 0.025f, center)
            }
            "accessory:mint_scarf" -> {
                val scarfPath =
                    Path().apply {
                        moveTo(size.width * 0.38f, size.height * 0.75f)
                        quadraticBezierTo(center.x, size.height * 0.82f, size.width * 0.62f, size.height * 0.75f)
                        lineTo(size.width * 0.57f, size.height * 0.85f)
                        lineTo(size.width * 0.43f, size.height * 0.85f)
                        close()
                    }
                drawPath(scarfPath, Color(0xFF7FC1A5))
            }
        }
    }
}
