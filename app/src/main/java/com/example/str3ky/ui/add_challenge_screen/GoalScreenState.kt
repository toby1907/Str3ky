package com.example.str3ky.ui.add_challenge_screen

import com.example.str3ky.data.DayProgress
import com.example.str3ky.data.Duration
import com.example.str3ky.data.Goal
import com.example.str3ky.data.Occurrence
import com.example.str3ky.data.OccurrenceSelection

data class GoalScreenState(
    val goalName: String = "",
    val frequency: OccurrenceSelection = OccurrenceSelection(Occurrence.DAILY, emptySet()),
    val focusTime: Duration = Duration(isCompleted = false, countdownTime = 1800000),
    val alarmTime: Long? = null,
    val startDate: Long = System.currentTimeMillis(),
    val progress: List<DayProgress> = emptyList(),
    val noOfDays: Int = 30
)

data class GoalState(
    val goal:Goal? = null
)