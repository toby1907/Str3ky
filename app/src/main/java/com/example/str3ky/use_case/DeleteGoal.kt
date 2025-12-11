package com.example.str3ky.use_case

import com.example.str3ky.data.Goal
import com.example.str3ky.repository.GoalRepository

class DeleteGoal(
    private val repository: GoalRepository
) {

    suspend operator fun invoke(note: Goal) {
        repository.delete(note)
    }
}