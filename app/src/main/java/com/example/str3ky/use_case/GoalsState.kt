package com.example.str3ky.use_case

import com.example.str3ky.data.Goal
import com.example.str3ky.use_case.util.GoalOrder
import com.example.str3ky.use_case.util.OrderType

data class GoalsState(
    val goals: List<Goal> = emptyList(),
    val goalOrder: GoalOrder = GoalOrder.Date(OrderType.Descending),
    val isOrderSectionVisible: Boolean = false
)