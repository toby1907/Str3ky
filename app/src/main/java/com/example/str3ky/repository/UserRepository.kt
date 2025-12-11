package com.example.str3ky.repository

import com.example.str3ky.data.Goal
import com.example.str3ky.data.User
import com.example.str3ky.data.Achievement
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun save(user: User)
    fun getUser(): Flow<List<User>>
    suspend  fun deleteUsers(vararg users: User)
    suspend fun update(user: User)
    // Atomically read & update the current user.
    suspend fun updateUserAtomically(transform: suspend (User) -> User)

    // Atomically add achievements to the current user and return the list of achievements that were actually added
    suspend fun addAchievementsAtomically(achievementsToAdd: List<Achievement>): List<Achievement>
}
