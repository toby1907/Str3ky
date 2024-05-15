package com.example.str3ky.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Goal(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "duration") val duration: Long,
    @ColumnInfo(name = "occurrence") val occurrence: Occurrence,
    @ColumnInfo(name = "alarm_time") val alarmTime: Long?,
    @ColumnInfo(name = "start_date") val startDate: Long,
    @ColumnInfo(name = "progress") val progress: List<DayProgress>
)
enum class Occurrence {
    DAILY,
    WEEKLY,
    MONTHLY
}

data class DayProgress(
    val date: Long, // date in milliseconds
    val completed: Boolean // whether the goal was completed on this day
)
