package com.monsters.mobimon.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.geometry.Rect

/**
 * A feature owns the presentation and state for its registered destinations.
 * Keep virtual Compose parameters explicit: the pinned compiler does not bridge inherited default arguments correctly.
 */
interface FeatureEntry {
    val routes: Set<AppRoute>

    @Composable
    @UiComposable
    fun Content(
        route: AppRoute,
        navigator: FeatureNavigator,
        modifier: Modifier,
    )
}

/** Shell events only; feature state and repositories never cross this boundary. */
data class FeatureNavigator(
    val navigate: (AppRoute) -> Unit,
    val back: () -> Unit,
    val returnHome: () -> Unit,
    val openMenu: () -> Unit,
    /** Trigger bounds in the Compose root, sampled on activation rather than during layout. */
    val navigateFrom: (AppRoute, Rect) -> Unit = { route, _ -> navigate(route) },
)

/** Rejects missing or ambiguous registrations before rendering a destination. */
class FeatureRegistry(
    entries: Set<FeatureEntry>,
) {
    private val destinations =
        buildMap {
            require(
                AppRoute.entries
                    .map { it.name }
                    .distinct()
                    .size == AppRoute.entries.size,
            ) {
                "Destination names must be unique for saved-state restoration"
            }
            entries.forEach { entry ->
                require(entry.routes.isNotEmpty()) { "A feature must register a destination" }
                entry.routes.forEach { route ->
                    require(put(route, entry) == null) { "Duplicate destination: $route" }
                }
            }
            require(keys == AppRoute.entries.toSet()) { "Missing destinations: ${AppRoute.entries.toSet() - keys}" }
        }

    operator fun get(route: AppRoute): FeatureEntry = destinations.getValue(route)
}
