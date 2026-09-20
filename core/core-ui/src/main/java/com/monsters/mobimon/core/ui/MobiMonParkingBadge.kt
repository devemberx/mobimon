package com.monsters.mobimon.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Shared Copilot parking geometry; callers supply display state, never authorization. */
@Composable
fun MobiMonParkingBadge(
    status: String,
    modifier: Modifier = Modifier,
    scale: Float = 1f,
    showParkingIcon: Boolean = true,
) {
    val positioned = LocalDensity.current.fontScale <= 1f && showParkingIcon
    val textStyle =
        TextStyle(
            fontFamily = MobiMonFontFamily,
            fontSize = (30 * scale).sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.sp,
            textMotion = TextMotion.Animated,
            lineHeight = (42 * scale).sp,
            platformStyle = PlatformTextStyle(includeFontPadding = false),
        )
    Surface(
        modifier =
            modifier
                .then(if (positioned) Modifier.size(344.dp * scale, 76.dp * scale) else Modifier)
                .semantics(mergeDescendants = true) { contentDescription = status }
                .drawWithContent {
                    drawContent()
                    // The reference stroke is centered on the capsule boundary.
                    drawRoundRect(
                        MobiMonColors.border,
                        cornerRadius = CornerRadius(size.height / 2),
                        style = Stroke((2.dp * scale).toPx()),
                    )
                },
        shape = RoundedCornerShape(50),
        color = MobiMonColors.panel,
        contentColor = MobiMonColors.accent,
    ) {
        if (positioned) {
            Box(Modifier.fillMaxSize()) {
                Icon(
                    painterResource(R.drawable.mobimon_parking),
                    contentDescription = null,
                    modifier = Modifier.offset(62.dp * scale, 18.dp * scale).size(40.dp * scale),
                )
                Text(
                    status,
                    Modifier.offset(127.65.dp * scale, 7.dp * scale).paddingFromBaseline(top = 42.dp * scale),
                    style = textStyle,
                )
            }
        } else {
            Row(
                Modifier
                    .widthIn(min = 344.dp * scale)
                    .heightIn(min = 76.dp * scale)
                    .padding(horizontal = 24.dp * scale, vertical = 12.dp * scale),
                horizontalArrangement = Arrangement.spacedBy(25.65.dp * scale, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showParkingIcon) {
                    Icon(painterResource(R.drawable.mobimon_parking), null, Modifier.size(40.dp * scale))
                }
                Text(status, modifier = Modifier.weight(1f, fill = false), style = textStyle)
            }
        }
    }
}
