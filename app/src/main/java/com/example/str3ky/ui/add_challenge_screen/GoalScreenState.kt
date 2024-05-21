package com.example.str3ky.ui.add_challenge_screen

import com.example.str3ky.data.DayProgress
import com.example.str3ky.data.Goal
import com.example.str3ky.data.Occurrence
import java.time.Duration

data class GoalScreenState(
    val goalName: String = "",
    val frequency: Occurrence = Occurrence.DAILY,
    val focusTime: Long = 1800000,
    val alarmTime: Long? = null,
    val startDate: Long = System.currentTimeMillis(),
    val progress: List<DayProgress> = emptyList(),
    val goalDuration: Int = 30
)

data class GoalState(
    val goal:Goal? = null
)