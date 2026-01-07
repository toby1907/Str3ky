package com.example.str3ky.core.notification

import com.example.str3ky.data.Achievement
import com.example.str3ky.data.CountdownTimerManager
import com.florianwalther.incentivetimer.core.notification.DefaultNotificationHelper
import javax.inject.Inject

class DefaultNotificationAdapter @Inject constructor(
    private val helper: DefaultNotificationHelper
) : NotificationAdapter {
    override fun removeTimerCompletedNotification() {
        helper.removeTimerCompletedNotification()
    }

    override fun removeTimerServiceNotification() {
        helper.removeTimerServiceNotification()
    }

    override fun removeResumeTimerNotification() {
        helper.removeResumeTimerNotification()
    }

    override fun showAchievementUnlockedNotification(achievement: Achievement) {
        helper.showAchievementUnlockedNotification(achievement)
    }

    override fun showTimerServiceNotification() {
        // The helper doesn't expose a no-arg showTimerServiceNotification; call update with defaults if needed
        // We'll keep a safe no-op to avoid surprising behavior
    }

    override fun showResumeTimerNotification(
        currentPhase: CountdownTimerManager.Phase,
        timeLeftInMillis: Long,
        focusCompleted: Int,
        breakCompleted: Int
    ) {
        helper.showResumeTimerNotification(currentPhase, timeLeftInMillis, focusCompleted, breakCompleted)
    }

    override fun showTimerCompletedNotification(finishedPhase: CountdownTimerManager.Phase, goalId: Int, progressDate: Long, sessionDuration: Long) {
        helper.showTimerCompletedNotification(finishedPhase, goalId, progressDate, sessionDuration)
    }

    override fun updateTimerServiceNotification(
        currentPhase: CountdownTimerManager.Phase,
        timeLeftInMillis: Long,
        timerRunning: Boolean,
        goalId: Int,
        totalSessions: Int,
        sessionDuration: Int,
        progressDate: Long,
        focusCompleted: Int,
        breakCompleted: Int
    ) {
        helper.updateTimerServiceNotification(
            currentPhase,
            timeLeftInMillis,
            timerRunning,
            goalId,
            totalSessions,
            sessionDuration,
            progressDate,
            focusCompleted,
            breakCompleted
        )
    }
}

















