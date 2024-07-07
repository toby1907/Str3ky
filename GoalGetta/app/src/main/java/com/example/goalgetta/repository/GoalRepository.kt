package com.example.goalgetta.repository


import android.content.Context
import androidx.lifecycle.LiveData
import androidx.paging.*
import com.example.goalgetta.Database.Dao.GoalDao
import com.example.goalgetta.model.Goal


class GoalRepository(private val goalDao: GoalDao) {

    companion object {
        const val PAGE_SIZE = 30
        const val PLACEHOLDERS = true

        private val PAGING_CONFIG = PagedList.Config.Builder().apply {
            setEnablePlaceholders(PLACEHOLDERS)
            setPageSize(PAGE_SIZE)
        }.build()


        @Volatile
        private var instance: GoalRepository? = null

        fun getInstance(context: Context): GoalRepository {
            return instance ?: synchronized(this) {
                if (instance == null) {
                    val database = GoalDatabase.getInstance(context)
                    instance = GoalRepository(database.goalDao())
                }
                return instance as GoalRepository
            }

        }
    }

    fun getAllGoals() : LiveData<PagedList<Goal>> {
        val pagingSource = goalDao.getAllGoals()
        return LivePagedListBuilder(pagingSource, PAGING_CONFIG).build()
    }

    fun getCompletedGoals() : LiveData<PagedList<Goal>> {
        val pagingSource = goalDao.getCompletedGoals()
        return  LivePagedListBuilder(pagingSource, PAGING_CONFIG).build()
    }

    fun getActiveGoals() : LiveData<PagedList<Goal>> {
        val pagingSource = goalDao.getActiveGoals()
        return LivePagedListBuilder(pagingSource, PAGING_CONFIG).build()
    }

    fun getNearestActiveGoal(): List<Goal> {
        return  goalDao.getNearestActiveGoals()
    }

    fun getGoalById(goalId: Int): LiveData<Goal> {
        return goalDao.getGoalById(goalId)
    }


    suspend fun insertGoal(newGoal: Goal) {
        return goalDao.insertGoal(newGoal)
    }

    suspend fun deleteGoal(goal: Goal) {
     goalDao.deleteGoal(goal)
    }

    suspend fun updateGoal(goal: Goal) {
        goalDao.updateGoal(goal)
    }


}