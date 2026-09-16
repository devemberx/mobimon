package com.monsters.mobimon.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Shared Home menu and destination Back control. */
@Composable
fun MobiMonNavigationButton(
    icon: Painter,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    visualSize: Dp = 76.dp,
    iconSize: Dp = 28.dp,
) {
    var focused by remember { mutableStateOf(false) }
    val targetSize = visualSize.coerceAtLeast(76.dp)
    IconButton(
        onClick,
        modifier
            .size(
                targetSize,
            ).onFocusChanged { focused = it.isFocused }
            .semantics { contentDescription = description },
    ) {
        Surface(
            Modifier.size(visualSize),
            shape = CircleShape,
            color = MobiMonColors.panel,
            border =
                BorderStroke(
                    if (focused) 3.dp else 1.dp,
                    if (focused) MobiMonColors.accent else MobiMonColors.border,
                ),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, Modifier.size(iconSize), tint = MobiMonColors.text)
            }
        }
    }
}
