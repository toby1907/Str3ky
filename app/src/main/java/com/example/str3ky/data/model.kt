package com.example.str3ky.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.str3ky.theme.BabyBlue
import com.example.str3ky.theme.LightGreen
import com.example.str3ky.theme.RedOrange
import com.example.str3ky.theme.RedPink
import com.example.str3ky.theme.Violet
import java.time.DayOfWeek

@Entity
data class Goal(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "goal_id")
    var id: Int? = null,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "duration") val durationInfo: Duration,
    @ColumnInfo(name = "occurrence") val occurrence: OccurrenceSelection,
    @ColumnInfo(name = "alarm_time") val alarmTime: Long?,
    @ColumnInfo(name = "start_date") val startDate: Long,
    @ColumnInfo(name = "progress") var progress: List<DayProgress>,
    @ColumnInfo(name = "color") var color: Int,
    @ColumnInfo(name = "completed") var completed: Boolean,
    @ColumnInfo(name = "no_of_days") var noOfDays: Int,
) {
    companion object {
        val goalColors = listOf(RedOrange, LightGreen, Violet, BabyBlue, RedPink)
        val no_of_days = listOf(7, 14, 30, 60, 100)
    }
}

class InvalidGoalException(message: String) : Exception(message)

enum class Occurrence {
    DAILY,
    DAILY_WITHOUT_WEEKEND,
    CUSTOM
}

enum class DayOfWeek {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY
}

data class OccurrenceSelection(
    val dayOption: Occurrence,
    val selectedDays: Set<com.example.str3ky.data.DayOfWeek>
)

data class DayProgress(
    var date: Long, // date in milliseconds
    var completed: Boolean // whether the goal was completed on this day
)

data class Duration(
    val isCompleted: Boolean,
    var countdownTime: Long
)
