package com.example.goalgetta.Database.Dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import com.example.goalgetta.model.Goal

interface GoalDao {

    @Query("SELECT * FROM goals")
    fun getAllGoals(): DataSource.Factory<Int, Goal>

    @Query("SELECT * FROM tasks WHERE isCompleted = 1")
    fun getCompletedGoals(): DataSource.Factory<Int, Goal>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0")
    fun getActiveGoals(): DataSource.Factory<Int, Goal>

    @Query("SELECT * FROM tasks WHERE id = :goalId")
    fun getGoalById(goalId: Int) : LiveData<Goal>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 LIMIT 6")
    fun getNearestActiveGoals(): List<Goal>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal)

    @Update
    suspend fun updateGoal(goal: Goal)

    @Delete
    suspend fun deleteGoal(goal: Goal)
}