package com.monsters.mobimon.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.monsters.mobimon.R
import com.monsters.mobimon.core.ui.CompanionIcon
import com.monsters.mobimon.core.ui.MobiMonFontFamily
import com.monsters.mobimon.core.ui.MobiMonSettingsColors
import com.monsters.mobimon.core.ui.MobiMonTheme

@Composable
fun CompanionMenu(
    onClose: () -> Unit,
    onNavigate: (AppRoute) -> Unit,
) {
    val first = remember { FocusRequester() }
    BoxWithConstraints(Modifier.fillMaxSize().testTag("menu-host")) {
        val windowWidth = maxWidth
        val windowHeight = maxHeight
        Popup(onDismissRequest = onClose, properties = PopupProperties(focusable = true)) {
            // AAOS compatibility density can give Popup larger constraints than the host window.
            Box(Modifier.size(windowWidth, windowHeight)) {
                Box(Modifier.fillMaxSize().background(Color(0x7A071521)).clickable(onClick = onClose))
                BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
                    val scale = minOf(maxWidth.value / 2560f, maxHeight.value / 1268f).coerceIn(0.5f, 1f)
                    val left = (64.dp * scale).coerceAtMost(24.dp + maxWidth * 0.02f)
                    val top = (200.dp * scale).coerceAtMost((maxHeight - 576.dp * scale - 24.dp).coerceAtLeast(24.dp))
                    MenuPanel(
                        onClose,
                        onNavigate,
                        first,
                        scale,
                        Modifier
                            .padding(start = left, end = 24.dp, top = top, bottom = 24.dp)
                            .width(608.dp * scale)
                            .heightIn(max = maxHeight - top - 24.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuPanel(
    onClose: () -> Unit,
    onNavigate: (AppRoute) -> Unit,
    first: FocusRequester,
    scale: Float,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier.heightIn(min = 576.dp * scale).testTag("companion-menu"),
        shape = RoundedCornerShape(40.dp * scale),
        color = MobiMonSettingsColors.menu,
        border = BorderStroke(1.dp, MobiMonSettingsColors.border),
        shadowElevation = 12.dp * scale,
    ) {
        BoxWithConstraints {
            if (118.dp * scale >= 76.dp && LocalDensity.current.fontScale <= 1.2f && maxHeight >= 576.dp * scale) {
                ReferenceMenu(onClose, onNavigate, first, scale)
            } else {
                ScrollingMenu(onClose, onNavigate, first, scale)
            }
            LaunchedEffect(first) { first.requestFocus() }
        }
    }
}

@Composable
private fun ReferenceMenu(
    onClose: () -> Unit,
    onNavigate: (AppRoute) -> Unit,
    first: FocusRequester,
    scale: Float,
) {
    val rowHeight = 104.dp * scale
    val touchHeight = rowHeight.coerceAtLeast(76.dp)
    Box(Modifier.size(608.dp * scale, 576.dp * scale)) {
        Text(
            stringResource(R.string.drawer_menu),
            Modifier.offset(48.dp * scale, 48.dp * scale).semantics { heading() },
            color = MobiMonSettingsColors.text,
            style = menuStyle(36f, scale),
        )
        MenuClose(
            onClose,
            scale,
            Modifier.offset(540.dp * scale - 38.dp, 70.dp * scale - 38.dp),
        )
        listOf(
            Triple(R.string.drawer_menu_vehicle, CompanionIcon.VEHICLE, AppRoute.VEHICLE_INFO),
            Triple(R.string.drawer_menu_quests, CompanionIcon.QUEST, AppRoute.QUESTS),
            Triple(R.string.drawer_settings, CompanionIcon.SETTINGS, AppRoute.SETTINGS),
        ).forEachIndexed { index, (title, icon, route) ->
            MenuDestination(
                title,
                icon,
                route,
                scale,
                Modifier
                    .offset(32.dp * scale, (136 + index * 128).dp * scale - (touchHeight - rowHeight) / 2)
                    .width(544.dp * scale)
                    .height(touchHeight)
                    .then(if (index == 0) Modifier.focusRequester(first) else Modifier),
                onNavigate,
                reference = true,
            )
        }
    }
}

@Composable
private fun ScrollingMenu(
    onClose: () -> Unit,
    onNavigate: (AppRoute) -> Unit,
    first: FocusRequester,
    scale: Float,
) {
    Column(
        Modifier.verticalScroll(rememberScrollState()).padding(
            start = 32.dp * scale,
            end = 32.dp * scale,
            top =
                32.dp * scale,
            bottom = 24.dp,
        ),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp * scale, bottom = 8.dp * scale),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.drawer_menu),
                Modifier.weight(1f).semantics { heading() },
                color = MobiMonSettingsColors.text,
                style = menuStyle(36f, scale),
            )
            MenuClose(onClose, scale)
        }
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            MenuDestination(
                R.string.drawer_menu_vehicle,
                CompanionIcon.VEHICLE,
                AppRoute.VEHICLE_INFO,
                scale,
                Modifier.focusRequester(first),
                onNavigate,
            )
            MenuDestination(
                R.string.drawer_menu_quests,
                CompanionIcon.QUEST,
                AppRoute.QUESTS,
                scale,
                onNavigate = onNavigate,
            )
            MenuDestination(
                R.string.drawer_settings,
                CompanionIcon.SETTINGS,
                AppRoute.SETTINGS,
                scale,
                onNavigate = onNavigate,
            )
        }
    }
}

