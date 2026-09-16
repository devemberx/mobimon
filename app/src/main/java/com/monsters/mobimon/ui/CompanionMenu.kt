package com.monsters.mobimon.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.monsters.mobimon.R
import com.monsters.mobimon.core.navigation.AiRoute
import com.monsters.mobimon.core.navigation.AppRoute
import com.monsters.mobimon.core.navigation.CompanionRoute
import com.monsters.mobimon.core.navigation.QuestRoute
import com.monsters.mobimon.core.navigation.VehicleRoute

private data class DrawerDestination(
    val label: Int,
    val icon: Int,
    val route: AppRoute,
)

private val destinations =
    listOf(
        DrawerDestination(R.string.drawer_menu_home, R.drawable.drawer_home_v4, CompanionRoute.HOME),
        DrawerDestination(R.string.drawer_menu_chat, R.drawable.drawer_chat_v4, AiRoute.COPILOT),
        DrawerDestination(R.string.drawer_menu_quests, R.drawable.drawer_quest_v4, QuestRoute.QUESTS),
        DrawerDestination(R.string.drawer_menu_vehicle, R.drawable.drawer_vehicle_v4, VehicleRoute.VEHICLE_INFO),
        DrawerDestination(R.string.drawer_menu_appearance, R.drawable.drawer_appearance_v4, CompanionRoute.APPEARANCE),
        DrawerDestination(R.string.drawer_settings, R.drawable.drawer_settings_v4, CompanionRoute.SETTINGS),
    )

/** Portrait resources are selected by saved friend ID, never embedded in the drawer. */
private fun drawerProfile(friendId: String?): Pair<Int, Int> =
    when (friendId) {
        "friend:mobi" -> R.drawable.drawer_mobi_v4 to R.string.drawer_mobi_name
        "friend:luna" -> R.drawable.drawer_luna_v4 to R.string.drawer_luna_name
        else -> R.drawable.drawer_mobi_v4 to R.string.drawer_no_friend
    }

@Composable
fun CompanionMenu(
    currentRoute: AppRoute,
    onClose: () -> Unit,
    onNavigate: (AppRoute) -> Unit,
    activeFriendId: String? = null,
) {
    val first = remember { FocusRequester() }
    val closeDescription = stringResource(R.string.close)
    val (portrait, name) = drawerProfile(activeFriendId)
    var clickedRoute by remember { mutableStateOf<AppRoute?>(null) }

    LaunchedEffect(clickedRoute) {
        val route = clickedRoute
        if (route != null) {
            repeat(9) {
                withFrameNanos { }
            }
            onNavigate(route)
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize().testTag("menu-host")) {
        val windowWidth = maxWidth
        val windowHeight = maxHeight
        Popup(onDismissRequest = onClose, properties = PopupProperties(focusable = true)) {
            // AAOS Popup constraints may exceed the app compatibility-density window.
            Box(Modifier.size(windowWidth, windowHeight)) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.58f))
                        .clickable(onClick = onClose)
                        .testTag("menu-backdrop"),
                )
                AnimatedVisibility(
                    visible = true,
                    enter = slideInHorizontally(animationSpec = tween(280), initialOffsetX = { -it }),
                ) {
                    Column(
                        Modifier
                            .width((windowWidth * 0.27f).coerceIn(320.dp, 690.dp))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(topEnd = 56.dp, bottomEnd = 56.dp))
                            .background(Color(0xFF13263F))
                            .testTag("companion-menu")
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 60.dp, vertical = 32.dp),
                    ) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "MobiMon",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(stringResource(R.string.drawer_tagline), color = Color(0xFFB9C9E1))
                            }
                            Text(
                                "×",
                                Modifier
                                    .size(76.dp)
                                    .clickable(onClick = onClose)
                                    .semantics { contentDescription = closeDescription },
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color(0xFFF7FAFF),
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                        ) {
                            Image(
                                painterResource(portrait),
                                null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(109.dp),
                            )
                            Column {
                                Text(
                                    stringResource(name),
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier.semantics { heading() },
                                )
                                Text(
                                    stringResource(R.string.drawer_profile_subtitle),
                                    color = Color(0xFF9FB2CE),
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                )
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                        destinations.forEachIndexed { index, item ->
                            val selected =
                                if (clickedRoute != null) {
                                    clickedRoute == item.route
                                } else {
                                    item.route == currentRoute
                                }
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(if (selected) Color(0xFF244563) else Color.Transparent)
                                    .then(if (index == 0) Modifier.focusRequester(first) else Modifier)
                                    .clickable {
                                        if (clickedRoute == null) {
                                            clickedRoute = item.route
                                        }
                                    }.padding(horizontal = 24.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(22.dp),
                            ) {
                                Image(painterResource(item.icon), null, modifier = Modifier.size(56.dp))
                                Text(
                                    stringResource(item.label),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = if (selected) Color(0xFF82D4FF) else Color(0xFFF1F5FC),
                                )
                            }
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
