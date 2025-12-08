package com.example.str3ky.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.str3ky.data.CountdownTimerManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TimerNotificationBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var pomodoroTimerManager: CountdownTimerManager

    override fun onReceive(p0: Context?, intent: Intent?) {
        val timerRunning = intent?.getBooleanExtra(EXTRA_TIMER_RUNNING, false) ?: false
        val focusCompleted = intent?.getIntExtra(EXTRA_FOCUS_COMPLETED, 0) ?: 0
        val breakCompleted = intent?.getIntExtra(EXTRA_BREAK_COMPLETED, 0) ?: 0

        android.util.Log.d("TimerNotifBR", "onReceive: timerRunning=$timerRunning focusCompleted=$focusCompleted breakCompleted=$breakCompleted")

        if (timerRunning) {
            // Pause action pressed: pause the timer. TimerService will update/remove notifications accordingly.
            pomodoroTimerManager.pauseCountdown()
        } else {
            // Resume action pressed
            // Provide a no-op openAndPopUp lambda since BroadcastReceiver cannot show UI directly.
            pomodoroTimerManager.resumeCountdown { _, _ -> }
        }
    }
}

const val EXTRA_TIMER_RUNNING = "EXTRA_TIMER_RUNNING"
const val EXTRA_POMODORO_PHASE = "EXTRA_POMODORO_PHASE"
const val EXTRA_TIME_LEFT_IN_MILLIS = "EXTRA_TIME_LEFT_IN_MILLIS"
const val EXTRA_FOCUS_COMPLETED = "EXTRA_FOCUS_COMPLETED"
const val EXTRA_BREAK_COMPLETED = "EXTRA_BREAK_COMPLETED"
