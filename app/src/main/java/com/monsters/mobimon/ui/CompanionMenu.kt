package com.monsters.mobimon.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.monsters.mobimon.R

/** The home menu is transient. Destinations are rendered by the shell in the full content area. */
@Composable
fun CompanionMenu(
    onClose: () -> Unit,
    onNavigate: (AppRoute) -> Unit,
) {
    val focus = remember { FocusRequester() }
    Dialog(onDismissRequest = onClose) {
        Surface(shape = MaterialTheme.shapes.extraLarge) {
            Column(
                Modifier.widthIn(max = 440.dp).fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(stringResource(R.string.drawer_menu), style = MaterialTheme.typography.headlineMedium)
                listOf(AppRoute.VEHICLE_INFO, AppRoute.QUESTS, AppRoute.SETTINGS).forEachIndexed { index, route ->
                    TextButton(
                        onClick = { onNavigate(route) },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = 76.dp)
                                .then(if (index == 0) Modifier.focusRequester(focus) else Modifier),
                    ) {
                        Text(stringResource(route.title()), style = MaterialTheme.typography.titleLarge)
                    }
                }
                TextButton(onClick = onClose, modifier = Modifier.fillMaxWidth().heightIn(min = 76.dp)) {
                    Text(stringResource(R.string.close))
                }
            }
        }
        LaunchedEffect(Unit) { focus.requestFocus() }
    }
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
