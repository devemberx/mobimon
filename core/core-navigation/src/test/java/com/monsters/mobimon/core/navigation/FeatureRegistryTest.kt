package com.monsters.mobimon.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class FeatureRegistryTest {
    @Test
    fun savedDestinationNamesAreGloballyUniqueAcrossFeatureEnums() {
        assertEquals(
            AppRoute.entries.size,
            AppRoute.entries
                .map { it.name }
                .toSet()
                .size,
        )
    }

    @Test
    fun routesResolveToTheirRegisteredOwnerRegardlessOfRegistrationOrder() {
        val entries = AppRoute.entries.associateWith { Entry(setOf(it)) }
        val registry = FeatureRegistry(entries.values.reversed().toSet())
        entries.forEach { (route, owner) -> assertSame(owner, registry[route]) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun duplicateDestinationsFailInsteadOfChoosingAnArbitraryOwner() {
        FeatureRegistry(setOf(Entry(AppRoute.entries.toSet()), Entry(setOf(QuestRoute.QUESTS))))
    }

    @Test(expected = IllegalArgumentException::class)
    fun missingDestinationsFailBeforeNavigation() {
        FeatureRegistry(setOf(Entry(setOf(CompanionRoute.HOME))))
    }

    private class Entry(
        override val routes: Set<AppRoute>,
    ) : FeatureEntry {
        @Composable
        override fun Content(
            route: AppRoute,
            navigator: FeatureNavigator,
            modifier: Modifier,
        ) = Unit
    }
}
