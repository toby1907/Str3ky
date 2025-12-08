package com.florianwalther.incentivetimer.core.notification

import androidx.core.app.NotificationCompat
import com.example.str3ky.data.CountdownTimerManager
import com.example.str3ky.data.Achievement

interface NotificationHelper {
    fun getBaseTimerServiceNotification(): NotificationCompat.Builder
    fun updateTimerServiceNotification(
        currentPhase: CountdownTimerManager.Phase,
        timeLeftInMillis: Long,
        timerRunning: Boolean,
        goalId: Int,
        totalSessions: Int,
        sessionDuration:Int,
        progressDate: Long,
        focusCompleted: Int = 0,
        breakCompleted: Int = 0
    )

    fun showResumeTimerNotification(
        currentPhase: CountdownTimerManager.Phase,
        timeLeftInMillis: Long,
        focusCompleted: Int = 0,
        breakCompleted: Int = 0,
    )

    fun showTimerCompletedNotification(finishedPhase: CountdownTimerManager.Phase,goalId: Int, progressDate: Long,sessionDuration: Long)

    // New: show a notification when an achievement is unlocked
    fun showAchievementUnlockedNotification(achievement: Achievement)

  //  fun showRewardUnlockedNotification(reward: Reward)
    fun removeTimerServiceNotification()
    fun removeTimerCompletedNotification()
    fun removeResumeTimerNotification()
}