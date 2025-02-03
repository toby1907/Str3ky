package com.example.str3ky.use_case

import com.example.str3ky.data.Goal
import com.example.str3ky.data.InvalidGoalException
import com.example.str3ky.repository.GoalRepositoryImpl

class AddGoal(
    private val repository: GoalRepositoryImpl
) {

    @Throws(InvalidGoalException::class)
    suspend operator fun invoke(goal: Goal) {
        if(goal.title.isBlank()) {
            throw InvalidGoalException("The title of the note can't be empty.")
        }
//        if(goal.content.isBlank()) {
//            throw InvalidGoalException("The content of the note can't be empty.")
//        }
        repository.save(goal){goalId ->
            goal.id = goalId
        }
    }
}