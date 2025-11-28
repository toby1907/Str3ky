package com.example.str3ky.core.notification

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.str3ky.data.CountdownTimerManager
import com.example.str3ky.millisecondsToMinutes
import com.florianwalther.incentivetimer.core.notification.DefaultNotificationHelper
import com.florianwalther.incentivetimer.core.notification.TIMER_SERVICE_NOTIFICATION_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TimerService : Service() {

    @Inject
    lateinit var serviceScope: CoroutineScope

    @Inject
    lateinit var notificationHelper: DefaultNotificationHelper

    @Inject
    lateinit var countdownTimerManager: CountdownTimerManager
    
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var timerJob: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(TIMER_SERVICE_NOTIFICATION_ID, notificationHelper.getBaseTimerServiceNotification().build())

        timerJob?.cancel()
        timerJob = coroutineScope.launch {
            countdownTimerManager.combinedFlow.collectLatest { timerStates ->
                notificationHelper.updateTimerServiceNotification(
                    timerStates.currentPhase,
                    timerStates.timeLeftInMillis,
                    true,
                    timerStates.goalId,
                    timerStates.totalFocusSet,
                    millisecondsToMinutes(timerStates.timeLeftInMillis),
                    timerStates.progressDate
                )
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        coroutineScope.cancel()
        notificationHelper.removeTimerServiceNotification()
    }
}
