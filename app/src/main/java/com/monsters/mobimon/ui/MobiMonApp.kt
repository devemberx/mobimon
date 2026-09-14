package com.monsters.mobimon.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monsters.mobimon.R
import com.monsters.mobimon.core.domain.AppUseState
import com.monsters.mobimon.core.navigation.AiRoute
import com.monsters.mobimon.core.navigation.AppRoute
import com.monsters.mobimon.core.navigation.CompanionRoute
import com.monsters.mobimon.core.navigation.FeatureEntry
import com.monsters.mobimon.core.navigation.FeatureNavigator
import com.monsters.mobimon.core.navigation.FeatureRegistry
import com.monsters.mobimon.core.navigation.HomeSurface
import com.monsters.mobimon.core.ui.MobiMonContentColumn
import com.monsters.mobimon.core.ui.MobiMonMessage
import com.monsters.mobimon.core.ui.MobiMonTheme
import com.monsters.mobimon.runtime.AppUseStateSource

@Composable
fun MobiMonApp(
    entries: Set<FeatureEntry>,
    appUse: AppUseStateSource,
) {
    val state by appUse.states.collectAsStateWithLifecycle()
    MobiMonContent(entries = entries, appUseState = state)
}

internal val ShellSaver =
    listSaver<ShellState, String>(
        save = { listOf(it.home.name, it.route.name, it.connectionOrigin.name) },
        restore = { saved ->
            ShellState(
                home = HomeSurface.entries.firstOrNull { it.name == saved.getOrNull(0) } ?: HomeSurface.PET,
                route = AppRoute.entries.firstOrNull { it.name == saved.getOrNull(1) } ?: CompanionRoute.HOME,
                connectionOrigin =
                    if (saved.getOrNull(2) ==
                        CompanionRoute.SETTINGS.name
                    ) {
                        CompanionRoute.SETTINGS
                    } else {
                        CompanionRoute.HOME
                    },
            )
        },
    )

/** App shell owns only navigation, restoration, menu and the global AAOS restriction gate. */
@Composable
fun MobiMonContent(
    entries: Set<FeatureEntry>,
    modifier: Modifier = Modifier,
    appUseState: AppUseState = AppUseState.UNAVAILABLE,
) {
    val registry = remember(entries) { FeatureRegistry(entries) }
    var shell by rememberSaveable(stateSaver = ShellSaver) { mutableStateOf(ShellState()) }
    val stateHolder = rememberSaveableStateHolder()
    MobiMonTheme {
        Surface(modifier = modifier.fillMaxSize()) {
            if (appUseState != AppUseState.ALLOWED) {
                MobiMonContentColumn {
                    Text(stringResource(R.string.app_use_paused), style = MaterialTheme.typography.headlineMedium)
                    MobiMonMessage(stringResource(R.string.app_use_restricted))
                }
            } else {
                BackHandler(enabled = shell.menuOpen || shell.route != CompanionRoute.HOME) { shell = shell.back() }
                val navigator =
                    FeatureNavigator(
                        home = shell.home,
                        navigate = { route ->
                            shell =
                                if (route == AiRoute.COPILOT) shell.openCopilot() else shell.navigate(route)
                        },
                        back = { shell = shell.back() },
                        returnHome = { shell = shell.returnHome() },
                        openMenu = { shell = shell.openMenu() },
                        switchHome = { shell = shell.switchHome() },
                    )
                stateHolder.SaveableStateProvider(shell.route.name) {
                    registry[shell.route].Content(shell.route, navigator, Modifier)
                }
                if (shell.menuOpen) {
                    CompanionMenu(onClose = navigator.back, onNavigate = navigator.navigate)
                }
            }
        }
    }
}
