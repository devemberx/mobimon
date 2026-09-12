package com.monsters.mobimon.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Renders a minimal pet placeholder; replace this body when character assets arrive.
 * [appearanceKey] accepts GOLDEN or CREAM without importing a domain model.
 * [stage] is retained only for source compatibility with the legacy renderer.
 */
@Composable
fun PetAvatar(
    modifier: Modifier = Modifier,
    appearanceKey: String = "GOLDEN",
    stage: Int = 1,
    friendId: String = "friend:mobi",
) {
    val cat = friendId == "friend:luna"
    val cream = appearanceKey == "CREAM"
    val description = stringResource(if (cat) R.string.mobimon_luna_description else R.string.mobimon_mobi_description)
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
        drawCircle(Color(0xFF51402C), radius * 0.1f, center + Offset(0f, radius * 0.25f))
    }
}
