package com.example.str3ky.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.str3ky.theme.BabyBlue
import com.example.str3ky.theme.LightGreen
import com.example.str3ky.theme.RedOrange
import com.example.str3ky.theme.RedPink
import com.example.str3ky.theme.Violet

@Entity
data class Goal(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "duration") val duration: Long,
    @ColumnInfo(name = "occurrence") val occurrence: Occurrence,
    @ColumnInfo(name = "alarm_time") val alarmTime: Long?,
    @ColumnInfo(name = "start_date") val startDate: Long,
    @ColumnInfo(name = "progress") val progress: List<DayProgress>,
    @ColumnInfo(name = "color") var color: Int,
    @ColumnInfo(name = "completed") var completed: Boolean,
){
    companion object {
        val goalColors = listOf(RedOrange, LightGreen, Violet, BabyBlue, RedPink)
    }
}

enum class Occurrence {
    DAILY,
    WEEKLY,
    MONTHLY
}

data class DayProgress(
    val date: Long, // date in milliseconds
    val completed: Boolean // whether the goal was completed on this day
)
