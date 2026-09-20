package com.monsters.mobimon.core.ui

import androidx.annotation.DrawableRes

/** Shared artwork mapping; missing time uses Night without reading a clock during composition. */
@DrawableRes
fun companionBackgroundRes(timeOfDay: String?): Int {
    val value = timeOfDay?.trim()?.lowercase(java.util.Locale.ROOT)
    return when (value) {
        "morning", "아침" -> R.drawable.pet_home_background_morning
        "day", "낮" -> R.drawable.pet_home_background_day
        "night", "밤" -> R.drawable.pet_home_background_night
        else ->
            when (value?.toIntOrNull()) {
                in 8..11 -> R.drawable.pet_home_background_morning
                in 12..18 -> R.drawable.pet_home_background_day
                else -> R.drawable.pet_home_background_night
            }
    }
}
