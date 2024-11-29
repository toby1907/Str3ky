package com.example.str3ky

import com.example.str3ky.data.DayOfWeek.*
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
fun convertCalendarDayOfWeekToStrings(dayInt:Int): String{
    return when (dayInt) {
        1 ->"Sun"
        2  ->"Mon"
        3 ->"Tue"
        4 -> "Wed"
        5 ->"Thu"
        6 -> "Fri"
        7->  "Sat"
        else -> ""
    }
}

fun convertToDayOfWeekSet(dayAbbreviations: List<String>): Set<com.example.str3ky.data.DayOfWeek> {
    return dayAbbreviations.map { getDayOfWeek(it) }.toSet()
}


fun formatMillisecondsToTimeString(
    milliseconds: Long,
    showHoursWithoutSeconds: Boolean = false
): String {
    val secondsAdjusted = (milliseconds + 999) / 1000
    val s = secondsAdjusted % 60
    val m = (secondsAdjusted / 60) % 60
    val h = secondsAdjusted / (60 * 60)
    return if (h < 1) {
        if (!showHoursWithoutSeconds) {
            String.format("%d:%02d", m, s)
        } else {
            String.format("%d:%02d", 0, m)
        }
    } else {
        if (!showHoursWithoutSeconds) {
            String.format("%d:%02d:%02d", h, m, s)
        } else {
            String.format("%d:%02d", h, m)
        }
    }
}

fun formatMinutesToTimeString(minutes: Int, showHoursWithoutSeconds: Boolean = false): String =
    formatMillisecondsToTimeString(minutesToMilliseconds(minutes), showHoursWithoutSeconds)


/*
fun Int.minutesToMilliseconds(): Long = this * 60_000L

fun Long.millisecondsToMinutes(): Int = (this / 60_000L).toInt()
*/
