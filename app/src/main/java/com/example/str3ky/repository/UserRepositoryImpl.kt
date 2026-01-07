package com.example.str3ky.repository

import com.example.str3ky.data.Goal
import com.example.str3ky.data.User
import com.example.str3ky.data.UserDao
import com.example.str3ky.data.Achievement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import android.util.Log


class UserRepositoryImpl @Inject constructor(private val userDao: UserDao) : UserRepository {
    private val mutex = Mutex()

    override suspend fun save(user: User) {
        userDao.insertUser(user)
    }

    override fun getUser(): Flow<List<User>> {
        return userDao.getUser()
    }

    override suspend fun deleteUsers(vararg users: User) {
        userDao.deleteUsers(*users)
    }

    override suspend fun update(user: User) {
        userDao.updateUser(user)
    }

    override suspend fun updateUserAtomically(transform: suspend (User) -> User) {
        // Use a coroutine Mutex so we can safely call suspend functions inside the critical section
        mutex.withLock {
            val users = userDao.getUser().first()
            if (users.isNotEmpty()) {
                val current = users[0]
                val merged = transform(current)
                userDao.updateUser(merged)
            }
        }
    }

    override suspend fun addAchievementsAtomically(achievementsToAdd: List<Achievement>): List<Achievement> {
        return mutex.withLock {
            val users = userDao.getUser().first()
            if (users.isEmpty()) return@withLock emptyList()
            val current = users[0]
            val existingNames = current.achievementsUnlocked.map { it.name }.toSet()
            val actuallyToAdd = achievementsToAdd.filter { it.name !in existingNames }
            if (actuallyToAdd.isEmpty()) {
                Log.d("UserRepo", "addAchievementsAtomically: nothing to add (requested=${achievementsToAdd.map { it.name }})")
                return@withLock emptyList()
            }
            val merged = (current.achievementsUnlocked + actuallyToAdd).distinctBy { it.name }
            val updated = current.copy(achievementsUnlocked = merged)
            userDao.updateUser(updated)
            Log.d("UserRepo", "addAchievementsAtomically: added=${actuallyToAdd.map { it.name }}")
            actuallyToAdd
        }
    }
}
















