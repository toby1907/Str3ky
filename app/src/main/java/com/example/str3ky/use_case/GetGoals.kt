package com.example.str3ky.use_case

import android.util.Log
import com.example.str3ky.data.Goal
import com.example.str3ky.repository.GoalRepositoryImpl
import com.example.str3ky.use_case.util.GoalOrder
import com.example.str3ky.use_case.util.OrderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetGoals(
    private val repository: GoalRepositoryImpl
) {

    operator fun invoke(
        goalOrder: GoalOrder = GoalOrder.Date(OrderType.Descending),
        userId: Int,
    ): Flow<List<Goal>> {

        return repository.getGoalsForUser(userId).map { goals ->
            when(goalOrder.orderType) {
                is OrderType.Ascending -> {
                    when(goalOrder) {
                        is GoalOrder.Title -> goals.sortedBy { it.title.lowercase() }
                        is GoalOrder.Date -> goals.sortedBy { it.startDate }
                        is GoalOrder.Color -> goals.sortedBy { it.color }
                    }
                }
                is OrderType.Descending -> {
                    when(goalOrder) {
                        is GoalOrder.Title -> goals.sortedByDescending { it.title.lowercase() }
                        is GoalOrder.Date -> goals.sortedByDescending { it.startDate}
                        is GoalOrder.Color -> goals.sortedByDescending { it.color
                        }
                    }
                }
            }
        }
    }
}
