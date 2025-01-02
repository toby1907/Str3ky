package com.example.str3ky.ui.session

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.str3ky.core.notification.TimerServiceManager
import com.example.str3ky.data.CountdownTimerManager
import com.example.str3ky.data.DayProgress
import com.example.str3ky.data.Duration
import com.example.str3ky.data.Goal
import com.example.str3ky.repository.GoalRepositoryImpl
import com.example.str3ky.ui.add_challenge_screen.GoalState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionScreenViewModel
@Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val goalRepository: GoalRepositoryImpl,
    val countdownTimerManager: CountdownTimerManager

) : ViewModel() {
    private var currentGoalId: Int? = null


    private val currentTimeTargetInMillisFlow = MutableStateFlow(0L)
    val currentTimeTargetInMillis: Flow<Long> = currentTimeTargetInMillisFlow
    private val timeLeftInMillisFlow = MutableStateFlow(10000L)
    val timeLeftInMillis: Flow<Long> = timeLeftInMillisFlow
    private val
            currentphase = MutableStateFlow(
        CountdownTimerManager.Phase.FOCUS_SESSION
    )
    val currentPhase: StateFlow<CountdownTimerManager.Phase> = currentphase
    private val pauseResumeStateFlow = MutableStateFlow(false)
    val pauseResumeState: StateFlow<Boolean> = pauseResumeStateFlow

    private val _goalState = mutableStateOf(
        GoalState()
    )
    private val dayProgressFlow = MutableStateFlow(emptyList<DayProgress>())
    val dayProgress: StateFlow<List<DayProgress>> = dayProgressFlow

    var progressDate = mutableStateOf(0L)
        private set
      var sessionDuration = mutableStateOf(-1)
        private set

    var dayHourSpent = mutableStateOf(0L)
    private set

    init {

        savedStateHandle.get<Int>("goalId")?.let { goalId ->
            if (goalId != -1) {
                currentGoalId = goalId
countdownTimerManager.goalId = goalId
                viewModelScope.launch {
                    goalRepository.getGoal(goalId).collect { goal ->
                        _goalState.value = _goalState.value.copy(goal = goal)
                        if (goal != null) {
                            dayProgressFlow.value = goal.progress


                        }
                    }
                }
            }

        }
        savedStateHandle.get<Int>("totalSessions").let {
            if (it != -1) {
                if (it != null) {
                    viewModelScope.launch {
                        countdownTimerManager._totalNoOfSessions.value = it
                        countdownTimerManager.totalFocusSetFlow.value = it
                        countdownTimerManager.totalBreakSetFlow.value = if (it > 1) it - 1 else 0
                        countdownTimerManager._totalNoOfBreaks.value = if (it > 1) it - 1 else 0
                    }
                    /*_totalNoOfSessions.value = _totalNoOfSessions.value.copy(
                        totalSessions = it
                    )
                    _totalNoOfBreaks.value = _totalNoOfBreaks.value.copy(
                        totalBreaks = it - 1
                    )
                    Log.d("totalSessions", "${totalNoOfSessions.value} , ${totalNoOfBreaks.value}")*/
                }
            }
        }
        savedStateHandle.get<Int>("sessionDuration").let {
            if (it != -1) {
                if (it != null) {

                    viewModelScope.launch {
                        // Update the value
                        countdownTimerManager.currentTimeTargetInMillisFlow.value = 10000L
                        countdownTimerManager.timeLeftInMillisFlow.value = 10000L
                        countdownTimerManager._sessionTotalDurationMillis.value = 10000L
                        Log.d("sessionInVMScope", "$it")
                    }

                    /*    _countdownTimeMillis.value = _countdownTimeMillis.value.copy(
                            countdownTimeMillis = (it * 60000).toLong()
                        )*/

                    Log.d("sessionDuration", "$it")
                    sessionDuration.value = it

                }
            }

        }
        savedStateHandle.get<Long>("progressDate")?.let { date ->
            if (date != 0L) {
                progressDate.value = date
                countdownTimerManager.setProgressDate(date)
                countdownTimerManager.work = {
                    it-> onDayChallengeCompleted(it)

                }
                viewModelScope.launch {
                    goalRepository.getGoal(currentGoalId!!).collect { goal ->
                        if (goal != null) {
                            dayHourSpent.value =
                                goal.progress.find { it.date == progressDate.value }?.hoursSpent ?: 0L
                        }
                    }
                }

            }
        }
    }

    fun startSession(openAndPopUp: (String, String) -> Unit) {

        viewModelScope.launch {
            countdownTimerManager.startSession(openAndPopUp)
        }
    }

    fun cancelCountdown() {

        viewModelScope.launch {
            countdownTimerManager.resetCountdown()
        }
    }

    fun pauseResumeCountdown(state: Boolean, openAndPopUp: (String, String) -> Unit) {

        if (state) {

            viewModelScope.launch {
                countdownTimerManager.pauseCountdown()
            }
        }
        else {

            viewModelScope.launch {
                countdownTimerManager.resumeCountdown(openAndPopUp)
            }
        }
    }

    private fun onDayChallengeCompleted(change: Boolean) {
        dayHourSpent.value = sessionDuration.value.toLong()
        viewModelScope.launch {
            val progressList = dayProgressFlow.value.map {
                if (it.date == progressDate.value) {
                    it.copy(
                        date = progressDate.value,
                        completed = if(it.hoursSpent>=sessionDuration.value.toLong()) change else false,
                        hoursSpent = it.hoursSpent + dayHourSpent.value
                    )
                } else it

            }
            _goalState.value.goal?.let {

                goalRepository.save(
                    it.copy(
                        progress = progressList,
                        durationInfo = Duration(
                            countdownTime = it.durationInfo.countdownTime + sessionDuration.value.toLong(),
                            isCompleted = sessionDuration.value.toLong() == it.focusSet
                        ) )
                )
                Log.d("DayHourSpentFromSession","${dayHourSpent.value}")
            }

        }


    }


}
