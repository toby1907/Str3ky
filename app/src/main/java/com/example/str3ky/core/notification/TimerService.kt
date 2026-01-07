package com.example.str3ky.core.notification

import android.app.Service
import android.app.Notification
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.example.str3ky.data.CountdownTimerManager
import com.example.str3ky.data.CountdownTimerManager.Phase
import com.example.str3ky.di.ApplicationScope
import com.example.str3ky.millisecondsToMinutes
import com.florianwalther.incentivetimer.core.notification.DefaultNotificationHelper
import com.florianwalther.incentivetimer.core.notification.TIMER_SERVICE_NOTIFICATION_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.str3ky.data.TimerState

@AndroidEntryPoint
class TimerService : Service() {

    @Inject
    @ApplicationScope
    lateinit var serviceScope: CoroutineScope

    @Inject
    lateinit var notificationHelper: DefaultNotificationHelper

    @Inject
    lateinit var countdownTimerManager: CountdownTimerManager

    @Inject
    lateinit var timerStateCache: com.example.str3ky.data.TimerServiceStateCache

    private var timerJob: Job? = null
    // When true, the next finished event received will be ignored. Used to avoid handling a stale
    // finished event when the user starts a fresh session immediately after completion.
    @Volatile
    private var suppressNextFinished = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle PiP action intents quickly before full startup logic
        val action = intent?.action
        if (action != null) {
            Log.d(TAG, "TimerService received action: $action")
            when (action) {
                ACTION_REFRESH_NOTIFICATION -> {
                    // If service is already running and collectors are active, do a quick refresh
                    // and return. If the service is not yet started, fall through so the startup
                    // path will call startForeground and set up collectors which will keep
                    // updating notifications regularly.
                    val serviceAlreadyRunning = timerJob != null && timerJob?.isActive == true
                    if (serviceAlreadyRunning) {
                        serviceScope.launch {
                            try {
                                val timerStates = countdownTimerManager.combinedFlow.first()
                                val isRunning = (countdownTimerManager.timerState.value == TimerState.Running) && countdownTimerManager.sessionInProgress()
                                if (isRunning) {
                                    notificationHelper.updateTimerServiceNotification(
                                        timerStates.currentPhase,
                                        timerStates.timeLeftInMillis,
                                        true,
                                        timerStates.goalId,
                                        timerStates.totalFocusSet,
                                        millisecondsToMinutes(timerStates.timeLeftInMillis),
                                        timerStates.progressDate,
                                        timerStates.focusCompleted,
                                        timerStates.breakCompleted
                                    )
                                } else {
                                    notificationHelper.showResumeTimerNotification(
                                        currentPhase = timerStates.currentPhase,
                                        timeLeftInMillis = timerStates.timeLeftInMillis,
                                        focusCompleted = timerStates.focusCompleted,
                                        breakCompleted = timerStates.breakCompleted
                                    )
                                }
                            } catch (e: Exception) {
                                Log.w(TAG, "Failed to refresh notification: ${e.message}")
                            }
                        }
                        return START_NOT_STICKY
                    }
                    // else: allow normal startup flow to run (startForeground + collectors)
                }
                ACTION_PIP_TOGGLE_PLAY_PAUSE -> {
                    try {
                        // If running -> pause, else resume
                        if (countdownTimerManager.timerState.value == TimerState.Running && countdownTimerManager.sessionInProgress()) {
                            Log.d(TAG, "PiP action: pausing countdown")
                            countdownTimerManager.pauseCountdown()
                        } else {
                            Log.d(TAG, "PiP action: resuming countdown")
                            // resumeCountdown expects an openAndPopUp lambda; provide a no-op
                            countdownTimerManager.resumeCountdown { _, _ -> }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to handle PIP toggle action: ${e.message}")
                    }
                    return START_NOT_STICKY
                }
                ACTION_PIP_STOP -> {
                    Log.d(TAG, "PiP action: stop requested")
                    try {
                        handleStop()
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to handle PIP stop action: ${e.message}")
                    }
                    return START_NOT_STICKY
                }
            }
        }

        // Use API-34 overload to provide foreground service type when available to satisfy targetSdk 34 requirements.
        val baseNotification = notificationHelper.getBaseTimerServiceNotification().build()

        try {
            if (Build.VERSION.SDK_INT >= 34) {
                // Try to use the API-34 three-arg startForeground at runtime via reflection.
                // This avoids a compile-time dependency on ServiceInfo when compileSdk < 34.
                try {
                    val serviceInfoClass = Class.forName("android.app.ServiceInfo")
                    val field = serviceInfoClass.getField("FOREGROUND_SERVICE_TYPE_DATA_SYNC")
                    val type = (field.get(null) as? Int) ?: 0

                    // Call startForeground(int, Notification, int) reflectively
                    val startForegroundMethod = Service::class.java.getMethod(
                        "startForeground",
                        Int::class.javaPrimitiveType,
                        Notification::class.java,
                        Int::class.javaPrimitiveType
                    )
                    startForegroundMethod.invoke(this, TIMER_SERVICE_NOTIFICATION_ID, baseNotification, type)
                } catch (inner: Exception) {
                    Log.w(TAG, "Reflection startForeground failed, falling back to two-arg startForeground: ${inner.message}")
                    startForeground(TIMER_SERVICE_NOTIFICATION_ID, baseNotification)
                }
            } else {
                startForeground(TIMER_SERVICE_NOTIFICATION_ID, baseNotification)
            }
        } catch (e: Exception) {
            Log.w(TAG, "startForeground failed: ${e.message}")
            startForeground(TIMER_SERVICE_NOTIFICATION_ID, baseNotification)
        }

        timerJob?.cancel()
        timerJob = serviceScope.launch {
            // Collector A: observe combined state for running/paused updates
            launch {
                countdownTimerManager.combinedFlow.collectLatest { timerStates ->
                    // Persist the latest timer state for restoration after process death
                    timerStateCache.updateTimerState(timerStates)

                    // Guard: if the manager already reports COMPLETED/Finished, do not post any
                    // running/resume service notifications — remove them instead. This prevents
                    // races where the service posts a stale running notification just after the
                    // manager transitioned to COMPLETED and posted the completed notification.
                    val managerTimerState = countdownTimerManager.timerState.value
                    val managerCompletedFlag = try { countdownTimerManager.isCompleted.value } catch (e: Exception) { false }
                    if (managerTimerState == TimerState.Finished || managerCompletedFlag) {
                        Log.d(TAG, "Manager in Finished/Completed state; removing service/resume notifications and skipping update.")
                        notificationHelper.removeTimerServiceNotification()
                        notificationHelper.removeResumeTimerNotification()
                        return@collectLatest
                    }

                    // Consider the manager 'running' only when its timerState reports Running
                    // and the sessionInProgress() flag is true. This reduces races where a
                    // very-late combinedFlow emission looks 'running' but the manager has
                    // already transitioned to Finished.
                    val isRunning = (countdownTimerManager.timerState.value == TimerState.Running) && countdownTimerManager.sessionInProgress()

                    // Additional race guard: if the remaining time is very small (<= 1500ms)
                    // and the manager recently recorded a finished timestamp, skip posting
                    // the running notification to avoid the stale "Focus X/Y 0:01" case.
                    val lastFinished = try { countdownTimerManager.lastFinishedAt.value } catch (e: Exception) { 0L }
                    if (timerStates.timeLeftInMillis <= 1500L && lastFinished != 0L && kotlin.math.abs(System.currentTimeMillis() - lastFinished) < 3000L) {
                        Log.d(TAG, "Skipping service update because timeLeft=${timerStates.timeLeftInMillis} and lastFinished was $lastFinished")
                        // Remove any running/resume notifications just in case
                        notificationHelper.removeTimerServiceNotification()
                        notificationHelper.removeResumeTimerNotification()
                        return@collectLatest
                    }

                    if (isRunning) {
                        notificationHelper.updateTimerServiceNotification(
                            timerStates.currentPhase,
                            timerStates.timeLeftInMillis,
                            true,
                            timerStates.goalId,
                            timerStates.totalFocusSet,
                            millisecondsToMinutes(timerStates.timeLeftInMillis),
                            timerStates.progressDate,
                            timerStates.focusCompleted,
                            timerStates.breakCompleted
                        )
                        // Clear any stale resume notification
                        notificationHelper.removeResumeTimerNotification()
                    } else {
                        // Do not show resume notification when the timer is in Initial state (e.g., after reset)
                        val ts = countdownTimerManager.timerState.value
                        // If the manager is in Initial state, ensure no resume or running notifications are shown.
                        if (ts == com.example.str3ky.data.TimerState.Initial) {
                            notificationHelper.removeTimerServiceNotification()
                            notificationHelper.removeResumeTimerNotification()
                        } else {
                            // Remove any running notification first to avoid showing a stale running notification
                            notificationHelper.removeTimerServiceNotification()
                            // Show resume notification when paused
                            notificationHelper.showResumeTimerNotification(
                                currentPhase = timerStates.currentPhase,
                                timeLeftInMillis = timerStates.timeLeftInMillis,
                                focusCompleted = timerStates.focusCompleted,
                                breakCompleted = timerStates.breakCompleted
                            )
                        }
                    }
                }
            }

            // Collector B: separate finished event handler to post completed notifications deterministically
            launch {
                countdownTimerManager.timerFinishedEvent.collectLatest { token ->
                    // If suppression was requested, ignore the first finished event and clear the flag
                    if (suppressNextFinished) {
                        Log.d(TAG, "Suppressing finished event token=$token due to suppressNextFinished")
                        suppressNextFinished = false
                        return@collectLatest
                    }

                    try {
                        Log.d(TAG, "Received timerFinishedEvent token=$token, manager.lastFinishedAt=${countdownTimerManager.lastFinishedAt.value}")

                        // Verify token matches manager's lastFinishedAt to avoid stale events
                        if (token != countdownTimerManager.lastFinishedAt.value) {
                            Log.w(TAG, "Finished token $token did not match manager.lastFinishedAt ${countdownTimerManager.lastFinishedAt.value}; ignoring")
                            return@collectLatest
                        }

                        // Wait briefly for the manager to set final completion flags to avoid races
                        var managerReportsFinished = countdownTimerManager.isCompleted.value || countdownTimerManager.timerState.value == TimerState.Finished
                        var attempts = 0
                        while (!managerReportsFinished && attempts < 10) {
                            delay(100)
                            managerReportsFinished = countdownTimerManager.isCompleted.value || countdownTimerManager.timerState.value == TimerState.Finished
                            attempts++
                        }

                        if (!managerReportsFinished) {
                            Log.w(TAG, "Finished event received but manager did not report final completion state after wait; skipping completed notification")
                            return@collectLatest
                        }

                        val goalId = countdownTimerManager.goalId.value
                        // Post completed notification whenever we have a valid goalId (don't gate on totalFocusSet)
                        if (goalId != -1) {
                            // Extra safety: if manager currently reports a session in progress, skip posting completed
                            if (countdownTimerManager.sessionInProgress()) {
                                Log.w(TAG, "Manager reports session in progress at finished token=$token; skipping completed notification to avoid race")
                                return@collectLatest
                            }
                            Log.d(TAG, "Posting completed notification for goalId=$goalId token=$token")
                            val finishedPhase = countdownTimerManager.lastFinishedPhase.value ?: Phase.COMPLETED
                            notificationHelper.showTimerCompletedNotification(
                                finishedPhase = finishedPhase,
                                goalId = goalId,
                                progressDate = countdownTimerManager.progressDate.value,
                                sessionDuration = countdownTimerManager.sessionDuration.value.toLong()
                            )
                            // Ensure any running/resume notifications are removed to avoid stale UI
                            notificationHelper.removeTimerServiceNotification()
                            notificationHelper.removeResumeTimerNotification()
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Error while handling finished event: ${e.message}")
                    }

                    // Clear persisted timer state so a subsequent fresh session doesn't reload completed state
                    try {
                        timerStateCache.clear()
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to clear timer state cache after finished event: ${e.message}")
                    }

                    // Stop the foreground service now that session reached COMPLETED
                    if (Build.VERSION.SDK_INT >= 24) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                    } else {
                        @Suppress("DEPRECATION")
                        stopForeground(true)
                    }
                    stopSelf()
                }
            }
        }

        handleStart(intent)

        return START_STICKY
    }

