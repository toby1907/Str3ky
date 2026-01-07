package com.example.str3ky.data

import android.os.CountDownTimer
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.example.str3ky.core.notification.TimerServiceAdapter
import com.example.str3ky.millisecondsToMinutes
import com.example.str3ky.minutesToHours
import com.example.str3ky.repository.GoalRepository
import com.example.str3ky.repository.UserRepository
import com.example.str3ky.ui.achievements.getNewlyUnlockedAchievements
import com.example.str3ky.ui.add_challenge_screen.GoalState
import com.example.str3ky.ui.progress.toMinutes
import com.example.str3ky.ui.session.SessionScreenState
import com.example.str3ky.core.notification.NotificationAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import com.example.str3ky.di.ApplicationScope

@Singleton
class CountdownTimerManager @Inject constructor(
    private val timerServiceAdapter: TimerServiceAdapter,
    val goalRepository: GoalRepository,
    private val notificationAdapter: NotificationAdapter,
    private val userRepository: UserRepository,
    @ApplicationScope private val externalScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) {
    private val scope: CoroutineScope = externalScope

    // New: emit newly unlocked achievements for UI display
    private val _unlockedAchievementsEvent = MutableSharedFlow<List<Achievement>>(replay = 0)
    val unlockedAchievementsEvent = _unlockedAchievementsEvent.asSharedFlow()

    enum class Phase {
        FOCUS_SESSION, BREAK, COMPLETED
    }

    private val _timerState = MutableStateFlow<TimerState>(TimerState.Initial)
    val timerState: StateFlow<TimerState> = _timerState


    // Event to signal timer completion
    private val _timerFinishedEvent = MutableSharedFlow<Long>()
    val timerFinishedEvent: SharedFlow<Long> = _timerFinishedEvent.asSharedFlow()

    var popUpLambda: ((String, String) -> Unit)? = null
    var work: ((Boolean) -> Unit)? = null
    val goalId = MutableStateFlow(-1)

  /*  private val _progressDate = mutableStateOf(0L)
    val progressDate: State<Long> = _progressDate

    fun setProgressDate(date: Long) {
        _progressDate.value = date
    }*/



    private var countDownTimer: CountDownTimer? = null

    // Properties
    val _sessionTotalDurationMillis = MutableStateFlow(0L)
    val timeLeftInMillisFlow = MutableStateFlow(_sessionTotalDurationMillis.value)
    private val timeLeftInMillisForCombine: Flow<Long> = timeLeftInMillisFlow.asStateFlow()
    val timeLeftInMillis: Flow<Long> = timeLeftInMillisFlow
    // properties for completion function
    var sessionDuration = MutableStateFlow(-1)
    var progressDate =  MutableStateFlow(0L)
    var progressFlowCombine: Flow<Long> = progressDate.asStateFlow()
    val dayProgressFlow = MutableStateFlow(emptyList<DayProgress>())
    // Public alias removed; use dayProgressFlow directly from other classes (safer and less noisy)
    val _goalState = MutableStateFlow(
        GoalState()
    )

    private val
            currentphase = MutableStateFlow(
        Phase.FOCUS_SESSION
    )
    val currentPhase: StateFlow<Phase> = currentphase
    val currentPhaseFlow: Flow<Phase> = currentPhase


    val _countdownTimeMillis = mutableStateOf(SessionScreenState())
    val _breakDurationMillis = mutableStateOf(SessionScreenState())


    // total no of pomo
    val _totalNoOfSessions = MutableStateFlow(0)
    val _totalNoOfBreaks = MutableStateFlow(0)

    //no of pomocompleted
    private val focusSetFlow = MutableStateFlow(0)
    val focusSet: Flow<Int> = focusSetFlow

    val totalFocusSetFlow = MutableStateFlow(_totalNoOfSessions.value)
    val totalFocusSet: Flow<Int> = totalFocusSetFlow

    private val breakSetFlow = MutableStateFlow(0)
    val breakSet: Flow<Int> = breakSetFlow

    val totalBreakSetFlow = MutableStateFlow(_totalNoOfBreaks.value)
    val totalBreakSet: Flow<Int> = totalBreakSetFlow

    val combinedFlow: Flow<CombinedData> = combine(
        // focus/break totals and ids
        totalFocusSetFlow,
        goalId,
        currentPhaseFlow,
        timeLeftInMillisForCombine,
        progressFlowCombine,
        focusSetFlow,
        breakSetFlow

        // Include currentphase as a flow
    ) { values ->
        // values is an Array of Any? corresponding to the flows in order
        val totalFocusSet = values[0] as Int
        val goalIdFlow = values[1] as Int
        val currentPhaseFlow = values[2] as Phase
        val timeLeftInMillis = values[3] as Long
        val progressDateFlow = values[4] as Long
        val focusCompleted = values[5] as Int
        val breakCompleted = values[6] as Int

        CombinedData(
            totalFocusSet = totalFocusSet,
            currentPhase = currentPhaseFlow,
            timeLeftInMillis = timeLeftInMillis,
            goalId = goalIdFlow,
            progressDate = progressDateFlow,
            focusCompleted = focusCompleted,
            breakCompleted = breakCompleted
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(300),
        initialValue = CombinedData.EMPTY
    )
    private val isSessionInProgressFlow = MutableStateFlow(false)
    // Public read-only view for other components (e.g., TimerService) to observe whether a session is active
    val isSessionInProgress: StateFlow<Boolean> = isSessionInProgressFlow.asStateFlow()

    // Backwards-compatible simple boolean accessor used by some consumers (TimerService) to avoid
    // needing to import or observe the StateFlow directly.
    fun sessionInProgress(): Boolean = isSessionInProgressFlow.value

    private var _isBreakInProgress = mutableStateOf(
        false
    )
    var isCompleted = mutableStateOf(
        false
    )
    // timestamp of the last finished event emitted (ms since epoch). Used to verify freshness.
    val lastFinishedAt = MutableStateFlow(0L)

    // The phase that just finished (FOCUS_SESSION or BREAK) — set before transitioning to COMPLETED
    val lastFinishedPhase = MutableStateFlow<Phase?>(null)

    val currentTimeTargetInMillisFlow = MutableStateFlow(_sessionTotalDurationMillis.value)
    val currentTimeTargetInMillis: Flow<Long> = currentTimeTargetInMillisFlow


    // Removed unused public aliases

    val onSkipClickedFlow = MutableStateFlow(false)
    val onSkipClicked: StateFlow<Boolean> = onSkipClickedFlow

    init {

        _breakDurationMillis.value = _breakDurationMillis.value.copy(
            breakDurationMillis = 5000L
        )

        // Reset per-goal state when goalId changes so switching goals doesn't inherit previous run state
        scope.launch {
            var lastObservedGoalId = goalId.value
            goalId.collect { newGoalId ->
                if (newGoalId != lastObservedGoalId) {
                    // Clear session-specific counters and flags for the new goal
                    isCompleted.value = false
                    currentphase.value = Phase.FOCUS_SESSION
                    focusSetFlow.value = 0
                    breakSetFlow.value = 0
                    timeLeftInMillisFlow.value = _sessionTotalDurationMillis.value
                    currentTimeTargetInMillisFlow.value = _sessionTotalDurationMillis.value
                    _timerState.value = TimerState.Initial
                    lastFinishedAt.value = 0L
                }
                lastObservedGoalId = newGoalId
            }
        }
    }

    private fun startCountDown(countDownTimeMillis: Long) {
        // Guard: don't start timers with non-positive duration (prevents immediate onFinish)
        if (countDownTimeMillis <= 0L) {
            return
        }

        // Cancel any existing timer to avoid overlapping timers
        countDownTimer?.cancel()
        countDownTimer = null

        // Ensure remainingTimeMillis is initialized
        remainingTimeMillis = countDownTimeMillis

        countDownTimer = object : CountDownTimer(countDownTimeMillis, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillisFlow.value = millisUntilFinished
                // keep the remaining time updated so resume uses a fresh value
                remainingTimeMillis = millisUntilFinished

            }

            override fun onFinish() {
                // DEBUG: log finish and current phase
                Log.d("CountdownTimerManager", "onFinish - currentPhase=${currentphase.value} timeTarget=${currentTimeTargetInMillisFlow.value}")

                // Increment the appropriate completed counter based on which phase finished
                when (currentphase.value) {
                    Phase.FOCUS_SESSION -> {
                        focusSetFlow.value = focusSetFlow.value + 1
                        Log.d("CountdownTimerManager", "Focus session completed. focusSet=${focusSetFlow.value}")

                        // Per-session hour accumulation delegated to helper (testable)
                        try {
                            val sessionHours = minutesToHours(sessionDuration.value.toLong())
                            accumulateSessionHours(sessionHours)
                        } catch (e: Exception) {
                            Log.w("CountdownTimerManager", "Failed to accumulate session hours: ${e.message}")
                        }
                    }

                    Phase.BREAK -> {
                        breakSetFlow.value = breakSetFlow.value + 1
                        Log.d("CountdownTimerManager", "Break completed. breakSet=${breakSetFlow.value}")
                    }

                    else -> {
                        // no-op for COMPLETED
                        Log.d("CountdownTimerManager", "onFinish - phase was COMPLETED (no increment)")
                    }
                }

                // Cancel countdown and mark not in progress BEFORE invoking next phase to avoid races
                cancelCountdown()

                // Only attempt to start the next phase if we are not already in COMPLETED
                if (currentphase.value != Phase.COMPLETED) {
                    popUpLambda?.let { startNextPhase(it) }
                } else {
                    Log.d("CountdownTimerManager", "onFinish - current phase is COMPLETED, skipping startNextPhase")
                }


                // Timer finished (e.g., perform some action)
                // You can emit this event using LiveData if needed
            }
        }.start()
        isSessionInProgressFlow.value = true

    }

    fun startSession(openAndPopUp: (String, String) -> Unit) {
        val durationToStart = currentTimeTargetInMillisFlow.value.takeIf { it > 0L } ?: _sessionTotalDurationMillis.value
        if (durationToStart <= 0L) return

        Log.d("CountdownTimerManager", "startSession - duration=$durationToStart totalSessions=${totalFocusSetFlow.value} goalId=${goalId.value}")

        // If previous run reached COMPLETED, clear that state so a fresh session can start cleanly
        val wasCompleted = isCompleted.value
        if (wasCompleted) {
            // Clear completed state to start a fresh session
            isCompleted.value = false
            currentphase.value = Phase.FOCUS_SESSION
            focusSetFlow.value = 0
            breakSetFlow.value = 0
            timeLeftInMillisFlow.value = _sessionTotalDurationMillis.value
            currentTimeTargetInMillisFlow.value = _sessionTotalDurationMillis.value
            // Also clear the last finished timestamp so TimerService won't treat an old finished token as current
            lastFinishedAt.value = 0L
        }

        _timerState.value = TimerState.Running
        notificationAdapter.removeTimerCompletedNotification()
        // ensure state updated before starting the service
        popUpLambda = openAndPopUp
        // ensure remainingTimeMillis reflects the active countdown
        remainingTimeMillis = durationToStart
        // start the service after internal state is ready
        // If we just cleared a completed run, ask the service to suppress any next finished event to avoid race
        timerServiceAdapter.startTimerService(suppressNextFinished = wasCompleted)
        startCountDown(durationToStart)
    }

    fun startNextPhase(openAndPopUp: (String, String) -> Unit) {
        val currentPhase = currentPhase.value
        val sessionsCompleted = focusSetFlow.value
        val totalNoOfSessions = totalFocusSetFlow.value

        Log.d("CountdownTimerManager", "startNextPhase - currentPhase=$currentPhase sessionsCompleted=$sessionsCompleted totalNoOfSessions=$totalNoOfSessions")

        val nextPhase: Phase = when (currentPhase) {
            Phase.FOCUS_SESSION -> {
                if (totalNoOfSessions > 0 && sessionsCompleted >= totalNoOfSessions) {
                    Phase.COMPLETED
                } else if (onSkipClicked.value) {
                    Phase.FOCUS_SESSION
                } else {
                    Phase.BREAK
                }
            }

            Phase.BREAK -> {
                Phase.FOCUS_SESSION

            }

            Phase.COMPLETED -> {
                Phase.COMPLETED
            }
        }
        currentphase.value = nextPhase
        Log.d("CountdownTimerManager", "startNextPhase - decided nextPhase=$nextPhase")
        val nextTimeTarget = when (nextPhase) {
            Phase.FOCUS_SESSION -> {

                _sessionTotalDurationMillis.value
            }

            Phase.BREAK -> {

                _breakDurationMillis.value.breakDurationMillis
            }

            Phase.COMPLETED -> {
                _sessionTotalDurationMillis.value

            }
        }

        // If we determined the next phase is COMPLETED, handle completion now and do not start a new timer
        if (nextPhase == Phase.COMPLETED) {
            Log.d("CountdownTimerManager", "startNextPhase - handling COMPLETED state")
            timeLeftInMillisFlow.value = _sessionTotalDurationMillis.value
            focusSetFlow.value = 0
            breakSetFlow.value = 0
            // Set to COMPLETED and keep it so observers can react
            // Record which phase just finished so notifications can reflect it correctly
            lastFinishedPhase.value = currentPhase
            currentphase.value = Phase.COMPLETED
            isCompleted.value = true
            _timerState.value = TimerState.Finished
            notificationAdapter.removeTimerServiceNotification()

            work?.invoke(true)

            scope.launch {
                val ts = System.currentTimeMillis()
                Log.d("CountdownTimerManager", "EMIT_TIMER_FINISHED at $ts")
                lastFinishedAt.value = ts
                _timerFinishedEvent.emit(ts)
            }

            return
        }

        currentTimeTargetInMillisFlow.value = nextTimeTarget
        timeLeftInMillisFlow.value = nextTimeTarget
        // Additional completion guard: if session counts reached, treat as completion
        if (totalNoOfSessions > 0 && sessionsCompleted >= totalNoOfSessions) {
            Log.d("CountdownTimerManager", "startNextPhase - session count reached, handling COMPLETED state")
            timeLeftInMillisFlow.value = _sessionTotalDurationMillis.value
            focusSetFlow.value = 0
            breakSetFlow.value = 0
            currentphase.value = Phase.COMPLETED
            isCompleted.value = true
            _timerState.value = TimerState.Finished
            notificationAdapter.removeTimerServiceNotification()
            // Record which phase just finished so notifications can reflect it correctly
            lastFinishedPhase.value = currentPhase
            work?.invoke(true)
            scope.launch {
                val ts = System.currentTimeMillis()
                Log.d("CountdownTimerManager", "EMIT_TIMER_FINISHED at $ts")
                lastFinishedAt.value = ts
                _timerFinishedEvent.emit(ts)
            }
            return
        }
        startSession(openAndPopUp)
    }

    fun pauseCountdown() {
        _timerState.value = TimerState.Paused
        remainingTimeMillis = timeLeftInMillisFlow.value
        countDownTimer?.cancel()
        isSessionInProgressFlow.value = false
    }

    fun resumeCountdown(openAndPopUp: (String, String) -> Unit) {
        // Use the freshest available remaining time: prefer remainingTimeMillis (set on pause / ticks),
        // otherwise fallback to flow's current value
        val toStart = if (remainingTimeMillis > 0L) remainingTimeMillis else timeLeftInMillisFlow.value
        if (toStart <= 0L) return
        _timerState.value = TimerState.Running
        popUpLambda = openAndPopUp
        // ensure service is running when resuming
        timerServiceAdapter.startTimerService()
        remainingTimeMillis = toStart
        startCountDown(toStart)
    }

    // Removed duplicate parameterless resumeCountdown() overload to avoid ambiguity/conflicts

    fun cancelCountdown() {
        timerServiceAdapter.stopTimerService()
        countDownTimer?.cancel()
        countDownTimer = null
        isSessionInProgressFlow.value = false

    }

    fun resetCountdown() {
        timerServiceAdapter.stopTimerService()
        countDownTimer?.cancel()
        timeLeftInMillisFlow.value = _sessionTotalDurationMillis.value
        focusSetFlow.value = 0
        breakSetFlow.value = 0
        // Reset state to initial so UI and resume logic behave correctly
        isCompleted.value = false
        currentphase.value = Phase.FOCUS_SESSION
        remainingTimeMillis = _sessionTotalDurationMillis.value
        _timerState.value = TimerState.Initial
        // Ensure session-in-progress flag is cleared so UI & TimerService don't show running/paused notifications
        isSessionInProgressFlow.value = false
        // Clear any previously emitted finished timestamp to avoid stale completed notifications
        lastFinishedAt.value = 0L
    }

    // Add a property to save the remaining time when paused
    private var remainingTimeMillis = 0L

    // Public helper to accumulate hours for a finished focus session.
    // Updates in-memory dayProgressFlow immediately and persists totalHoursSpent asynchronously.
    fun accumulateSessionHours(sessionHours: Double) {
        try {
            val todayKey = startOfDayMillis(progressDate.value)
            val updatedProgress = dayProgressFlow.value.toMutableList()
            var found = false
            for (i in updatedProgress.indices) {
                if (startOfDayMillis(updatedProgress[i].date) == todayKey) {
                    updatedProgress[i] = updatedProgress[i].copy(hoursSpent = updatedProgress[i].hoursSpent + sessionHours)
                    found = true
                    break
                }
            }
            if (!found) {
                updatedProgress.add(DayProgress(date = progressDate.value, completed = false, hoursSpent = sessionHours))
            }
            dayProgressFlow.value = updatedProgress

            // Persist user total hours incrementally inside coroutine
            scope.launch {
                try {
                    val users = userRepository.getUser().first()
                    if (users.isNotEmpty()) {
                        val user = users[0]
                        val newTotal = user.totalHoursSpent + sessionHours
                        val merged = user.copy(totalHoursSpent = newTotal)
                        userRepository.update(merged)

                        // Immediately check for any achievements unlocked by the increased hours
                        try {
                            checkAndUnlockAchievements(merged)
                        } catch (e: Exception) {
                            Log.w("CountdownTimerManager", "Failed to check/unlock achievements after hours update: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    Log.w("CountdownTimerManager", "Failed to persist user hours in coroutine: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.w("CountdownTimerManager", "accumulateSessionHours failed: ${e.message}")
        }
    }


    fun onDayChallengeCompleted(change: Boolean) {
        // make this idempotent: only add hours and update streaks when today's DayProgress transitions to completed
        _timerState.value = TimerState.Initial
        val sessionDurationMinutes = sessionDuration.value * _totalNoOfSessions.value.toLong()
        Log.d("sessionDurationMinutes", "${minutesToHours(sessionDuration.value.toLong())}")

        scope.launch {
            val totalBreakMins = if (_totalNoOfBreaks.value != 0) _totalNoOfBreaks.value * 5 else 0

            // Compute new progress list by marking today's entry completed based on existing hours
            val oldProgress = dayProgressFlow.value
            val todayKey = startOfDayMillis(progressDate.value)

            var wasCompletedBefore = false
            val newProgress = oldProgress.map { dayProgress ->
                if (startOfDayMillis(dayProgress.date) == todayKey) {
                    wasCompletedBefore = dayProgress.completed
                    // Do NOT add hours here; per-session accumulation already updated hoursSpent.
                    val isNowCompleted = dayProgress.hoursSpent >= minutesToHours(_goalState.value.goal?.focusSet?.toMinutes() ?: 0)
                    dayProgress.copy(
                        date = dayProgress.date,
                        completed = if (isNowCompleted) change else false,
                        hoursSpent = dayProgress.hoursSpent
                    )
                } else dayProgress
            }

            // If today's entry did not exist in the list, append it (no hours added)
            val containsToday = newProgress.any { startOfDayMillis(it.date) == todayKey }
            val finalProgress = if (!containsToday) {
                val hours = 0.0
                newProgress + DayProgress(date = progressDate.value, completed = false, hoursSpent = hours)
            } else newProgress

            // Update in-memory flow immediately
            dayProgressFlow.value = finalProgress

            // Persist goal progress (save will run on background executor in repository)
            _goalState.value.goal?.let { goal ->
                try {
                    goalRepository.save(
                        goal.copy(
                            progress = finalProgress,
                            durationInfo = Duration(
                                countdownTime = minutesToHours(sessionDurationMinutes + totalBreakMins),
                                isCompleted = sessionDurationMinutes * _totalNoOfSessions.value == millisecondsToMinutes(goal.focusSet).toLong()
                            )
                        )
                    ) { _ -> /* no-op callback */ }
                } catch (e: Exception) {
                    Log.w("CountdownTimerManager", "Failed to persist goal progress: ${e.message}")
                }
            }

            // If today's entry newly became completed (was not completed before, now is), recalc streaks and persist user
            val todaysProgress = finalProgress.find { startOfDayMillis(it.date) == todayKey }
            val isCompletedNow = todaysProgress?.completed == true

            if (isCompletedNow && !wasCompletedBefore) {
                try {
                    val users = userRepository.getUser().first()
                    if (users.isNotEmpty()) {
                        val user = users[0]

                        // Recalculate streaks from finalProgress
                        val calculatedLongest = calculateLongestStreak(finalProgress)
                        val calculatedCurrent = calculateCurrentStreak(finalProgress, todayKey)

                        val mergedUser = user.copy(
                            // totalHoursSpent already updated per-session
                            longestStreak = maxOf(user.longestStreak, calculatedLongest),
                            currentStreak = calculatedCurrent,
                            lastCompletedDate = todayKey
                        )

                        // Persist updated user and check achievements
                        userRepository.update(mergedUser)
                        checkAndUnlockAchievements(mergedUser)
                    }
                } catch (e: Exception) {
                    Log.w("CountdownTimerManager", "Failed to update user hours/streaks: ${e.message}")
                }
            }

            // Cancel any scheduled reminders for completed days
            val lastDayProgress = finalProgress.lastOrNull()
            if (lastDayProgress?.completed == true) {
                _goalState.value.goal?.let { goal ->
                    goalRepository.cancelReminderForDayProgress(goal, lastDayProgress)
                }
            }

            // Stop service (we consider day-completed flows to finish timer-related work)
            timerServiceAdapter.stopTimerService()
            Log.d("CountdownTimerManager", "onDayChallengeCompleted finished for date=$todayKey")
        }
    }

    // Helper: normalize millis to day-start (local timezone)
    private fun startOfDayMillis(millis: Long): Long {
        // Use UTC day-start to make day keys timezone-agnostic for storage and comparisons
        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
         cal.timeInMillis = millis
         cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
         cal.set(java.util.Calendar.MINUTE, 0)
         cal.set(java.util.Calendar.SECOND, 0)
         cal.set(java.util.Calendar.MILLISECOND, 0)
         return cal.timeInMillis
     }

    // Helper: compute the longest consecutive completed streak in the list
    private fun calculateLongestStreak(progressList: List<DayProgress>): Int {
        val completedDays = progressList.filter { it.completed }.map { startOfDayMillis(it.date) }.distinct().sorted()
        if (completedDays.isEmpty()) return 0
        var longest = 0
        var current = 1
        for (i in 1 until completedDays.size) {
            if (completedDays[i] - completedDays[i - 1] == 24L * 60L * 60L * 1000L) {
                current++
            } else {
                if (current > longest) longest = current
                current = 1
            }
        }
        if (current > longest) longest = current
        return longest
    }

    // Helper: compute the current streak based on today's date and the progress list
    private fun calculateCurrentStreak(progressList: List<DayProgress>, todayMillis: Long): Int {
        val todayStart = startOfDayMillis(todayMillis)
        val completedToday = progressList.any { startOfDayMillis(it.date) == todayStart && it.completed }
        return if (completedToday) {
            // Count consecutive completed days including today
            var count = 1
            var currentDay = todayStart - 24L * 60L * 60L * 1000L // go back one day
            while (progressList.any { startOfDayMillis(it.date) == currentDay && it.completed }) {
                count++
                currentDay -= 24L * 60L * 60L * 1000L
            }
            count
        } else {
            0
        }
    }

    fun checkAndUnlockAchievements(user: User) {

        // Use explicit newly-unlocked diff function to keep intent clear and idempotent
        val newlyUnlocked = getNewlyUnlockedAchievements(user)

        if (newlyUnlocked.isEmpty()) {
            Log.d("Achievements", "checkAndUnlockAchievements - no newly unlocked achievements for user=${user.id}")
            return
        }

        scope.launch {
            try {
                // Persist only truly new achievements; the repository method dedupes atomically
                val actualAdded = try {
                    userRepository.addAchievementsAtomically(newlyUnlocked)
                } catch (e: Exception) {
                    Log.w("Achievements", "addAchievementsAtomically failed: ${e.message}")
                    emptyList<Achievement>()
                }

                if (actualAdded.isEmpty()) {
                    Log.d("Achievements", "No actual achievements added (likely already present). Skipping notifications/emits.")
                    return@launch
                }

                Log.d("Achievements", "New achievements unlocked: $actualAdded")

                // Show a notification for each newly unlocked achievement
                actualAdded.forEach { achievement ->
                    try {
                        notificationAdapter.showAchievementUnlockedNotification(achievement)
                    } catch (e: Exception) {
                        Log.w("Achievements", "Failed to show achievement notification for ${achievement.name}: ${e.message}")
                    }
                }

                // Emit event for UI to display an in-app banner
                _unlockedAchievementsEvent.emit(actualAdded)
            } catch (e: Exception) {
                Log.w("Achievements", "Failed to persist/emit new achievements: ${e.message}")
            }
        }
    }

}

sealed class TimerState {
    object Initial : TimerState()
    object Running: TimerState()
    object Paused : TimerState()
    object Finished : TimerState()
}















