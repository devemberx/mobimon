package com.monsters.mobimon.core.ui

import androidx.annotation.DrawableRes

/** Normalizes timeOfDay string/hour into standard period ("morning", "day", "afternoon", "sunset", "night"). */
fun companionTimePeriod(timeOfDay: String?): String {
    val value = timeOfDay?.trim()?.lowercase(java.util.Locale.ROOT)
    return when (value) {
        "morning", "아침" -> "morning"
        "day", "낮" -> "day"
        "afternoon", "오후", "늦은 오후" -> "afternoon"
        "sunset", "노을", "저녁" -> "sunset"
        "night", "밤" -> "night"
        else ->
            when (value?.toIntOrNull()) {
                in 6..11 -> "morning"
                in 12..15 -> "day"
                in 16..17 -> "afternoon"
                in 18..19 -> "sunset"
                else -> "night"
            }
    }
}

/** Shared artwork mapping; missing time uses Night without reading a clock during composition. */
@DrawableRes
fun companionBackgroundRes(timeOfDay: String?): Int =
    when (companionTimePeriod(timeOfDay)) {
        "morning" -> R.drawable.pet_home_background_morning
        "day" -> R.drawable.pet_home_background_day
        "afternoon" -> R.drawable.pet_home_background_afternoon
        "sunset" -> R.drawable.pet_home_background_sunset
        else -> R.drawable.pet_home_background_night
    }
