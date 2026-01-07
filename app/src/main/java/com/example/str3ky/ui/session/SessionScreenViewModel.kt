package com.example.str3ky.ui.session

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.str3ky.data.CountdownTimerManager
import com.example.str3ky.data.DayProgress
import com.example.str3ky.data.TimerState
import com.example.str3ky.repository.GoalRepositoryImpl
import com.example.str3ky.ui.add_challenge_screen.GoalState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.asStateFlow
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

    /* var dayHourSpent = mutableStateOf(0L)
       private set */

    // Read initial seeds from savedStateHandle (support Int or String) and fall back to manager defaults
    private val savedTotalSessions = (savedStateHandle.get<Int>("totalSessions") ?: savedStateHandle.get<String>("totalSessions")?.toIntOrNull())?.takeIf { it > 0 }
    val initialTotalSessions: Int = savedTotalSessions ?: countdownTimerManager._totalNoOfSessions.value
    private val savedSessionDuration = (savedStateHandle.get<Int>("sessionDuration") ?: savedStateHandle.get<String>("sessionDuration")?.toIntOrNull())?.takeIf { it > 0 }
    val initialSessionDuration: Int = savedSessionDuration ?: countdownTimerManager.sessionDuration.value
    val initialTotalBreaks: Int = if (initialTotalSessions > 1) initialTotalSessions - 1 else 0

    // When true, the UI should suppress immediate auto-navigation to DONE (used when arriving fresh from SessionSettings)
    private val _suppressAutoNavigate = MutableStateFlow(false)
    val suppressAutoNavigate = _suppressAutoNavigate.asStateFlow()

    // Clean init: set totals/session duration synchronously and keep async flows for goal loading and combinedFlow
    init {
        // Debug log initial seeds
        try {
            Log.d("SessionScreenVM", "init seeds: initialTotalSessions=$initialTotalSessions initialSessionDuration=$initialSessionDuration initialTotalBreaks=$initialTotalBreaks")
        } catch (e: Exception) {
            Log.d("SessionScreenVM", "failed logging init seeds: ${e.message}")
        }

        // Apply totals/session duration synchronously (seed manager state so UI reads consistent values immediately)
        if (initialTotalSessions > 0) {
            countdownTimerManager._totalNoOfSessions.value = initialTotalSessions
            countdownTimerManager.totalFocusSetFlow.value = initialTotalSessions
            val breaks = if (initialTotalSessions > 1) initialTotalSessions - 1 else 0
            countdownTimerManager.totalBreakSetFlow.value = breaks
            countdownTimerManager._totalNoOfBreaks.value = breaks
        }
        if (initialSessionDuration > 0) {
            // keep quick/dev duration (10s) as before for UI/dev; production mapping can convert minutes to millis
            countdownTimerManager.currentTimeTargetInMillisFlow.value = 10000L
            countdownTimerManager.timeLeftInMillisFlow.value = 10000L
            countdownTimerManager._sessionTotalDurationMillis.value = 10000L
            countdownTimerManager.sessionDuration.value = initialSessionDuration
        }

        // If this screen was opened with explicit navigation parameters (starting a fresh session),
        // ensure any previous 'completed' state is cleared synchronously so collectors won't see stale completed state.
        if (savedTotalSessions != null || savedSessionDuration != null) {
            try {
                // Suppress auto-navigation until the user explicitly starts the session
                _suppressAutoNavigate.value = true
                countdownTimerManager.resetCountdown()
            } catch (e: Throwable) {
                Log.w("SessionScreenVM", "Failed to resetCountdown on navigation seed: ${e.message}")
            }
        }

        // CombinedFlow collector for emitting sessionCompleted event
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

        // Load goal data asynchronously (flow) and set dayProgress when available
        savedStateHandle.get<Int>("goalId")?.let { goalId ->
            if (goalId != -1) {
                val previousGoal = countdownTimerManager.goalId.value
                if (previousGoal != goalId) {
                    try { countdownTimerManager.resetCountdown() } catch (e: Exception) { viewModelScope.launch { countdownTimerManager.resetCountdown() } }
                }
                currentGoalId = goalId
                countdownTimerManager.goalId.value = goalId
                viewModelScope.launch {
                    goalRepository.getGoal(goalId).collect { goal ->
                        // Update manager's goalState (use correct backing property name)
                        countdownTimerManager._goalState.value = countdownTimerManager._goalState.value.copy(goal = goal)
                        if (goal != null) countdownTimerManager.dayProgressFlow.value = goal.progress
                    }
                }
            }
        }

        // Progress date / work callback
        savedStateHandle.get<Long>("progressDate")?.let { date ->
            if (date != 0L) {
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
            // Clear suppression before starting a fresh session so completion is handled normally after this run
            _suppressAutoNavigate.value = false
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
