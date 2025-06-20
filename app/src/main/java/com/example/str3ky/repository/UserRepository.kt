package com.example.str3ky.repository

import com.example.str3ky.data.Goal
import com.example.str3ky.data.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun save(user: User)
    fun getUser(): Flow<List<User>>
    suspend  fun deleteUsers(vararg users: User)
    suspend fun update(user: User)
}
