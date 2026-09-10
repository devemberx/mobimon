package com.monsters.mobimon.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Renders a minimal pet placeholder; replace this body when character assets arrive.
 * [appearanceKey] accepts GOLDEN or CREAM without importing a domain model.
 * [stage] is a caller-derived growth stage. Interaction belongs to the containing screen.
 */
@Composable
fun PetAvatar(
    modifier: Modifier = Modifier,
    appearanceKey: String = "GOLDEN",
    stage: Int = 1,
) {
    val cream = appearanceKey == "CREAM"
    val appearance =
        stringResource(if (cream) R.string.mobimon_appearance_cream else R.string.mobimon_appearance_golden)
    val description = stringResource(R.string.mobimon_pet_description, appearance, stage)
    val fur = if (cream) Color(0xFFF2E4C8) else Color(0xFFD6AB6D)
    val ear = if (cream) Color(0xFFD4C29D) else Color(0xFFB18751)
    Canvas(modifier = modifier.size(120.dp).semantics { contentDescription = description }) {
        val radius = size.minDimension * 0.34f
        val center = Offset(size.width / 2, size.height / 2)
        drawCircle(ear, radius * 0.48f, center + Offset(-radius * 0.85f, -radius * 0.4f))
        drawCircle(ear, radius * 0.48f, center + Offset(radius * 0.85f, -radius * 0.4f))
        drawCircle(fur, radius, center)
        drawCircle(Color(0xFF51402C), radius * 0.1f, center + Offset(0f, radius * 0.25f))
    }
}
