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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private val _sessionCompleted = MutableSharedFlow<Boolean>(replay = 0)
    val sessionCompleted: SharedFlow<Boolean> = _sessionCompleted.asSharedFlow()

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

        // Listen for combinedFlow transitions to COMPLETED and emit a single-shot event
        viewModelScope.launch {
            var previousPhase = countdownTimerManager.currentPhase.value
            countdownTimerManager.combinedFlow.collectLatest { combined ->
                val currentPhase = combined.currentPhase
                if (previousPhase != CountdownTimerManager.Phase.COMPLETED && currentPhase == CountdownTimerManager.Phase.COMPLETED) {
                    Log.d("SessionScreenVM", "session completed detected - emitting sessionCompleted event")
                    _sessionCompleted.emit(true)
                }
                previousPhase = currentPhase
            }
        }

        savedStateHandle.get<Int>("goalId")?.let { goalId ->
            if (goalId != -1) {
                // Always update manager goalId and load the goal state so UI doesn't show stale completed state
                // If we're switching to a different goal than the manager currently has, reset manager state
                val previousGoal = countdownTimerManager.goalId.value
                if (previousGoal != goalId) {
                    // Reset manager state for the new goal to avoid showing stale COMPLETED UI
                    // resetCountdown is synchronous (stops service, clears timers) so call it directly
                    try {
                        countdownTimerManager.resetCountdown()
                    } catch (e: Exception) {
                        // Fallback: if something goes wrong, schedule a reset asynchronously
                        viewModelScope.launch { countdownTimerManager.resetCountdown() }
                    }
                }
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
        savedStateHandle.get<Int>("totalSessions")?.let { totalSessions ->
            if (totalSessions != -1) {
                viewModelScope.launch {
                    // Always update totals so UI and manager are in sync when entering the screen
                    countdownTimerManager._totalNoOfSessions.value = totalSessions
                    countdownTimerManager.totalFocusSetFlow.value = totalSessions
                    countdownTimerManager.totalBreakSetFlow.value = if (totalSessions > 1) totalSessions - 1 else 0
                    countdownTimerManager._totalNoOfBreaks.value = if (totalSessions > 1) totalSessions - 1 else 0
                }
            }
        }
        savedStateHandle.get<Int>("sessionDuration")?.let { durationVal ->
            if (durationVal != -1) {
                viewModelScope.launch {
                    // Always set session values; use 10000L for quick/dev runs (previous behaviour). Replace with durationVal * 60000 for production.
                    countdownTimerManager.currentTimeTargetInMillisFlow.value = 10000L
                    countdownTimerManager.timeLeftInMillisFlow.value = 10000L
                    countdownTimerManager._sessionTotalDurationMillis.value = 10000L
                    countdownTimerManager.sessionDuration.value = durationVal
                    Log.d("sessionInVMScope", "$durationVal")
                    Log.d("sessionDuration", "$durationVal")
                }
            }
        }
        savedStateHandle.get<Long>("progressDate")?.let { date ->
            if (date != 0L) {
                // Always set progress date so per-goal state is populated when entering the screen
                countdownTimerManager.progressDate.value = date
                countdownTimerManager.work = { it -> countdownTimerManager.onDayChallengeCompleted(it) }
            }
        }

    }

    fun startSession(openAndPopUp: (String, String) -> Unit) {

        viewModelScope.launch {
            // If we are resuming from a completed run, reset first to avoid immediate completed notification
            if (countdownTimerManager.timerState.value == TimerState.Initial || countdownTimerManager.isCompleted.value) {
                countdownTimerManager.resetCountdown()
                kotlinx.coroutines.delay(400)
            }
            countdownTimerManager.startSession(openAndPopUp)
        }
    }

    fun cancelCountdown() {

        viewModelScope.launch {
            countdownTimerManager.resetCountdown()
        }
    }

    fun pauseResumeCountdown(openAndPopUp: (String, String) -> Unit) {

        if (timerState.value==TimerState.Running) {

            viewModelScope.launch {
                countdownTimerManager.pauseCountdown()
            }
        }
        else {
            viewModelScope.launch {
                // If the timer is in Initial state or the manager previously marked isCompleted,
                // starting a fresh session is safer than trying to resume — avoids racing with completion handling.
                if (countdownTimerManager.timerState.value == TimerState.Initial || countdownTimerManager.isCompleted.value) {
                    // Reset any stale state first to prevent immediate completed notifications
                    countdownTimerManager.resetCountdown()
                    // Small delay to ensure reset has taken effect and service stopped
                    kotlinx.coroutines.delay(400)
                    // Start a new session (this clears isCompleted inside startSession if needed)
                    countdownTimerManager.startSession(openAndPopUp)
                } else {
                    countdownTimerManager.resumeCountdown(openAndPopUp)
                }
            }
        }
    }




}
