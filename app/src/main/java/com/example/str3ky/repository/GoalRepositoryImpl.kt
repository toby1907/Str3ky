package com.example.str3ky.repository

import com.example.str3ky.data.Goal
import com.example.str3ky.data.GoalDao
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.Executors

class GoalRepositoryImpl (private val goalDao: GoalDao):
    GoalRepository {

    private val SINGLE_EXECUTOR = Executors.newSingleThreadExecutor()
    fun executeThread(f: () -> Unit) {
        SINGLE_EXECUTOR.execute(f)
    }

    override fun getGoal(id: Int): Flow<Goal?> {
        return goalDao.getLetter(id)
    }

    override fun delete(goal: Goal) = executeThread{
        goalDao.delete(goal)
    }
    override suspend fun save(goal: Goal)= executeThread {
        goalDao.insert(goal)
    }
    override suspend fun update(goal: Goal) = executeThread {
        goalDao.update(goal)
    }

    override fun getAllGoals(): Flow<List<Goal>> {
        return   goalDao.getAllLetters()
    }





}