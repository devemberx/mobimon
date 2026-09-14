package com.monsters.mobimon.core.domain

fun interface UtcClock {
    fun nowEpochMillis(): Long
}
