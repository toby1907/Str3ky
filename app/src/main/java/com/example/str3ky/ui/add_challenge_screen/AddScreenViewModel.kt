package com.example.str3ky.ui.add_challenge_screen

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.str3ky.convertToDayOfWeekSet
import com.example.str3ky.data.DayOfWeek
import com.example.str3ky.data.DayProgress
import com.example.str3ky.data.Duration
import com.example.str3ky.data.Goal
import com.example.str3ky.data.InvalidGoalException
import com.example.str3ky.data.Occurrence
import com.example.str3ky.data.OccurrenceSelection
import com.example.str3ky.getAbbreviation
import com.example.str3ky.minutesToMilliseconds
import com.example.str3ky.repository.GoalRepositoryImpl
import com.example.str3ky.repository.UserRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject


@HiltViewModel
class AddScreenViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val goalRepository: GoalRepositoryImpl,
    private val userRepository: UserRepositoryImpl
) : ViewModel() {

    private var currentGoalId: Int? = null
    private var userId = mutableStateOf(0)

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
    private val _selectedDays =
        mutableStateOf(listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"))


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

                viewModelScope.launch {
                    goalRepository.getGoal(goalId).collect { goal ->
                        _goalState.value = goalState.value.copy(goal = goal)
                        if (goal != null) {
                            currentGoalId = goal.id
                        }
                        if (goal != null) {
                            _goalName.value = _goalName.value.copy(goalName = goal.title)
                        }
                        if (goal != null) {
                            _frequency.value = _frequency.value.copy(frequency = goal.occurrence)
                        }
                        if (goal != null) {
                            _focusTime.value = _focusTime.value.copy(focusTime = goal.durationInfo)
                        }
                        if (goal != null) {
                            _alarmTime.value = _alarmTime.value.copy(alarmTime = goal.alarmTime)
                        }
                        if (goal != null) {
                            _startDate.value = _startDate.value.copy(startDate = goal.startDate)
                        }
                        if (goal != null) {
                            _goalColor.intValue = goal.color
                        }
                        if (goal != null) {
                            _goalCompleted.value = goal.completed
                        }
                        if (goal != null) {
                            userId.value = goal.userId
                        }

                    }

                }

            }


        }
        viewModelScope.launch {

            val user = userRepository.getUser().first()
            if (user.isNotEmpty()) {
                if (user[0].id != null) {
                    userId.value = user[0].id!!
                }
            }

        }
    }


    fun onEvent(event: AddChallengeEvent) {

        when (event) {

            is AddChallengeEvent.EnteredName -> {
                _goalName.value = _goalName.value.copy(
                    goalName = event.value
                )
            }

            is AddChallengeEvent.AlarmTime -> {
                _alarmTime.value = _alarmTime.value.copy(
                    alarmTime = event.time
                )
            }

            is AddChallengeEvent.ChangeColor -> {
                _goalColor.intValue = event.color
            }

            is AddChallengeEvent.Completed -> {
                _goalCompleted.value = event.completed
            }

            is AddChallengeEvent.DeleteGoal -> {
                /*  viewModelScope.launch {
                      event.goal?.let { goalRepository.delete(it) }
                      recentlyGoal = event.goal
                  }*/
            }

            is AddChallengeEvent.EnteredDate -> {
                _startDate.value = _startDate.value.copy(
                    startDate = event.value
                )
            }

            is AddChallengeEvent.Error -> {
                viewModelScope.launch {
                    _eventFlow.emit(
                        UiEvent.ShowSnackbar(event.message)
                    )
                }

            }

            is AddChallengeEvent.FocusTime -> {
                _focusTime.value = _focusTime.value.copy(
                    focusTime = event.focusDuration
                )
            }

            is AddChallengeEvent.Frequency -> {
                _frequency.value = _frequency.value.copy(
                    frequency = event.frequency
                )
                _selectedDays.value = _frequency.value.frequency.selectedDays.map {
                    getAbbreviation(it)
                }
            }

            AddChallengeEvent.SaveNote -> {
                viewModelScope.launch {
                    try {
                        val progressList = generateProgress(
                            noOfDays = noOfDays.value.noOfDays,
                            selectedDays = convertToDayOfWeekSet(selectedDays.value).toList()
                        )
                        val goal = Goal(
                            id = currentGoalId,
                            title = goalName.value.goalName,
                            durationInfo = focusTime.value.focusTime,
                            occurrence = frequency.value.frequency,
                            alarmTime = alarmTime.value.alarmTime,
                            startDate = startDate.value.startDate,
                            progress = progressList,
                            color = goalColor.value,
                            completed = goalCompleted.value,
                            noOfDays = noOfDays.value.noOfDays,
                            userId = userId.value
                        )
                        goalRepository.save(goal)
                        _eventFlow.emit(UiEvent.SaveNote)
                        _eventFlow.emit(UiEvent.ShowSnackbar("Challenge saved!"))
                    } catch (e: InvalidGoalException) {

                        Log.d("Goal", "could not save")
                        _eventFlow.emit(
                            UiEvent.ShowSnackbar(
                                message = e.message ?: "Couldn't save note"
                            )
                        )

                    }
                }
            }
        }
    }

    fun onColorSelected(color: Int) {
        _goalColor.intValue = color
    }

    fun onNoOfDaysSelected(noOfDays: Int) {
        _noOfDays.value = _noOfDays.value.copy(
            noOfDays = noOfDays
        )
    }

    fun addDay(day: String) {
        if (!selectedDays.value.contains(day)) {
            _selectedDays.value += day

            val occurrence =
                OccurrenceSelection(Occurrence.CUSTOM, convertToDayOfWeekSet(_selectedDays.value))
            _frequency.value = _frequency.value.copy(
                frequency = occurrence
            )
        }
    }

    fun removeDay(day: String) {
        _selectedDays.value = _selectedDays.value - day
        if (_selectedDays.value.isNotEmpty()) {
            val occurrence =
                OccurrenceSelection(Occurrence.CUSTOM, convertToDayOfWeekSet(_selectedDays.value))
            _frequency.value = _frequency.value.copy(
                frequency = occurrence
            )
        } else {
            val occurrence =
                OccurrenceSelection(
                    Occurrence.DAILY,
                    setOf(
                        DayOfWeek.MONDAY,
                        DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY,
                        DayOfWeek.SATURDAY,
                        DayOfWeek.SUNDAY,
                    )
                )
            _frequency.value = _frequency.value.copy(
                frequency = occurrence
            )
        }
    }

    fun timerIncrement() {
        _focusTime.value = _focusTime.value.copy(
            focusTime = Duration(
                isCompleted = false,
                countdownTime = focusTime.value.focusTime.countdownTime + minutesToMilliseconds(10)
            )
        )
    }

    fun timerDecrement() {
        _focusTime.value = _focusTime.value.copy(
            focusTime = Duration(
                isCompleted = false,
                countdownTime = focusTime.value.focusTime.countdownTime - minutesToMilliseconds(10)
            )
        )
    }

    fun generateProgress(noOfDays: Int, selectedDays: List<DayOfWeek>): List<DayProgress> {

        val progressList = mutableListOf<DayProgress>()

        // Calculate the number of days for each selected day
        val daysPerSelectedDay = noOfDays / selectedDays.size

        // Initialize the date (you can set it to the current date or any other start date)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()

        // Generate progress for each selected day
        for (day in selectedDays) {
            for (i in 0 until daysPerSelectedDay) {
                progressList.add(DayProgress(calendar.timeInMillis, false))
                calendar.add(Calendar.DAY_OF_MONTH, 1) // Add one day
            }
        }

        return progressList
    }

}
