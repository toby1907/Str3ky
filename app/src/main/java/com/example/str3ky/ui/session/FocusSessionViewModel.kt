package com.example.str3ky.ui.session

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.str3ky.repository.GoalRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class FocusSessionViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val goalRepository: GoalRepositoryImpl,
) : ViewModel() {

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

    // Function to increase timer
    fun increaseTimer() {
        val currentValue = timerValue.value
        val incrementedValue = when {
            currentValue in 10..50 -> currentValue + 5
            currentValue in 50..240 -> currentValue + 15
            else -> currentValue // No change outside the specified range
        }
        timerValue.value = incrementedValue.coerceIn(10, 240) // Ensure it stays within bounds
        numOfSessionsAndBreaks()
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
        numOfSessionsAndBreaks()
    }

    // Function to toggle the skip break option
    fun toggleSkipBreak() {
        skipBreak.value = !skipBreak.value
       if(skipBreak.value) {
            numSessions.intValue = 1
            numBreaks.intValue = 0
        }
      else  { numOfSessionsAndBreaks() }
    }

    // Function to start the focus session
   private fun numOfSessionsAndBreaks() {
        when (timerValue.value) {
            in 10..25 -> {
                numSessions.intValue = 1
                numBreaks.intValue = 0
            }
            in 30..65 -> {
                numSessions.intValue = 2
                numBreaks.intValue = 1
            }
            in 80..95 -> {
                numSessions.intValue = 3
                numBreaks.intValue = 2
            }
            in 110..140 -> {
                numSessions.intValue = 4
                numBreaks.intValue = 3
            }
            in 155..170 -> {
                numSessions.intValue = 6
                numBreaks.intValue = 5
            }
            in 185..215 -> {
                numSessions.intValue = 7
                numBreaks.intValue = 6
            }
            in 200..240 -> {
                numSessions.intValue = 8
                numBreaks.intValue = 7
            }

        }

      //  sessionDurationCalculation()

    }

    fun sessionDurationCalculation() {
        // Calculate total break time (in minutes)
        val totalBreakTime = numBreaks.intValue * 5

        // Adjusted timer value after removing break time
        val adjustedTimerValue = timerValue.value - totalBreakTime

        // Calculate session duration (including breaks)
         sessionDuration.value = adjustedTimerValue / numSessions.intValue
    }


}