@Composable
private fun MenuClose(
    onClose: () -> Unit,
    scale: Float,
    modifier: Modifier = Modifier,
) {
    val closeDescription = stringResource(R.string.close)
    Box(
        modifier
            .size(76.dp)
            .clickable(role = Role.Button, onClick = onClose)
            .semantics { contentDescription = closeDescription },
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            Modifier.size(72.dp * scale).testTag("menu-close-visual"),
            shape = RoundedCornerShape(28.dp * scale),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp * scale, MobiMonSettingsColors.menuControlBorder),
        ) {
            Box(contentAlignment = Alignment.Center) {
                CompanionIcon(CompanionIcon.CLOSE, MobiMonSettingsColors.text, Modifier.size(40.dp * scale))
            }
        }
    }
}

@Composable
private fun MenuDestination(
    title: Int,
    icon: CompanionIcon,
    route: AppRoute,
    scale: Float,
    modifier: Modifier = Modifier,
    onNavigate: (AppRoute) -> Unit,
    reference: Boolean = false,
) {
    Box(
        modifier
            .fillMaxWidth()
            .heightIn(min = (104.dp * scale).coerceAtLeast(76.dp))
            .clickable(role = Role.Button) { onNavigate(route) },
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            (
                if (reference) {
                    Modifier.height(
                        104.dp * scale,
                    )
                } else {
                    Modifier.heightIn(min = (104.dp * scale).coerceAtLeast(76.dp))
                }
            ).fillMaxWidth()
                .testTag("menu-item-visual-${route.name}"),
            shape = RoundedCornerShape(36.dp * scale),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp * scale, MobiMonSettingsColors.menuControlBorder),
        ) {
            Box(
                Modifier.padding(horizontal = 28.dp * scale, vertical = 20.dp * scale),
                contentAlignment = Alignment.Center,
            ) {
                CompanionIcon(
                    icon,
                    MaterialTheme.colorScheme.onSurfaceVariant,
                    Modifier.align(Alignment.CenterStart).size(
                        36.dp * scale,
                    ),
                )
                Text(
                    stringResource(title),
                    Modifier.padding(horizontal = 52.dp * scale).offset(16.dp * scale, 1.5.dp * scale),
                    color = MobiMonSettingsColors.text,
                    style = menuStyle(34f, scale),
                )
            }
        }
    }
}

private fun menuStyle(
    size: Float,
    scale: Float,
) = TextStyle(
    fontFamily = MobiMonFontFamily,
    fontSize = (size * scale).sp,
    lineHeight = (size * scale * 1.2f).sp,
    fontWeight = FontWeight.Bold,
    letterSpacing = 0.sp,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None),
)

@Preview(name = "Menu · AAOS app content", widthDp = 1792, heightDp = 888, locale = "ko")
@Composable
private fun MenuPreview() {
    MobiMonTheme { CompanionMenu({}, {}) }
}

internal fun AppRoute.title(): Int =
    when (this) {
        AppRoute.HOME -> R.string.drawer_menu
        AppRoute.QUESTS -> R.string.drawer_quests
        AppRoute.VEHICLE_INFO -> R.string.drawer_vehicle_info
        AppRoute.APPEARANCE -> R.string.drawer_appearance
        AppRoute.SETTINGS -> R.string.drawer_settings
        AppRoute.CONVERSATION -> R.string.drawer_conversation
    }
