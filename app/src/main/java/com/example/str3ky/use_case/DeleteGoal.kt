package com.example.str3ky.use_case

import com.example.str3ky.data.Goal
import com.example.str3ky.repository.GoalRepository
import com.example.str3ky.repository.GoalRepositoryImpl

class DeleteGoal(
    private val repository: GoalRepositoryImpl
) {

    suspend operator fun invoke(note: Goal) {
        repository.delete(note)
    }
}