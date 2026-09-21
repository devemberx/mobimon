package com.monsters.mobimon.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.monsters.mobimon.R
import com.monsters.mobimon.core.domain.AppUseState
import com.monsters.mobimon.core.domain.CosmeticSlot
import com.monsters.mobimon.core.domain.GitHubAuthentication
import com.monsters.mobimon.core.domain.GitHubSession
import com.monsters.mobimon.core.domain.SettingsRepository
import com.monsters.mobimon.core.navigation.AiRoute
import com.monsters.mobimon.core.navigation.AppRoute
import com.monsters.mobimon.core.navigation.CompanionRoute
import com.monsters.mobimon.core.navigation.FeatureEntry
import com.monsters.mobimon.core.navigation.FeatureNavigator
import com.monsters.mobimon.core.navigation.FeatureRegistry
import com.monsters.mobimon.core.presentation.CompanionAppearancePresentation
import com.monsters.mobimon.core.ui.LocalMobiMonMotionEnabled
import com.monsters.mobimon.core.ui.MobiMonContentColumn
import com.monsters.mobimon.core.ui.MobiMonMessage
import com.monsters.mobimon.core.ui.MobiMonTheme
import com.monsters.mobimon.runtime.AppUseStateSource

internal const val NAVIGATION_MOTION_DURATION_MILLIS = 220
internal const val CONVERSATION_REVEAL_DURATION_MILLIS = 300

private data class DestinationReveal(
    val route: AppRoute,
    val origin: Rect,
)

@Composable
fun MobiMonApp(
    entries: Set<FeatureEntry>,
    appUse: AppUseStateSource,
    companion: CompanionAppearancePresentation,
    settings: SettingsRepository,
    authentication: GitHubAuthentication,
) {
    val factory = remember(settings) { viewModelFactory { initializer { MotionPreferencesViewModel(settings) } } }
    val motion: MotionPreferencesViewModel = viewModel(factory = factory)
    val reducedMotion by motion.reducedMotion.collectAsStateWithLifecycle()
    val state by appUse.states.collectAsStateWithLifecycle()
    val session by authentication.session.collectAsStateWithLifecycle()
    val appearance = companion.state()
    val activeFriendId = appearance.inventory?.equippedItemIds?.get(CosmeticSlot.FRIEND)
    MobiMonContent(
        entries = entries,
        appUseState = state,
        activeFriendId = activeFriendId,
        activeAccessoryId = appearance.accessoryId,
        activeOutfitId = appearance.outfitId,
        activeBackgroundId = appearance.backgroundId,
        reducedMotion = reducedMotion,
        conversationAuthenticated = session is GitHubSession.Authenticated,
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
@OptIn(ExperimentalAnimationApi::class)
fun MobiMonContent(
    entries: Set<FeatureEntry>,
    modifier: Modifier = Modifier,
    appUseState: AppUseState = AppUseState.UNAVAILABLE,
    activeFriendId: String? = null,
    activeAccessoryId: String? = null,
    activeOutfitId: String? = null,
    activeBackgroundId: String? = null,
    reducedMotion: Boolean = false,
    conversationAuthenticated: Boolean = false,
    debugOverlay: @Composable () -> Unit = { DebugOverlay() },
) {
    val registry = remember(entries) { FeatureRegistry(entries) }
    var savedShell by rememberSaveable(stateSaver = ShellSaver) { mutableStateOf(ShellState()) }
    val shell = savedShell.requireConversationAccount(conversationAuthenticated)
    var returning by remember { mutableStateOf(false) }
    var reveal by remember { mutableStateOf<DestinationReveal?>(null) }
    var contentCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val stateHolder = rememberSaveableStateHolder()
    val navigator =
        FeatureNavigator(
            navigate = { route ->
                reveal = null
                returning = false
                savedShell = shell.navigate(route, conversationAuthenticated)
            },
            back = {
                val previous = shell
                savedShell = previous.back()
                if (savedShell.route != previous.route) returning = true
            },
            returnHome = {
                returning = true
                savedShell = shell.returnHome()
            },
            openMenu = { savedShell = shell.openMenu() },
            navigateFrom = { requested, bounds ->
                val next = shell.navigate(requested, conversationAuthenticated)
                val route = next.route
                val coordinates = contentCoordinates?.takeIf { it.isAttached }
                reveal =
                    if (
                        shell.route == CompanionRoute.HOME &&
                        route in setOf(AiRoute.COPILOT, AiRoute.CONVERSATION) &&
                        coordinates != null &&
                        !bounds.isEmpty
                    ) {
                        val local = bounds.translate(-coordinates.positionInRoot())
                        DestinationReveal(
                            route,
                            Rect(
                                local.left / coordinates.size.width,
                                local.top / coordinates.size.height,
                                local.right / coordinates.size.width,
                                local.bottom / coordinates.size.height,
                            ),
                        )
                    } else {
                        null
                    }
                returning = false
                savedShell = next
            },
        )
    CompositionLocalProvider(LocalMobiMonMotionEnabled provides !reducedMotion) {
        MobiMonTheme {
            Surface(modifier = modifier.fillMaxSize()) {
                Box(Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxSize().safeDrawingPadding().onGloballyPositioned { contentCoordinates = it }) {
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
                                    val anchored = reveal
                                    if (
                                        anchored != null &&
                                        setOf(initialState, targetState) == setOf(CompanionRoute.HOME, anchored.route)
                                    ) {
                                        (EnterTransition.None togetherWith ExitTransition.KeepUntilTransitionsFinished)
                                            .using(null)
                                            .apply {
                                                targetContentZIndex =
                                                    if (targetState == CompanionRoute.HOME) 0f else 1f
                                            }
                                    } else {
                                        val direction = if (returning) -1 else 1
                                        val motion =
                                            tween<IntOffset>(
                                                if (reducedMotion) 0 else NAVIGATION_MOTION_DURATION_MILLIS,
                                                easing = FastOutSlowInEasing,
                                            )
                                        val fade =
                                            tween<Float>(
                                                if (reducedMotion) 0 else NAVIGATION_MOTION_DURATION_MILLIS,
                                                easing = FastOutSlowInEasing,
                                            )
                                        (
                                            (
                                                slideInHorizontally(motion) { direction * it / 18 } + fadeIn(fade)
                                            ) togetherWith
                                                (slideOutHorizontally(motion) { -direction * it / 36 } + fadeOut(fade))
                                        ).using(null).apply { targetContentZIndex = 1f }
                                    }
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
                                        navigateFrom = { destination, bounds ->
                                            if (active) navigator.navigateFrom(destination, bounds)
                                        },
                                    )
                                val anchored = reveal?.takeIf { it.route == route }
                                val revealModifier =
                                    if (anchored != null) {
                                        val progress =
                                            transition.animateFloat(
                                                transitionSpec = {
                                                    tween(
                                                        if (reducedMotion) {
                                                            0
                                                        } else if (targetState ==
                                                            EnterExitState.Visible
                                                        ) {
                                                            CONVERSATION_REVEAL_DURATION_MILLIS
                                                        } else {
                                                            NAVIGATION_MOTION_DURATION_MILLIS
                                                        },
                                                        easing = FastOutSlowInEasing,
                                                    )
                                                },
                                                label = "conversation reveal",
                                            ) { state -> if (state == EnterExitState.Visible) 1f else 0f }
                                        Modifier.revealFrom(anchored.origin) { progress.value }
                                    } else {
                                        Modifier
                                    }
                                Box(
                                    Modifier.fillMaxSize().then(revealModifier).then(
                                        if (active) Modifier else Modifier.clearAndSetSemantics {},
                                    ),
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
}
