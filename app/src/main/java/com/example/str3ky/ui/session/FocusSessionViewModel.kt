package com.example.str3ky.ui.session

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.str3ky.data.CountdownTimerManager
import com.example.str3ky.millisecondsToMinutes
import com.example.str3ky.repository.GoalRepositoryImpl
import com.example.str3ky.computeSessionsAndBreaksForTotalMinutes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class FocusSessionViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val goalRepository: GoalRepositoryImpl,
    val countdownTimerManager: CountdownTimerManager
) : ViewModel() {

    private var currentGoalId: Int? = null

    private val _goalId = mutableIntStateOf(-1)
    val goalId: State<Int> = _goalId
    var timerValue = mutableStateOf(30)
        private set

    var skipBreak = mutableStateOf(false)
        private set
    var numSessions = mutableIntStateOf(2)
        private set
    var numBreaks = mutableIntStateOf(1)
        private set
    var sessionDuration = mutableStateOf(30)
        private set
    var progressDate = mutableStateOf(0L)
        private set


    init {
        savedStateHandle.get<Int>("goalId")?.let { goalId ->
            if (goalId != -1) {
                currentGoalId = goalId
                _goalId.intValue = goalId
            }
        }
        savedStateHandle.get<Long>("focusTime")?.let { focusTime ->
            if (focusTime != 0L) {
                timerValue.value = millisecondsToMinutes(focusTime)
            }
        }
        savedStateHandle.get<Long>("progressDate")?.let { date ->
            if (date != 0L) {
                progressDate.value = date
            }
        }

        // Ensure the derived values (numSessions, numBreaks, sessionDuration)
        // are calculated based on the current timerValue at creation time.
        updateSessionsAndBreaks()
        sessionDurationCalculation()

        // Log initial computed values to help debug mapping issues
        Log.d("FocusSessionVM", "init: timerValue=${timerValue.value}, sessions=${numSessions.intValue}, breaks=${numBreaks.intValue}, sessionDuration=${sessionDuration.value}")
    }

    // Function to increase timer
    fun increaseTimer() {
        val currentValue = timerValue.value
        val incrementedValue = when {
            currentValue in 10..50 -> currentValue + 5
            currentValue in 50..240 -> currentValue + 15
            else -> currentValue // No change outside the specified range
        }
        timerValue.value = incrementedValue.coerceIn(10, 240) // Ensure it stays within bounds
        updateSessionsAndBreaks()
        sessionDurationCalculation()
    }

    // Function to decrease timer
    fun decreaseTimer() {
        val currentValue = timerValue.value
        val decrementedValue = when {
            currentValue in 240 downTo 50 -> currentValue - 15
            currentValue in 50 downTo 10 -> currentValue - 5
            else -> currentValue // No change outside the specified range
        }
        timerValue.value = decrementedValue.coerceIn(10, 240) // Ensure it stays within bounds
        updateSessionsAndBreaks()
        sessionDurationCalculation()
    }

    // Function to toggle the skip break option
    fun toggleSkipBreak() {
        skipBreak.value = !skipBreak.value
        if (skipBreak.value) {
            // when skipping break, only one session and no breaks
            numSessions.intValue = 1
            numBreaks.intValue = 0
        } else {
            updateSessionsAndBreaks()
        }
        // recalc session duration after change
        sessionDurationCalculation()
    }

    // Public function to set number of sessions and breaks based on total timer value (clamped)
    fun updateSessionsAndBreaks() {
        val minutes = timerValue.value.coerceIn(10, 240)
        val (sessions, breaks) = computeSessionsAndBreaksForTotalMinutes(minutes)
        numSessions.intValue = sessions
        numBreaks.intValue = breaks
        Log.d("FocusSessionVM", "updateSessionsAndBreaks: minutes=$minutes -> sessions=${numSessions.intValue}, breaks=${numBreaks.intValue}")
    }

    fun sessionDurationCalculation() {
        // Use clamped minutes for computation
        val minutes = timerValue.value.coerceIn(10, 240)

        // Calculate total break time (in minutes)
        val totalBreakTime = numBreaks.intValue * 5

        // Adjusted timer value after removing break time
        val adjustedTimerValue = (minutes - totalBreakTime).coerceAtLeast(0)

        // Protect against division by zero
        val sessions = numSessions.intValue.coerceAtLeast(1)

        // Calculate session duration (including breaks)
        sessionDuration.value = adjustedTimerValue / sessions
    }



}















