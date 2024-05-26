package com.example.str3ky.ui.add_challenge_screen

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.str3ky.data.DayOfWeek.*
import java.time.DayOfWeek
import java.util.concurrent.TimeUnit

fun minutesToMilliseconds(minutes: Int): Long {
    return TimeUnit.MINUTES.toMillis(minutes.toLong())
}

fun millisecondsToMinutes(milliseconds: Long): Int {
    return TimeUnit.MILLISECONDS.toMinutes(milliseconds).toInt()
}


fun getDayOfWeek(abbreviation: String): com.example.str3ky.data.DayOfWeek {
    return when (abbreviation) {
        "Mon" -> MONDAY
        "Tue" -> TUESDAY
        "Wed" -> WEDNESDAY
        "Thu" -> THURSDAY
        "Fri" -> FRIDAY
        "Sat" -> SATURDAY
        "Sun" -> SUNDAY
        else -> throw IllegalArgumentException("Invalid day abbreviation: $abbreviation")
    }
}

fun getAbbreviation(dayOfWeek: com.example.str3ky.data.DayOfWeek): String {
    return when (dayOfWeek) {
        MONDAY -> "Mon"
        TUESDAY -> "Tue"
        WEDNESDAY -> "Wed"
        THURSDAY -> "Thu"
        FRIDAY -> "Fri"
        SATURDAY -> "Sat"
        SUNDAY -> "Sun"
    }

}

fun convertToDayOfWeekSet(dayAbbreviations: List<String>): Set<com.example.str3ky.data.DayOfWeek> {
    return dayAbbreviations.map { getDayOfWeek(it) }.toSet()
}
