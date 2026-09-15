package com.monsters.mobimon.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
 * Renders the supplied Mobi artwork, with a placeholder for Luna until her final artwork is supplied.
 * Appearance and accessory arguments remain part of the handoff contract for future approved variants.
 */
@Composable
fun PetAvatar(
    modifier: Modifier = Modifier,
    appearanceKey: String = "GOLDEN",
    friendId: String = "friend:mobi",
    accessoryId: String? = null,
) {
    val cat = friendId == "friend:luna"
    val description = stringResource(if (cat) R.string.mobimon_luna_description else R.string.mobimon_mobi_description)
    if (!cat) {
        Image(
            painter = painterResource(R.drawable.mobimon_mobi_v4),
            contentDescription = description,
            modifier = modifier.size(120.dp),
        )
        return
    }
    val fur = Color(0xFFB9A8C4)
    val ear = Color(0xFF8D799C)
    Canvas(modifier = modifier.size(120.dp).semantics { contentDescription = description }) {
        val radius = size.minDimension * 0.34f
        val center = Offset(size.width / 2, size.height / 2)
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
        drawCircle(fur, radius, center)
        drawCircle(Color(0xFF51402C), radius * 0.1f, center + Offset(0f, radius * 0.25f))
    }
}
