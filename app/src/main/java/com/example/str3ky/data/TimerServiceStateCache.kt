package com.example.str3ky.data

import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject
import javax.inject.Singleton
import com.example.str3ky.data.CountdownTimerManager

@Singleton
class TimerServiceStateCache @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    companion object {
        private const val KEY_GOAL_ID = "timer_goal_id"
        private const val KEY_TOTAL_SESSIONS = "timer_total_sessions"
        private const val KEY_SESSION_DURATION = "timer_session_duration"
        private const val KEY_PROGRESS_DATE = "timer_progress_date"
        private const val KEY_PHASE = "timer_phase"
        private const val KEY_TIME_LEFT = "timer_time_left"
    }

    fun saveState(goalId: Int, totalSessions: Int, sessionDuration: Int, progressDate: Long) {
        sharedPreferences.edit {
            putInt(KEY_GOAL_ID, goalId)
            putInt(KEY_TOTAL_SESSIONS, totalSessions)
            putInt(KEY_SESSION_DURATION, sessionDuration)
            putLong(KEY_PROGRESS_DATE, progressDate)
        }
    }

    fun updateTimerState(timerState: CombinedData) {
        sharedPreferences.edit {
            putString(KEY_PHASE, timerState.currentPhase.name)
            putLong(KEY_TIME_LEFT, timerState.timeLeftInMillis)
            putInt(KEY_GOAL_ID, timerState.goalId)
            putLong(KEY_PROGRESS_DATE, timerState.progressDate)
        }
    }

    fun restoreState(): RestoredTimerState? {
        val goalId = sharedPreferences.getInt(KEY_GOAL_ID, -1)
        if (goalId == -1) return null
        val totalSessions = sharedPreferences.getInt(KEY_TOTAL_SESSIONS, 0)
        val sessionDuration = sharedPreferences.getInt(KEY_SESSION_DURATION, 0)
        val progressDate = sharedPreferences.getLong(KEY_PROGRESS_DATE, 0L)
        val phaseName = sharedPreferences.getString(KEY_PHASE, CombinedData.EMPTY.currentPhase.name)
        val phase = CountdownTimerManager.Phase.valueOf(phaseName ?: CombinedData.EMPTY.currentPhase.name)
        val timeLeft = sharedPreferences.getLong(KEY_TIME_LEFT, 0L)
        return RestoredTimerState(goalId, totalSessions, sessionDuration, progressDate, phase, timeLeft)
    }

    fun clear() {
        sharedPreferences.edit { clear() }
    }
}

data class RestoredTimerState(
    val goalId: Int,
    val totalSessions: Int,
    val sessionDurationMinutes: Int,
    val progressDate: Long,
    val phase: CountdownTimerManager.Phase,
    val timeLeftInMillis: Long
)
















