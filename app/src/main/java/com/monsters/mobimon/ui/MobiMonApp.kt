package com.monsters.mobimon.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monsters.mobimon.R
import com.monsters.mobimon.core.domain.AppUseState
import com.monsters.mobimon.core.domain.CosmeticSlot
import com.monsters.mobimon.core.domain.PointEconomy
import com.monsters.mobimon.core.navigation.AiRoute
import com.monsters.mobimon.core.navigation.AppRoute
import com.monsters.mobimon.core.navigation.CompanionRoute
import com.monsters.mobimon.core.navigation.FeatureEntry
import com.monsters.mobimon.core.navigation.FeatureNavigator
import com.monsters.mobimon.core.navigation.FeatureRegistry
import com.monsters.mobimon.core.presentation.CompanionAppearancePresentation
import com.monsters.mobimon.core.ui.MobiMonContentColumn
import com.monsters.mobimon.core.ui.MobiMonMessage
import com.monsters.mobimon.core.ui.MobiMonTheme
import com.monsters.mobimon.runtime.AppUseStateSource

internal const val NAVIGATION_MOTION_DURATION_MILLIS = 220

@Composable
fun MobiMonApp(
    entries: Set<FeatureEntry>,
    appUse: AppUseStateSource,
    points: PointEconomy,
) {
    val state by appUse.states.collectAsStateWithLifecycle()
    val appearance = remember(points) { CompanionAppearancePresentation(points) }.state()
    val activeFriendId = appearance.inventory?.equippedItemIds?.get(CosmeticSlot.FRIEND)
    MobiMonContent(
        entries = entries,
        appUseState = state,
        activeFriendId = activeFriendId,
        activeAccessoryId = appearance.accessoryId,
        activeOutfitId = appearance.outfitId,
        activeBackgroundId = appearance.backgroundId,
    )
}

internal val ShellSaver =
    listSaver<ShellState, String>(
        save = { listOf(it.route.name, it.connectionOrigin.name) },
        restore = { saved ->
            val routeIndex = if (saved.firstOrNull() in setOf("PET", "VEHICLE")) 1 else 0
            val originIndex = routeIndex + 1
            ShellState(
                route = AppRoute.entries.firstOrNull { it.name == saved.getOrNull(routeIndex) } ?: CompanionRoute.HOME,
                connectionOrigin =
                    when (saved.getOrNull(originIndex)) {
                        CompanionRoute.SETTINGS.name -> CompanionRoute.SETTINGS
                        AiRoute.CONVERSATION.name -> AiRoute.CONVERSATION
                        else -> CompanionRoute.HOME
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
    activeFriendId: String? = null,
    activeAccessoryId: String? = null,
    activeOutfitId: String? = null,
    activeBackgroundId: String? = null,
    debugOverlay: @Composable () -> Unit = { DebugOverlay() },
) {
    val registry = remember(entries) { FeatureRegistry(entries) }
    var shell by rememberSaveable(stateSaver = ShellSaver) { mutableStateOf(ShellState()) }
    var returning by remember { mutableStateOf(false) }
    val stateHolder = rememberSaveableStateHolder()
    val navigator =
        FeatureNavigator(
            navigate = { route ->
                returning = false
                shell =
                    if (route == AiRoute.COPILOT) shell.openCopilot() else shell.navigate(route)
            },
            back = {
                val previous = shell
                shell = previous.back()
                if (shell.route != previous.route) returning = true
            },
            returnHome = {
                returning = true
                shell = shell.returnHome()
            },
            openMenu = { shell = shell.openMenu() },
        )
    MobiMonTheme {
        Surface(modifier = modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxSize().safeDrawingPadding()) {
                    if (appUseState != AppUseState.ALLOWED) {
                        MobiMonContentColumn {
                            Text(
                                stringResource(R.string.app_use_paused),
                                style = MaterialTheme.typography.headlineMedium,
                            )
                            MobiMonMessage(stringResource(R.string.app_use_restricted))
                        }
                    } else {
                        BackHandler(enabled = shell.menuOpen || shell.route != CompanionRoute.HOME) {
                            navigator.back()
                        }
                        AnimatedContent(
                            targetState = shell.route,
                            modifier = Modifier.fillMaxSize(),
                            contentKey = { it.name },
                            transitionSpec = {
                                val direction = if (returning) -1 else 1
                                val motion =
                                    tween<IntOffset>(
                                        NAVIGATION_MOTION_DURATION_MILLIS,
                                        easing = FastOutSlowInEasing,
                                    )
                                val fade = tween<Float>(NAVIGATION_MOTION_DURATION_MILLIS, easing = FastOutSlowInEasing)
                                (
                                    (
                                        slideInHorizontally(motion) { direction * it / 18 } + fadeIn(fade)
                                    ) togetherWith
                                        (slideOutHorizontally(motion) { -direction * it / 36 } + fadeOut(fade))
                                ).using(null).apply { targetContentZIndex = 1f }
                            },
                            label = "destination change",
                        ) { route ->
                            val active = route == shell.route
                            val routeNavigator =
                                FeatureNavigator(
                                    navigate = { if (active) navigator.navigate(it) },
                                    back = { if (active) navigator.back() },
                                    returnHome = { if (active) navigator.returnHome() },
                                    openMenu = { if (active) navigator.openMenu() },
                                )
                            Box(
                                Modifier.fillMaxSize().then(if (active) Modifier else Modifier.clearAndSetSemantics {}),
                            ) {
                                stateHolder.SaveableStateProvider(route.name) {
                                    registry[route].Content(route, routeNavigator, Modifier)
                                }
                                if (!active) {
                                    Box(
                                        Modifier
                                            .fillMaxSize()
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null,
                                            ) {}
                                            .clearAndSetSemantics {},
                                    )
                                }
                            }
                        }
                    }
                }
                if (appUseState == AppUseState.ALLOWED) {
                    CompanionMenu(
                        visible = shell.menuOpen,
                        currentRoute = shell.route,
                        onClose = navigator.back,
                        onNavigate = navigator.navigate,
                        activeFriendId = activeFriendId,
                        accessoryId = activeAccessoryId,
                        outfitId = activeOutfitId,
                        backgroundId = activeBackgroundId,
                    )
                }
                if (appUseState == AppUseState.ALLOWED) {
                    debugOverlay()
                }
            }
        }
    }
}
