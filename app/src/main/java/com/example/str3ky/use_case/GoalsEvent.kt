package com.example.str3ky.use_case

import com.example.str3ky.data.Goal
import com.example.str3ky.use_case.util.GoalOrder

sealed class  GoalsEvent {
    data class Order(val goalOrder: GoalOrder): GoalsEvent()
    data class DeleteGoal(val goal: Goal): GoalsEvent()
    object RestoreNote: GoalsEvent()
    object ToggleOrderSection: GoalsEvent()
}
