package com.example.str3ky.repository

import com.example.str3ky.data.Goal
import com.example.str3ky.data.User
import com.example.str3ky.data.UserDao
import kotlinx.coroutines.flow.Flow


class UserRepositoryImpl(private val userDao: UserDao) : UserRepository {
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
}
