package com.example.str3ky.repository

import com.example.str3ky.data.Goal
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun getGoal(id: Int): Flow<Goal?>
    suspend  fun delete(goal: Goal)
    suspend  fun save(goal: Goal,callback: (Int) -> Unit)
    suspend fun update(goal: Goal)
    fun getAllGoals() : Flow<List<Goal>>
    fun getGoalsForUser(userId: Int): Flow<List<Goal>>
}