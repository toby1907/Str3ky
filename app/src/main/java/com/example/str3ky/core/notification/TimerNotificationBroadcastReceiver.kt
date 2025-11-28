package com.example.str3ky.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.str3ky.data.CountdownTimerManager
import com.example.str3ky.data.TimerState
import com.florianwalther.incentivetimer.core.notification.DefaultNotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TimerNotificationBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var pomodoroTimerManager: CountdownTimerManager

    @Inject
    lateinit var notificationHelper: DefaultNotificationHelper

    override fun onReceive(context: Context?, intent: Intent?) {
        val timerRunning = intent?.getBooleanExtra(EXTRA_TIMER_RUNNING, false) ?: false
        val currentPhase = intent?.getSerializableExtra(EXTRA_POMODORO_PHASE) as? CountdownTimerManager.Phase
        val timeLeftInMillis = intent?.getLongExtra(EXTRA_TIME_LEFT_IN_MILLIS, -1) ?: -1L

        if (timerRunning) {
            // Timer is running, so the user wants to pause
            pomodoroTimerManager.pauseCountdown()
            if (currentPhase != null && timeLeftInMillis != -1L) {
                notificationHelper.showResumeTimerNotification(
                    currentPhase = currentPhase,
                    timeLeftInMillis = timeLeftInMillis
                )
            }
        } else {
            // Timer is paused, so the user wants to resume
            notificationHelper.removeResumeTimerNotification()
            pomodoroTimerManager.resumeCountdown()
        }
    }
}

const val EXTRA_TIMER_RUNNING = "EXTRA_TIMER_RUNNING"
const val EXTRA_POMODORO_PHASE = "EXTRA_POMODORO_PHASE"
const val EXTRA_TIME_LEFT_IN_MILLIS = "EXTRA_TIME_LEFT_IN_MILLIS"