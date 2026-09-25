package com.monsters.mobimon.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Shared parking status copy and palette; callers supply display state, never authorization. */
@Composable
fun MobiMonParkingStatusBadge(
    confirmed: Boolean,
    modifier: Modifier = Modifier,
    scale: Float = 1f,
) {
    MobiMonParkingBadge(
        status =
            stringResource(
                if (confirmed) R.string.mobimon_parking_confirmed else R.string.mobimon_parking_restricted,
            ),
        modifier = modifier,
        scale = scale,
        restricted = !confirmed,
    )
}

/** Shared parking badge geometry; callers supply display state, never authorization. */
@Composable
fun MobiMonParkingBadge(
    status: String,
    modifier: Modifier = Modifier,
    scale: Float = 1f,
    restricted: Boolean = false,
) {
    val fixedSize = LocalDensity.current.fontScale <= 1f
    val width = 440.dp * scale
    val foreground = if (restricted) MobiMonColors.parkingRestrictedForeground else MobiMonColors.accent
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
                .then(if (fixedSize) Modifier.size(width, 76.dp * scale) else Modifier)
                .semantics(mergeDescendants = true) { contentDescription = status }
                .drawWithContent {
                    drawContent()
                    // The reference stroke is centered on the capsule boundary.
                    drawRoundRect(
                        if (restricted) MobiMonColors.parkingRestrictedBorder else MobiMonColors.border,
                        cornerRadius = CornerRadius(size.height / 2),
                        style = Stroke((2.dp * scale).toPx()),
                    )
                },
        shape = RoundedCornerShape(50),
        color = if (restricted) MobiMonColors.parkingRestrictedBackground else MobiMonColors.panel,
        contentColor = foreground,
    ) {
        if (fixedSize) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(status, style = textStyle, textAlign = TextAlign.Center)
            }
        } else {
            Box(
                Modifier
                    .widthIn(min = width)
                    .heightIn(min = 76.dp * scale)
                    .padding(horizontal = 24.dp * scale, vertical = 12.dp * scale),
                contentAlignment = Alignment.Center,
            ) {
                Text(status, style = textStyle, textAlign = TextAlign.Center)
            }
        }
    }
}
