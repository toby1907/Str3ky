package com.example.str3ky.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class OccurrenceConverter {
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