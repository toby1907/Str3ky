package com.example.str3ky.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/*class OccurrenceConverter {
    @TypeConverter
    fun fromOccurrence(occurrence: Occurrence): Int {
        return occurrence.ordinal // Convert enum to integer
    }

    @TypeConverter
    fun toOccurrence(value: Int): Occurrence {
        return when (value) {
            Occurrence.DAILY.ordinal -> Occurrence.DAILY
            Occurrence.WEEKLY.ordinal -> Occurrence.WEEKLY
            Occurrence.MONTHLY.ordinal -> Occurrence.MONTHLY
            else -> throw IllegalArgumentException("Invalid occurrence value: $value")
        }
    }
}*/
class OccurrenceSelectionConverter {

    @TypeConverter
    fun fromOccurrenceSelection(selection: OccurrenceSelection): String {
        val dayOption = selection.dayOption.name
        val selectedDays = selection.selectedDays.joinToString(",") { it.name }
        return "$dayOption|$selectedDays"
    }

    @TypeConverter
    fun toOccurrenceSelection(value: String): OccurrenceSelection {
        val parts = value.split("|")
        val dayOption = Occurrence.valueOf(parts[0])
        val selectedDays = parts[1].split(",").map { com.example.str3ky.data.DayOfWeek.valueOf(it) }.toSet()
        return OccurrenceSelection(dayOption, selectedDays)
    }
}
class ProgressConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromProgressList(progress: List<DayProgress>): String {
        return gson.toJson(progress)
    }

    @TypeConverter
    fun toProgressList(value: String): List<DayProgress> {
        val type = object : TypeToken<List<DayProgress>>() {}.type
        return gson.fromJson(value, type)
    }
}

class DurationTypeConverter {
    @TypeConverter
    fun fromDuration(duration: Duration): Long {
        // Convert Duration to Long (e.g., using countdownTime)
        return duration.countdownTime
    }

    @TypeConverter
    fun toDuration(countdownTime: Long): Duration {
        // Convert Long to Duration
        return Duration(isCompleted = false, countdownTime = countdownTime)
    }
}