    private fun handleStart(intent: Intent?) {
        // Support an optional action to suppress the next finished event (helps avoid race with UI)
        val action = intent?.action
        if (action == ACTION_SUPPRESS_NEXT_FINISHED) {
            // Only suppress next finished if the manager actually reports a previous finished timestamp.
            // This prevents suppressing a legitimate finished event when starting fresh after clearing state.
            val last = try {
                countdownTimerManager.lastFinishedAt.value
            } catch (e: Exception) {
                0L
            }
            if (last != 0L) {
                Log.d(TAG, "handleStart: ACTION_SUPPRESS_NEXT_FINISHED received - will ignore next finished event (lastFinishedAt=$last)")
                suppressNextFinished = true
            } else {
                Log.d(TAG, "handleStart: ACTION_SUPPRESS_NEXT_FINISHED ignored - no previous finished timestamp")
            }
             // Clear any completed notification and cached state proactively
             try {
                 timerStateCache.clear()
             } catch (e: Exception) {
                 Log.w(TAG, "Failed to clear timer state cache on suppress action: ${e.message}")
             }
             notificationHelper.removeTimerCompletedNotification()
             // still continue to save state if extras are present
         }

        val goalId = intent?.getIntExtra(EXTRA_GOAL_ID, countdownTimerManager.goalId.value)
        val totalSessions = intent?.getIntExtra(EXTRA_TOTAL_SESSIONS, 0) ?: 0
        val sessionDuration = intent?.getIntExtra(EXTRA_SESSION_DURATION, 0) ?: 0
        val progressDate = intent?.getLongExtra(EXTRA_PROGRESS_DATE, 0) ?: 0
        timerStateCache.saveState(goalId ?: -1, totalSessions, sessionDuration, progressDate)
    }

