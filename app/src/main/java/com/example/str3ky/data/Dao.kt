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
    @Query("SELECT* FROM goal WHERE goal_id =:goalId")
    fun getLetter(goalId: Int): Flow<Goal?>
    @Query("SELECT * FROM goal ORDER By goal_id ASC" )
    fun getRecentGoal(): LiveData<List<Goal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun  insert(goal: Goal):Long
    @Insert
    fun insertAll(vararg goal: Goal)

    @Update
    fun update(goal: Goal)

    @Delete
    fun delete(goal: Goal)

    @Query("SELECT * FROM goal")
    fun getAllLetters(): Flow<List<Goal>>
    @Query("SELECT * FROM goal WHERE user_id = :userId")
     fun getGoalsForUser(userId: Int): Flow<List<Goal>>
}
@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM user_table")
     fun getUser(): Flow<List<User>>

    @Delete
    suspend   fun deleteUsers(vararg users: User)

 }