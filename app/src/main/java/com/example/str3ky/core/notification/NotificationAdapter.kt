package com.example.str3ky.core.notification

import com.example.str3ky.data.Achievement
import com.example.str3ky.data.CountdownTimerManager

interface NotificationAdapter {
    fun removeTimerCompletedNotification()
    fun removeTimerServiceNotification()
    fun removeResumeTimerNotification()
    fun showAchievementUnlockedNotification(achievement: Achievement)
    fun showTimerServiceNotification()
    fun showResumeTimerNotification(
        currentPhase: CountdownTimerManager.Phase,
        timeLeftInMillis: Long,
        focusCompleted: Int,
        breakCompleted: Int,
    )
    fun showTimerCompletedNotification(finishedPhase: CountdownTimerManager.Phase, goalId: Int, progressDate: Long, sessionDuration: Long)
    fun updateTimerServiceNotification(
        currentPhase: CountdownTimerManager.Phase,
        timeLeftInMillis: Long,
        timerRunning: Boolean,
        goalId: Int,
        totalSessions: Int,
        sessionDuration: Int,
        progressDate: Long,
        focusCompleted: Int,
        breakCompleted: Int
    )
}

