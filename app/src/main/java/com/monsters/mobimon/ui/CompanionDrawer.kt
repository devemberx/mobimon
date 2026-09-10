package com.monsters.mobimon.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.monsters.mobimon.R

@Composable
fun CompanionDrawer(
    destination: DrawerDestination,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onNavigate: (DrawerDestination) -> Unit,
    simulated: Boolean = false,
    content: @Composable () -> Unit,
) {
    val initialFocus = remember { FocusRequester() }
    var lastDetail by remember { mutableStateOf<DrawerDestination?>(null) }
    val menuItems =
        listOf(
            DrawerDestination.PET_INFO,
            DrawerDestination.QUESTS,
            DrawerDestination.APPEARANCE,
            DrawerDestination.SETTINGS,
        )
    val focusMenuItem = lastDetail in menuItems
    val dismissLabel = stringResource(R.string.drawer_close)
    Dialog(
        onDismissRequest = onBack,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false),
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = 0.28f),
                    ).clickable(onClick = onClose)
                    .semantics {
                        contentDescription =
                            dismissLabel
                    },
            )
            val margin = if (maxWidth < 600.dp) 12.dp else 24.dp
            val panelWidth = minOf(374.dp, maxWidth - margin * 2)
            Surface(
                modifier =
                    Modifier
                        .align(Alignment.CenterStart)
                        .padding(margin)
                        .width(panelWidth)
                        .fillMaxHeight()
                        .pointerInput(Unit) { detectTapGestures { } }
                        .onPreviewKeyEvent {
                            if (it.key == Key.Escape && it.type == KeyEventType.KeyUp) {
                                onBack()
                                true
                            } else {
                                false
                            }
                        },
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (destination != DrawerDestination.MENU) {
                            TextButton(onClick = onBack, modifier = Modifier.focusRequester(initialFocus)) {
                                Text(stringResource(R.string.drawer_back))
                            }
                        }
                        Text(
                            stringResource(destination.title()),
                            Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        TextButton(
                            onClick = onClose,
                            modifier =
                                if (destination == DrawerDestination.MENU &&
                                    !focusMenuItem
                                ) {
                                    Modifier.focusRequester(initialFocus)
                                } else {
                                    Modifier
                                },
                        ) {
                            Text(stringResource(R.string.close))
                        }
                    }
                    if (simulated) {
                        Text(
                            stringResource(R.string.simulation_label),
                            Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    HorizontalDivider()
                    if (destination == DrawerDestination.MENU) {
                        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp)) {
                            menuItems.forEach { item ->
                                TextButton(
                                    onClick = { onNavigate(item) },
                                    modifier =
                                        Modifier.fillMaxWidth().then(
                                            if (lastDetail ==
                                                item
                                            ) {
                                                Modifier.focusRequester(initialFocus)
                                            } else {
                                                Modifier
                                            },
                                        ),
                                ) {
                                    Text(stringResource(item.title()), Modifier.weight(1f))
                                    Text("›")
                                }
                            }
                        }
                    } else {
                        Box(Modifier.weight(1f)) { content() }
                    }
                }
            }
            // The target is in BoxWithConstraints' subcomposition; focus only after it is composed.
            LaunchedEffect(destination) {
                if (destination != DrawerDestination.MENU) lastDetail = destination
                initialFocus.requestFocus()
            }
        }
    }
}

private fun DrawerDestination.title(): Int =
    when (this) {
        DrawerDestination.CLOSED, DrawerDestination.MENU -> R.string.drawer_menu
        DrawerDestination.PET_INFO -> R.string.drawer_pet_info
        DrawerDestination.QUESTS -> R.string.drawer_quests
        DrawerDestination.VEHICLE_INFO -> R.string.drawer_vehicle_info
        DrawerDestination.APPEARANCE -> R.string.drawer_appearance
        DrawerDestination.SETTINGS -> R.string.drawer_settings
    }
