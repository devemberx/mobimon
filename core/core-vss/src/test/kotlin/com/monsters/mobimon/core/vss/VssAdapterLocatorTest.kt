package com.monsters.mobimon.core.vss

import org.junit.Assert.assertNull
import org.junit.Test

class VssAdapterLocatorTest {
    @Test
    fun missingClosedNetworkAdapterReturnsNull() {
        assertNull(VssAdapterLocator.createOrNull(Any()))
    }
}
