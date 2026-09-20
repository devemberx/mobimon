package com.monsters.mobimon.core.ui

import androidx.annotation.DrawableRes

/** Shared artwork mapping; missing time uses Night without reading a clock during composition. */
@DrawableRes
fun companionBackgroundRes(timeOfDay: String?): Int {
    val value = timeOfDay?.trim()?.lowercase(java.util.Locale.ROOT)
    return when (value) {
        "morning", "아침" -> R.drawable.pet_home_background_morning
        "day", "낮" -> R.drawable.pet_home_background_day
        "afternoon", "오후", "늦은 오후" -> R.drawable.pet_home_background_afternoon
        "sunset", "노을", "저녁" -> R.drawable.pet_home_background_sunset
        "night", "밤" -> R.drawable.pet_home_background_night
        else ->
            when (value?.toIntOrNull()) {
                in 6..11 -> R.drawable.pet_home_background_morning
                in 12..15 -> R.drawable.pet_home_background_day
                in 16..17 -> R.drawable.pet_home_background_afternoon
                in 18..19 -> R.drawable.pet_home_background_sunset
                else -> R.drawable.pet_home_background_night
            }
    }
}
