package com.monsters.mobimon.feature.pet

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monsters.mobimon.core.ui.MobiMonFontFamily
import kotlin.math.roundToInt

internal fun homeTextStyle(
    size: Float,
    scale: Float,
    weight: FontWeight = FontWeight.Normal,
    align: TextAlign = TextAlign.Start,
) = TextStyle(fontFamily = MobiMonFontFamily, fontSize = (size * scale).sp, fontWeight = weight, textAlign = align)

private data class HomePosition(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val touch: Boolean,
    val baseline: Boolean,
) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?): Any = this@HomePosition
}

internal fun Modifier.reference(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    touch: Boolean = false,
): Modifier = then(HomePosition(x, y - 76f, width, height, touch, false))

internal fun Modifier.referenceText(
    x: Float,
    baseline: Float,
    width: Float,
): Modifier =
    then(
        HomePosition(
            x,
            baseline - 76f,
            width,
            0f,
            false,
            true,
        ),
    )

@Composable
internal fun HomeReferenceLayout(
    scale: Float,
    content: @Composable () -> Unit,
) {
    Layout(content, Modifier.fillMaxSize()) { measurables, constraints ->
        val factor = scale.dp.toPx()
        val measured =
            measurables.map { measurable ->
                val position = measurable.parentData as HomePosition
                val width = (position.width * factor).roundToInt()
                val height = (position.height * factor).roundToInt()
                val target = if (position.touch) 76.dp.roundToPx() else 0
                val childConstraints =
                    if (position.baseline) {
                        Constraints.fixedWidth(width)
                    } else {
                        Constraints.fixed(maxOf(width, target), maxOf(height, target))
                    }
                Triple(position, measurable.measure(childConstraints), Pair(width, height))
            }
        layout(constraints.maxWidth, constraints.maxHeight) {
            measured.forEach { (position, placeable, visual) ->
                val x = (position.x * factor).roundToInt() - (placeable.width - visual.first) / 2
                val y =
                    (position.y * factor).roundToInt() -
                        if (position.baseline) placeable[FirstBaseline] else (placeable.height - visual.second) / 2
                placeable.placeRelative(x, y)
            }
        }
    }
}
