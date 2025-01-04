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
import javax.inject.Inject

@HiltViewModel
class ProgressScreenViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val goalRepository: GoalRepositoryImpl,
) : ViewModel() {

    private var currentGoalId: Int? = null
private val _goalId = mutableIntStateOf(-1)
    private val _goalName = mutableStateOf(
        GoalScreenState()
    )

    private val _frequency = mutableStateOf(
        GoalScreenState()
    )
    private val _focusTime = mutableStateOf(
        GoalScreenState()
    )
    private val _alarmTime = mutableStateOf(
        GoalScreenState()
    )
    private val _startDate = mutableStateOf(
        GoalScreenState()
    )
    private val _goalState = mutableStateOf(
        GoalState()
    )
    private val _goalColor = mutableIntStateOf(Goal.goalColors.random().toArgb())

    private val _goalCompleted = mutableStateOf(false)

    private val _eventFlow = MutableSharedFlow<UiEvent>()

    private val _progress = mutableStateOf(emptyList<DayProgress>())
    private val _noOfDays = mutableStateOf(GoalScreenState())
    private val _selectedDays = mutableStateOf(emptyList<String>())


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
goalRepository.getGoal(goalId).collect{ goal ->
    _goalState.value = goalState.value.copy(goal = goal)
    if(goal!=null){
        currentGoalId = goal.id

    }
    if (goal!=null){
        _goalName.value = _goalName.value.copy(goalName=goal.title)
    }
    if (goal!=null){
        _frequency.value = _frequency.value.copy(frequency = goal.occurrence)
    }
    if (goal!=null){
        _focusTime.value = _focusTime.value.copy(totalTime = goal.focusSet)
    }
    if (goal!=null){
        _alarmTime.value = _alarmTime.value.copy(alarmTime=goal.alarmTime)
    }
    if (goal!=null){
        _startDate.value = _startDate.value.copy(startDate=goal.startDate)
    }
    if (goal!=null){
        _goalColor.intValue = goal.color
    }
    if (goal!=null){
        _goalCompleted.value = goal.completed
    }
    if (goal!=null){
        _progress.value = goal.progress
    }



}
                }

            }
        }
    }

    fun onDayChallengeCompleted(change:Boolean,currentGoal:Goal){

        viewModelScope.launch {
           goalRepository.save(
                currentGoal.copy(
                   progress = progress.value.toList()
                )
            )

        }


    }
}