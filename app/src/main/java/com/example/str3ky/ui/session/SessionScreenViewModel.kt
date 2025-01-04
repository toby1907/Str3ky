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
import com.example.str3ky.data.TimerState
import com.example.str3ky.repository.GoalRepositoryImpl
import com.example.str3ky.ui.add_challenge_screen.GoalState
import com.example.str3ky.ui.nav.DONE_SCREEN
import com.example.str3ky.ui.nav.SESSION_SCREEN
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
    val timerState: StateFlow<TimerState> = countdownTimerManager.timerState
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

   /* var progressDate = mutableStateOf(0L)
        private set*/
     /* var sessionDuration = mutableStateOf(-1)
        private set*/

  /*  var dayHourSpent = mutableStateOf(0L)
    private set
*/
    init {

        savedStateHandle.get<Int>("goalId")?.let { goalId ->
            if (goalId != -1) {
                if(countdownTimerManager.timerState.value==TimerState.Initial)   {
                    currentGoalId = goalId
                    countdownTimerManager.goalId.value = goalId
                    viewModelScope.launch {
                        goalRepository.getGoal(goalId).collect { goal ->
                            countdownTimerManager._goalState.value =
                                countdownTimerManager._goalState.value.copy(goal = goal)
                            if (goal != null) {
                                countdownTimerManager.dayProgressFlow.value = goal.progress


                            }
                        }
                    }
                }
            }

        }
        savedStateHandle.get<Int>("totalSessions").let {
            if (it != -1) {
                if (it != null) {
                    viewModelScope.launch {
                        if(countdownTimerManager.timerState.value==TimerState.Initial)  {
                            countdownTimerManager._totalNoOfSessions.value = it
                            countdownTimerManager.totalFocusSetFlow.value = it
                            countdownTimerManager.totalBreakSetFlow.value =
                                if (it > 1) it - 1 else 0
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
        }
        savedStateHandle.get<Int>("sessionDuration").let {
            if (it != -1) {
                if (it != null) {

                  if(countdownTimerManager.timerState.value==TimerState.Initial)  {
                        viewModelScope.launch {
                            // Update the value
                            countdownTimerManager.currentTimeTargetInMillisFlow.value =
                                (it * 60000).toLong()
                            countdownTimerManager.timeLeftInMillisFlow.value = (it * 60000).toLong()
                            countdownTimerManager._sessionTotalDurationMillis.value =
                                (it * 60000).toLong()
                            Log.d("sessionInVMScope", "$it")
                            Log.d("sessionDuration", "$it")
                            countdownTimerManager.sessionDuration.value = it
                        }

                        /*    _countdownTimeMillis.value = _countdownTimeMillis.value.copy(
                                countdownTimeMillis = (it * 60000).toLong()
                            )*/

                        Log.d("sessionDuration", "$it")
                        countdownTimerManager.sessionDuration.value = it
                    }

                }
            }

        }
        savedStateHandle.get<Long>("progressDate")?.let { date ->
            if (date != 0L) {

                if(countdownTimerManager.timerState.value==TimerState.Initial)  {
                    countdownTimerManager.progressDate.value = date
               // countdownTimerManager.setProgressDate(date)
                countdownTimerManager.work = {
                    it-> countdownTimerManager.onDayChallengeCompleted(it)

                }
                }
                /*viewModelScope.launch {
                    goalRepository.getGoal(currentGoalId!!).collect { goal ->
                        if (goal != null) {
                            countdownTimerManager.dayHourSpent.value =
                                goal.progress.find { it.date == countdownTimerManager.progressDate.value }?.hoursSpent ?: 0L
                        }
                    }
                }*/

            }
        }

       /* // Listen to timerFinishedEvent
        viewModelScope.launch {
            countdownTimerManager.timerFinishedEvent.collectLatest { isSessionCompleted ->
                Log.d("SessionScreenViewModel", "timerFinishedEvent received. isSessionCompleted: $isSessionCompleted")
                if (isSessionCompleted) {

                    countdownTimerManager.popUpLambda?.invoke(
                        DONE_SCREEN + "?goalId=${countdownTimerManager.goalId}&sessionDuration=${countdownTimerManager._sessionTotalDurationMillis.value}&progressDate=${countdownTimerManager.progressDate.value}",
                        SESSION_SCREEN
                    )
                } else {

                    Log.d("SessionScreenViewModel", "Phase completed")
                }
            }
        }*/
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

        if (state || countdownTimerManager.timerState.value==TimerState.Running) {

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

    /*private fun onDayChallengeCompleted(change: Boolean) {
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


    }*/


}
