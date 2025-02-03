package com.example.str3ky.ui.done

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.str3ky.data.DayProgress
import com.example.str3ky.data.Goal
import com.example.str3ky.repository.GoalRepositoryImpl
import com.example.str3ky.ui.add_challenge_screen.GoalScreenState
import com.example.str3ky.ui.add_challenge_screen.GoalState
import com.example.str3ky.ui.add_challenge_screen.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DoneScreenViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val goalRepository: GoalRepositoryImpl,
) : ViewModel() {

    private var currentGoalIdFlow = MutableStateFlow(-1)
    val currentGoalId: StateFlow<Int> = currentGoalIdFlow

    private val completedStreakFlow = MutableStateFlow(
        0
    )
    val completedStreak: StateFlow<Int> = completedStreakFlow

    private val _goalName = mutableStateOf(
        GoalScreenState()
    )

    private val _frequency = mutableStateOf(
        GoalScreenState()
    )
    private val _focusTime = mutableStateOf(
        GoalScreenState()
    )
    var sessionDurationState = mutableStateOf(
        0L
    )
        private set

    private val _goalCompleted = mutableStateOf(false)


    private val _progress = mutableStateOf(emptyList<DayProgress>())
    private val goalState = mutableStateOf(GoalState())


    val goalName: State<GoalScreenState> = _goalName
    val frequency: State<GoalScreenState> = _frequency
    val focusTime: State<GoalScreenState> = _focusTime
   val progress: State<List<DayProgress>> = _progress
    val goalCompleted: State<Boolean> = _goalCompleted
    val goal: State<GoalState> = goalState
    var progressDate = mutableStateOf(0L)
        private set
    var dayHourSpent = mutableStateOf(0L)
        private set

    init {
        savedStateHandle.get<Int>("goalId")?.let { goalId ->

            if (goalId != -1) {
                currentGoalIdFlow.value = goalId
                viewModelScope.launch {
                    goalRepository.getGoal(goalId).collect{ goal ->
                        if(goal!=null){
                           // currentGoalId = goal.id
                        }
                        if (goal!=null){
                            _goalName.value = _goalName.value.copy(goalName=goal.title)
                        }
                        if (goal!=null){
                            _frequency.value = _frequency.value.copy(frequency = goal.occurrence)
                        }
                        if (goal!=null){
                            _focusTime.value = _focusTime.value.copy(focusTime = goal.durationInfo)
                        }

                        if (goal!=null){
                            _goalCompleted.value = goal.completed
                        }
                        if (goal!=null){
                            _progress.value = goal.progress

                          val streak = streakCalculator(goal.progress)
                            completedStreakFlow.value = streak
                        }
                        if (goal!=null){
                          goalState.value = goalState.value.copy(
                              goal = goal
                          )
                        }



                    }
                }

            }
        }
        savedStateHandle.get<Long>("sessionCompleted")?.let {sessionDuration->
            if (sessionDuration != 0L) {
sessionDurationState.value = sessionDuration
            }
        }
        savedStateHandle.get<Long>("progressDate")?.let { date ->
            if (date != 0L) {
                progressDate.value = date
               if (currentGoalId.value != -1) {
                    viewModelScope.launch {
                        goalRepository.getGoal(currentGoalId.value).collect { goal ->
                            if (goal != null) {
                                dayHourSpent.value =
                                    goal.progress.find { it.date == progressDate.value }?.hoursSpent
                                        ?: 0L


                            }

                            if (goal != null) {
                                Log.d("dayHourSpentGoal", "${   goal.progress.find { it.date == progressDate.value }}")
                            }
                        }

                    }
                }
                Log.d("dayHourSpent", "${date},${currentGoalId.value}")
            }
        }
    }


    private fun streakCalculator(dayProgress: List<DayProgress>): Int {
        val sortedList = dayProgress.sortedBy { it.date }

        var currentStreak = 0
        var maxStreak = 0

        for (i in sortedList.indices) {
            if (sortedList[i].completed) {
                currentStreak++
                maxStreak = maxOf(maxStreak, currentStreak) // Update max streak
            } else {
                currentStreak = 0
            }
        }

        return maxStreak
    }

}