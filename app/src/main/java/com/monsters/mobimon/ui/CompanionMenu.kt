package com.monsters.mobimon.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.monsters.mobimon.R
import com.monsters.mobimon.core.navigation.AppRoute
import com.monsters.mobimon.core.navigation.CompanionRoute
import com.monsters.mobimon.core.navigation.QuestRoute
import com.monsters.mobimon.core.navigation.VehicleRoute
import com.monsters.mobimon.core.ui.MobiMonButton
import com.monsters.mobimon.core.ui.MobiMonButtonStyle
import com.monsters.mobimon.core.ui.MobiMonDimensions
import com.monsters.mobimon.core.ui.MobiMonPanel

/** Existing menu actions on v4 primitives; final menu composition remains feature work. */
@Composable
fun CompanionMenu(
    onClose: () -> Unit,
    onNavigate: (AppRoute) -> Unit,
) {
    val first = remember { FocusRequester() }
    val closeDescription = stringResource(R.string.close)
    BoxWithConstraints(Modifier.fillMaxSize().testTag("menu-host")) {
        val windowWidth = maxWidth
        val windowHeight = maxHeight
        Popup(onDismissRequest = onClose, properties = PopupProperties(focusable = true)) {
            // AAOS Popup constraints can exceed the app's compatibility-density window.
            Box(Modifier.size(windowWidth, windowHeight)) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f),
                        ).clickable(onClick = onClose),
                )
                MobiMonPanel(
                    Modifier
                        .safeDrawingPadding()
                        .padding(MobiMonDimensions.contentPadding)
                        .widthIn(max = 560.dp)
                        .testTag("companion-menu"),
                ) {
                    Column(
                        Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(MobiMonDimensions.contentGap),
                    ) {
                        Text(
                            stringResource(R.string.drawer_menu),
                            Modifier.semantics {
                                heading()
                            },
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        listOf(
                            R.string.drawer_menu_vehicle to VehicleRoute.VEHICLE_INFO,
                            R.string.drawer_menu_quests to QuestRoute.QUESTS,
                            R.string.drawer_settings to CompanionRoute.SETTINGS,
                        ).forEachIndexed { index, (title, route) ->
                            MobiMonButton(
                                { onNavigate(route) },
                                Modifier.fillMaxWidth().then(
                                    if (index ==
                                        0
                                    ) {
                                        Modifier.focusRequester(first)
                                    } else {
                                        Modifier
                                    },
                                ),
                                style = MobiMonButtonStyle.SECONDARY,
                            ) { Text(stringResource(title)) }
                        }
                        MobiMonButton(
                            onClose,
                            Modifier.align(Alignment.End).semantics {
                                contentDescription =
                                    closeDescription
                            },
                            style = MobiMonButtonStyle.SECONDARY,
                        ) {
                            Text(closeDescription)
                        }
                    }
                }
            }
            LaunchedEffect(first) {
                withFrameNanos { }
                first.requestFocus()
            }
        }
    }
}
