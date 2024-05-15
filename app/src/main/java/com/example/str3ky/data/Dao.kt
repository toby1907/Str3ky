package com.example.str3ky.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT* FROM `goal.db`WHERE goal_id =:goalId")
    fun getLetter(goalId: Int): Flow<Goal?>
    @Query("SELECT * FROM 'goal.db' ORDER By goal_id ASC" )
    fun getRecentGoal(): LiveData<List<Goal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(voiceJournal: Goal):Long
    @Insert
    fun insertAll(vararg goal: Goal)

    @Update
    fun update(goal: Goal)

    @Delete
    fun delete(goal: Goal)

    @Query("SELECT * FROM `goal.db`")
    fun getAllLetters(): Flow<List<Goal>>
}