    private fun handleStop() {
        timerJob?.cancel()
        countdownTimerManager.cancelCountdown()
        timerStateCache.clear()
        if (Build.VERSION.SDK_INT >= 24) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
        stopSelf()
    }

    override fun onBind(p0: Intent?): IBinder? {
        // This is a started (foreground) service; no binding provided.
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        notificationHelper.removeTimerServiceNotification()
    }

    companion object {
        private const val TAG = "TimerService"

        const val EXTRA_GOAL_ID = "extra_goal_id"
        const val EXTRA_TOTAL_SESSIONS = "extra_total_sessions"
        const val EXTRA_SESSION_DURATION = "extra_session_duration"
        const val EXTRA_PROGRESS_DATE = "extra_progress_date"

        const val ACTION_SUPPRESS_NEXT_FINISHED = "com.example.str3ky.action.SUPPRESS_NEXT_FINISHED"
        const val ACTION_PIP_TOGGLE_PLAY_PAUSE = "com.example.str3ky.action.PIP_TOGGLE_PLAY_PAUSE"
        const val ACTION_PIP_STOP = "com.example.str3ky.action.PIP_STOP"
        const val ACTION_REFRESH_NOTIFICATION = "com.example.str3ky.action.REFRESH_NOTIFICATION"
    }
}
















