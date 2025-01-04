package com.florianwalther.incentivetimer.core.notification

import androidx.core.app.NotificationCompat
import com.example.str3ky.data.CountdownTimerManager

interface NotificationHelper {
    fun getBaseTimerServiceNotification(): NotificationCompat.Builder
    fun updateTimerServiceNotification(
        currentPhase: CountdownTimerManager.Phase,
        timeLeftInMillis: Long,
        timerRunning: Boolean,
        goalId: Int,
        totalSessions: Int,
        sessionDuration:Int,
        progressDate: Long
    )

    fun showResumeTimerNotification(
        currentPhase: CountdownTimerManager.Phase,
        timeLeftInMillis: Long,

    )

    fun showTimerCompletedNotification(finishedPhase: CountdownTimerManager.Phase,goalId: Int, progressDate: Long,sessionDuration: Long)
  //  fun showRewardUnlockedNotification(reward: Reward)
    fun removeTimerServiceNotification()
    fun removeTimerCompletedNotification()
    fun removeResumeTimerNotification()
}