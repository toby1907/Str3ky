package com.example.str3ky.ui.progress

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
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

fun Long.toMinutes(): Long = if (this <= 0L) 0L else this / 60000L

@HiltViewModel
class ProgressScreenViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val goalRepository: GoalRepositoryImpl,
) : ViewModel() {


    private var currentGoalId: Int? = null
    private val _goalId = mutableIntStateOf(-1)
    private val _goalName = mutableStateOf(GoalScreenState())
    private val _frequency = mutableStateOf(GoalScreenState())
    private val _focusTime = mutableStateOf(GoalScreenState())
    private val _alarmTime = mutableStateOf(GoalScreenState())
    private val _startDate = mutableStateOf(GoalScreenState())
    private val _goalState = mutableStateOf(GoalState())
    private val _goalColor = mutableIntStateOf(Goal.goalColors.random().toArgb())
    private val _goalCompleted = mutableStateOf(false)
    private val _eventFlow = MutableSharedFlow<UiEvent>()
    private val _progress = mutableStateOf(emptyList<DayProgress>())
    private val _noOfDays = mutableStateOf(GoalScreenState())
    private val _selectedDays = mutableStateOf(emptyList<String>())

    // New properties for "attempted but not completed" message
    private val _activeProgressDate = mutableStateOf<Long?>(null)
    val activeProgressDate: State<Long?> = _activeProgressDate

    private val _showAttemptedNotCompletedMessage = mutableStateOf(false)
    val showAttemptedNotCompletedMessage: State<Boolean> = _showAttemptedNotCompletedMessage

    private val _remainingTimeToCompleteDailyGoal = mutableStateOf(0L) // In minutes
    val remainingTimeToCompleteDailyGoal: State<Long> = _remainingTimeToCompleteDailyGoal


    val goalId: State<Int> = _goalId
    val goalName: State<GoalScreenState> = _goalName
    val frequency: State<GoalScreenState> = _frequency
    val focusTime: State<GoalScreenState> = _focusTime
    val alarmTime: State<GoalScreenState> = _alarmTime
    val startDate: State<GoalScreenState> = _startDate
    val goalState: State<GoalState> = _goalState
    val goalColor: State<Int> = _goalColor
    val progress: State<List<DayProgress>> = _progress
    val goalCompleted: State<Boolean> = _goalCompleted
    val noOfDays: State<GoalScreenState> = _noOfDays
    val eventFlow = _eventFlow.asSharedFlow()
    val selectedDays: State<List<String>> = _selectedDays


    private var recentlyGoal: Goal? = null


    init {
        savedStateHandle.get<Int>("goalId")?.let { goalId ->

            if (goalId != -1) {
                _goalId.value = goalId
                viewModelScope.launch {
                    goalRepository.getGoal(goalId).collect { goal ->
                        _goalState.value = _goalState.value.copy(goal = goal)
                        if (goal != null) {
                            _goalName.value = _goalName.value.copy(goalName = goal.title)
                            _frequency.value = _frequency.value.copy(frequency = goal.occurrence)
                            _focusTime.value = _focusTime.value.copy(totalTime = goal.focusSet)
                            _alarmTime.value = _alarmTime.value.copy(alarmTime = goal.alarmTime)
                            _startDate.value = _startDate.value.copy(startDate = goal.startDate)
                            _goalColor.intValue = goal.color
                            _goalCompleted.value = goal.completed
                            _progress.value = goal.progress

                            checkAttemptedNotCompletedStatus()
                        } else {
                            _showAttemptedNotCompletedMessage.value = false
                            _remainingTimeToCompleteDailyGoal.value = 0L
                        }


                    }
                }

            }
        }

        val passedProgressDate = savedStateHandle.get<Long>("progressDate")
        if (passedProgressDate != null && passedProgressDate != 0L) {
            _activeProgressDate.value = passedProgressDate
        } else {
            _activeProgressDate.value = getStartOfDayInMillis()
        }

        if (_goalState.value.goal != null && _activeProgressDate.value != null) {
            checkAttemptedNotCompletedStatus()
        }
    }

    private fun getStartOfDayInMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun checkAttemptedNotCompletedStatus() {
        val currentGoal = _goalState.value.goal
        val dateToCheck = _activeProgressDate.value

        if (currentGoal != null && dateToCheck != null) {
            val dayProgressEntry = currentGoal.progress.find { it.date == dateToCheck }

            if (dayProgressEntry != null) {
                val dailyTargetMinutes = currentGoal.focusSet.toMinutes()
                val hoursSpentMinutes = dayProgressEntry.hoursSpent.toMinutes()

                if (hoursSpentMinutes > 0 && !dayProgressEntry.completed && hoursSpentMinutes < dailyTargetMinutes) {
                    _showAttemptedNotCompletedMessage.value = true
                    _remainingTimeToCompleteDailyGoal.value = dailyTargetMinutes - hoursSpentMinutes
                } else {
                    _showAttemptedNotCompletedMessage.value = false
                    _remainingTimeToCompleteDailyGoal.value = 0L
                }
            } else {
                _showAttemptedNotCompletedMessage.value = false
                _remainingTimeToCompleteDailyGoal.value = 0L
            }
        } else {
            _showAttemptedNotCompletedMessage.value = false
            _remainingTimeToCompleteDailyGoal.value = 0L // Ensure it's reset if no goal/date
        }
    }

    // You might want a public method to manually refresh this if underlying data can change
    // from an external source or another part of the app not directly tied to init.
    fun refreshDailyAttemptStatus() {
        // Could re-fetch activeProgressDate or assume it's correctly set
        // For simplicity, just re-run the check with current values:
        checkAttemptedNotCompletedStatus()
    }
}