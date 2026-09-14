package com.monsters.mobimon.core.domain

fun interface Clock {
    fun nowMillis(): Long
}

fun interface IdGenerator {
    fun nextId(): String
}

data class ProgressionIdentity(
    val profileId: String,
    val source: SignalSource,
)
