@file:Suppress("unused")

package com.example.str3ky

import android.app.PictureInPictureParams
import android.app.PendingIntent
import android.app.RemoteAction
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.example.str3ky.core.notification.TimerService
import com.example.str3ky.data.CountdownTimerManager.Phase
import com.example.str3ky.data.TimerState
import com.example.str3ky.repository.GoalRepositoryImpl
import com.example.str3ky.theme.Str3kyTheme
import com.example.str3ky.ui.nav.MyAppNavHost
import com.example.str3ky.ui.nav.SESSION_SCREEN
import com.example.str3ky.ui.nav.MAIN_SCREEN
import com.example.str3ky.ui.nav.DONE_SCREEN
import com.example.str3ky.ui.achievements.AchievementBanner
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import java.util.Locale
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.str3ky.ui.snackbar.SnackbarController
import com.example.str3ky.ui.snackbar.SnackbarEvent
import javax.inject.Inject
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.str3ky.data.CountdownTimerManager
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay

private const val USER_PREFERENCES_NAME = "user_preferences"
private const val ACTION_EXPAND_FROM_PIP = "com.example.str3ky.ACTION_EXPAND_FROM_PIP"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = USER_PREFERENCES_NAME)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var goalRepository: GoalRepositoryImpl
    @Inject
    lateinit var countdownTimerManager: CountdownTimerManager

     // Track three states: not checked, granted, denied
     private var permissionState by mutableStateOf<PermissionState>(PermissionState.NotChecked)
    // Compose-observed flag for PiP mode
    private var isInPip by mutableStateOf(false)
    // Handler + runnable to attempt PiP once the activity loses window focus
    private val pipHandler = Handler(Looper.getMainLooper())
    private var pipFallbackRunnable: Runnable? = null

    // Flow for sending PiP intents into Compose (e.g., expand action -> navigate to session)
    private val pipCommand = MutableSharedFlow<Intent>(replay = 1)

    sealed class PermissionState {
        object NotChecked : PermissionState()
        object Granted : PermissionState()
        object Denied : PermissionState()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shouldRequestNotifications = intent.getBooleanExtra(GoalRepositoryImpl.EXTRA_REQUEST_POST_NOTIFICATIONS, false)

        setContent {
            Str3kyTheme {
                val coroutineScope = rememberCoroutineScope()

                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                     contract = ActivityResultContracts.RequestPermission(),
                     onResult = { isGranted ->
                         permissionState = if (isGranted) PermissionState.Granted else PermissionState.Denied
                         if (!isGranted) {
                             coroutineScope.launch {
                                SnackbarController.sendEvent(
                                    SnackbarEvent(
                                        message = "Notification permission denied. Some features may not work.",
                                    )
                                )
                             }
                         }
                     }
                )

                LaunchedEffect(shouldRequestNotifications) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val permission = android.Manifest.permission.POST_NOTIFICATIONS
                        val currentStatus = ContextCompat.checkSelfPermission(applicationContext, permission)
                        permissionState = if (currentStatus == PackageManager.PERMISSION_GRANTED) {
                            PermissionState.Granted
                        } else {
                            PermissionState.Denied
                        }
                        if (shouldRequestNotifications && permissionState == PermissionState.Denied) {
                            notificationPermissionLauncher.launch(permission)
                        }
                    } else {
                        permissionState = PermissionState.Granted
                    }
                }

                // Observe lifecycle to enter PiP when appropriate
                lifecycle.addObserver(object : DefaultLifecycleObserver {
                    override fun onStop(owner: LifecycleOwner) {
                        super.onStop(owner)
                        // Only enter PiP on devices that support it and if a session is active
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                val inProgress = countdownTimerManager.sessionInProgress()
                                if (inProgress) {
                                    enterPipMode()
                                }
                            }
                        } catch (_: Throwable) {
                        }
                    }
                })

                when (permissionState) {
                    PermissionState.NotChecked -> {
                        // Show loading or nothing while checking
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    PermissionState.Granted -> {
                        // Show main app or compact PiP UI
                        val navController = rememberNavController()

                        // Collect PiP commands from activity (e.g. expand action) and react
                        LaunchedEffect(Unit) {
                            pipCommand.collectLatest { intentFromPip ->
                                try {
                                    val gid = intentFromPip.getIntExtra("goalId", -1)
                                    val sess = intentFromPip.getIntExtra("sessionDuration", -1)
                                    Log.d("MainActivity", "PiP expand intent received: goalId=$gid sessionDuration=$sess action=${intentFromPip.action}")

                                    // Decide whether to open the session screen or the done screen based on manager state.
                                    // There is a possible race where the manager's completion fields are set slightly
                                    // after the system sends the expand Intent. Poll briefly (up to ~1s) so we open the
                                    // correct route when the session finished while in PiP.
                                    var managerCompleted = false
                                    try {
                                        var completionAttempts = 0
                                        while (completionAttempts < 10) {
                                            managerCompleted = try {
                                                countdownTimerManager.isCompleted.value || countdownTimerManager.lastFinishedAt.value != 0L || countdownTimerManager.currentPhase.value == Phase.COMPLETED
                                            } catch (_: Throwable) {
                                                false
                                            }
                                            if (managerCompleted) break
                                            delay(100)
                                            completionAttempts++
                                        }
                                    } catch (_: Throwable) {
                                        managerCompleted = false
                                    }

                                    val sessionRoute = "$SESSION_SCREEN?goalId=${gid}&totalSessions=-1&sessionDuration=${sess}&progressDate=0"
                                    val doneRoute = "$DONE_SCREEN?goalId=${gid}&sessionDuration=${sess}&progressDate=0"

                                    // Wait until the NavController has initialized its graph/destination
                                    var attempts = 0
                                    while (navController.currentDestination == null && attempts < 50) {
                                        delay(20)
                                        attempts++
                                    }

                                    val routeToNavigate = if (managerCompleted) doneRoute else sessionRoute

                                    // Try to resume the countdown if session is active but not running in-app.
                                    // Only attempt resume if manager does not report completion.
                                    try {
                                        val inProgress = countdownTimerManager.sessionInProgress()
                                        val isRunning = countdownTimerManager.timerState.value == TimerState.Running
                                        Log.d("MainActivity", "Timer manager state: inProgress=$inProgress isRunning=$isRunning managerCompleted=$managerCompleted")
                                        if (!managerCompleted && inProgress && !isRunning) {
                                            Log.d("MainActivity", "Resuming countdown after expand")
                                            countdownTimerManager.resumeCountdown { routeToOpen, popUp ->
                                                try {
                                                    Log.d("MainActivity", "resumeCountdown requested nav to $routeToOpen")
                                                    navController.navigate(routeToOpen)
                                                } catch (e: Throwable) {
                                                    Log.w("MainActivity", "resumeCountdown navigation failed: ${e.message}")
                                                }
                                            }
                                        }
                                    } catch (resumeEx: Throwable) {
                                        Log.w("MainActivity", "Failed to resume countdown on expand: ${resumeEx.message}")
                                    }

                                    // Navigate to the selected route (session or done)
                                    try {
                                        Log.d("MainActivity", "Navigating to route: $routeToNavigate (attempts=$attempts) managerCompleted=$managerCompleted")
                                        navController.navigate(routeToNavigate) {
                                            launchSingleTop = true
                                            popUpTo(MAIN_SCREEN) { inclusive = false }
                                            restoreState = true
                                        }
                                        Log.d("MainActivity", "Navigation requested to $routeToNavigate")
                                    } catch (navEx: Throwable) {
                                        Log.w("MainActivity", "Navigation to route failed: ${navEx.message}")
                                        try {
                                            navController.navigate(routeToNavigate)
                                            Log.d("MainActivity", "Fallback navigation requested to $routeToNavigate")
                                        } catch (fallbackEx: Throwable) {
                                            Log.e("MainActivity", "Both navigation attempts failed: ${fallbackEx.message}")
                                        }
                                    }
                                 } catch (e: Throwable) {
                                     Log.w("MainActivity", "Error handling PiP expand intent: ${e.message}")
                                 }
                             }
                         }
                         if (isInPip) {
                             // Compact PiP content: show current time left
                             val timeLeft by countdownTimerManager.timeLeftInMillisFlow.collectAsState()
                             val currentPhase by countdownTimerManager.currentPhase.collectAsState()
                             val target by countdownTimerManager.currentTimeTargetInMillisFlow.collectAsState()
                             PiPMiniPlayer(timeLeftMillis = timeLeft, targetMillis = target, phase = currentPhase)
                         } else {
                             Scaffold() {
                                 // Show achievement banner at the top of the scaffold content
                                 Box(modifier = Modifier.padding(it)) {
                                     Column {
                                         AchievementBanner()
                                         MyAppNavHost(navController = navController, modifier = Modifier.weight(1f))
                                     }
                                     // No manual PiP button: PiP is entered automatically when the user leaves the app
                                     // (onUserLeaveHint/onPause/onWindowFocusChanged handle automatic PiP entry).
                                  }
                              }
                         }
                    }
                    PermissionState.Denied -> {
                        // Show permission request UI
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Please grant notification permission to proceed.")
                            Button(onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }) {
                                Text("Grant Permission")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun enterPipMode() {
         if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

         // Ensure device supports PiP and is not a low-ram device
         val pm = packageManager
         val supportsPip = pm.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
         val am = getSystemService(Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
         val isLowRam = am?.isLowRamDevice ?: false
         if (!supportsPip || isLowRam) {
             Log.w("MainActivity", "Device does not support PiP or is low-ram: supportsPip=$supportsPip isLowRam=$isLowRam")
             return
         }

         // Run UI work on main thread to ensure enterPictureInPictureMode is invoked correctly
         runOnUiThread {
             try {
                 // Ensure the TimerService is running so notifications and background updates continue
                 try {
                     val refreshIntent = Intent(this, TimerService::class.java).apply {
                         action = TimerService.ACTION_REFRESH_NOTIFICATION
                     }
                     startService(refreshIntent)
                 } catch (e: Exception) {
                     Log.w("MainActivity", "Failed to ensure TimerService running before PiP: ${e.message}")
                 }

                 val title = "Str3ky Session"
                 val subtitle = "Focus ongoing"
                 val builder = PictureInPictureParams.Builder()
                     .setAspectRatio(Rational(16, 9))
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                     // setTitle/setSubtitle are available on API 33+
                     builder.setTitle(title)
                     builder.setSubtitle(subtitle)
                 }

                 // Add actions for PiP
                 val actions = mutableListOf<RemoteAction>()
                 val playPauseIntent = Intent(this, TimerService::class.java).apply {
                     action = TimerService.ACTION_PIP_TOGGLE_PLAY_PAUSE
                 }
                 val playPausePending = PendingIntent.getService(this, 1, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                 val playPauseIconRes = if (countdownTimerManager.timerState.value == com.example.str3ky.data.TimerState.Running) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
                 val playPauseAction = RemoteAction(
                     Icon.createWithResource(this, playPauseIconRes),
                     "Toggle",
                     "Play/Pause",
                     playPausePending
                 )
                 actions.add(playPauseAction)

                 val stopIntent = Intent(this, TimerService::class.java).apply {
                     action = TimerService.ACTION_PIP_STOP
                 }
                 val stopPendingIntent = PendingIntent.getService(this, 2, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                 val stopAction = RemoteAction(
                     Icon.createWithResource(this, android.R.drawable.ic_menu_close_clear_cancel),
                     "Stop",
                     "Stop the session",
                     stopPendingIntent
                 )
                 actions.add(stopAction)

                 // Add an explicit 'Expand' action that brings the activity to foreground
                 try {
                     val expandIntent = Intent(this, MainActivity::class.java).apply {
                         action = ACTION_EXPAND_FROM_PIP
                         addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        // Attach current session params so Compose can restore exact session state
                        putExtra("goalId", countdownTimerManager.goalId.value)
                        putExtra("sessionDuration", countdownTimerManager.sessionDuration.value)
                     }
                     val expandPending = PendingIntent.getActivity(this, 3, expandIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                     val expandAction = RemoteAction(
                         Icon.createWithResource(this, android.R.drawable.ic_menu_view),
                         "Open",
                         "Open the app",
                         expandPending
                     )
                     actions.add(expandAction)
                 } catch (t: Throwable) {
                     Log.w("MainActivity", "Failed to add PiP expand action: ${t.message}")
                 }

                 // Log the actions being set
                 Log.d("MainActivity", "Attempting to enter PiP mode with actions: $actions")

                 val params = builder.setActions(actions).build()
                 val entered = enterPictureInPictureMode(params)
                 Log.d("MainActivity", "enterPictureInPictureMode returned: $entered")
             } catch (t: Throwable) {
                 Log.w("MainActivity", "enterPipMode failed: ${t.message}")
             }
         }
     }

    override fun onUserLeaveHint() {
         super.onUserLeaveHint()
         // Called when the user navigates away (e.g., presses Home or switches to another app).
         // If a session is in progress, enter PiP so the user keeps controls and the session remains visible.
         try {
             if (countdownTimerManager.sessionInProgress()) {
                 enterPipMode()
             }
         } catch (e: Exception) {
             Log.w("MainActivity", "onUserLeaveHint failed to enter PiP: ${e.message}")
         }
     }

    override fun onNewIntent(intent: Intent) {
         super.onNewIntent(intent)
         try {
             val act = intent.action
             if (act == ACTION_EXPAND_FROM_PIP) {
                 // System should bring the activity to the foreground; ensure local PiP flag updates
                 runOnUiThread {
                     isInPip = false
                 }
                // Notify Compose to open the session screen so user continues in-app
                try {
                    lifecycleScope.launch {
                        pipCommand.emit(intent)
                    }
                } catch (_: Throwable) {}
             }
         } catch (e: Exception) {
             Log.w("MainActivity", "onNewIntent handling failed: ${e.message}")
         }
    }

    override fun onPause() {
        super.onPause()
        // onPause fires in many situations; only enter PiP when not finishing, not already in PiP,
        // and a session is active. This helps catching transitions to another app that may not
        // call onUserLeaveHint in some cases (e.g., launching another app from Recents).
        try {
            if (!isInPip && !isFinishing && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (countdownTimerManager.sessionInProgress()) {
                    Log.d("MainActivity", "onPause: session active, attempting to enter PiP")
                    // Immediate attempt
                    enterPipMode()
                    // Schedule a fallback attempt after a short delay in case the immediate
                    // attempt didn't succeed (covers the case of switching to another app).
                    pipFallbackRunnable?.let { pipHandler.removeCallbacks(it) }
                    pipFallbackRunnable = Runnable {
                        try {
                            if (!isInPip && !hasWindowFocus() && countdownTimerManager.sessionInProgress()) {
                                Log.d("MainActivity", "onPause fallback: activity lost focus; entering PiP")
                                enterPipMode()
                            }
                        } catch (e: Exception) {
                            Log.w("MainActivity", "onPause fallback PiP attempt failed: ${e.message}")
                        }
                    }
                    pipHandler.postDelayed(pipFallbackRunnable!!, 250)
                 }
             }
         } catch (e: Exception) {
             Log.w("MainActivity", "onPause PiP attempt failed: ${e.message}")
         }
     }

    override fun onResume() {
        super.onResume()
        // Cancel any pending PiP fallback attempts when the activity comes back to foreground
        pipFallbackRunnable?.let { pipHandler.removeCallbacks(it); pipFallbackRunnable = null }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // If we lost window focus while a session is active and not already in PiP,
        // try to enter PiP. This covers switching to another app where onPause/onUserLeaveHint
        // may not reliably be delivered beforehand.
        try {
            if (!hasFocus && !isInPip && !isFinishing && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (countdownTimerManager.sessionInProgress()) {
                    Log.d("MainActivity", "onWindowFocusChanged: lost focus -> entering PiP")
                    enterPipMode()
                }
            }
        } catch (e: Exception) {
            Log.w("MainActivity", "onWindowFocusChanged PiP attempt failed: ${e.message}")
        }
    }

    @Suppress("DEPRECATION")
    @Deprecated("Overrides deprecated framework callback")
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        runOnUiThread {
            isInPip = isInPictureInPictureMode
            // If PiP was dismissed while a session is active, ensure the service posts the
            // correct running / resume notification so the user can continue pausing/resuming
            // from the notification. Do NOT mark progress as completed here — progress is
            // only updated when the session actually finishes.
            if (!isInPictureInPictureMode) {
                // Defer decision for a short moment to let the system bring activity to foreground
                // if the user enlarged the PiP. If the activity gains window focus we assume the
                // user returned to the app (enlarged), so do nothing. Otherwise, the PiP was
                // dismissed and we should refresh the notification so controls remain available.
                try {
                    if (countdownTimerManager.sessionInProgress()) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            try {
                                if (this.hasWindowFocus()) {
                                    // Activity is in foreground (PiP enlarged) — let the UI continue.
                                    Log.d("MainActivity", "PiP exit detected: activity in foreground (enlarged). No notification refresh needed.")
                                } else {
                                    Log.d("MainActivity", "PiP exit detected: activity not in foreground; sending refresh notification.")
                                    sendRefreshNotificationToService()
                                }
                            } catch (inner: Exception) {
                                Log.w("MainActivity", "Error during PiP exit follow-up: ${inner.message}")
                                // Fallback: send refresh to be safe
                                sendRefreshNotificationToService()
                            }
                        }, 300L)
                    }
                } catch (e: Exception) {
                    Log.w("MainActivity", "Failed to schedule notification refresh on PiP exit: ${e.message}")
                }
            }
        }
    }

    private fun sendRefreshNotificationToService() {
        try {
            val intent = Intent(this, TimerService::class.java).apply {
                action = TimerService.ACTION_REFRESH_NOTIFICATION
            }
            // Use startService so the service will be created if necessary and handle the action
            startService(intent)
        } catch (e: Exception) {
            Log.w("MainActivity", "sendRefreshNotificationToService failed: ${e.message}")
        }
    }
}

@Composable
fun PermissionDialog(
    title: String,
    description: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title)
        },
        text = {
            Text(text = description)
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(text = stringResource(id = R.string.ok))
            }
        }
    )
}

@Composable
fun PiPMiniPlayer(timeLeftMillis: Long, targetMillis: Long, phase: Phase) {
    // Simple compact UI for PiP mode
    val seconds = (timeLeftMillis / 1000) % 60
    val minutes = (timeLeftMillis / 1000) / 60
    val targetSeconds = (targetMillis / 1000) % 60
    val targetMinutes = (targetMillis / 1000) / 60
    val phaseColor = when (phase) {
        Phase.FOCUS_SESSION -> MaterialTheme.colorScheme.primary
        Phase.BREAK -> MaterialTheme.colorScheme.secondary
        Phase.COMPLETED -> MaterialTheme.colorScheme.tertiary
    }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(color = phaseColor)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Adapt font size to available width so the single-line text fits the PiP window.
        // Increased sizes for better readability in PiP.
        val availableWidth = maxWidth
        val fontSize = when {
            availableWidth < 160.dp -> 22.sp
            availableWidth < 240.dp -> 24.sp
            availableWidth < 320.dp -> 26.sp
            else -> 28.sp
        }

        val textToShow = if (phase == Phase.COMPLETED) {
            "Session completed!"
        } else {
            String.format(Locale.getDefault(), "%02d:%02d  •  of %02d:%02d", minutes, seconds, targetMinutes, targetSeconds)
        }

        Text(
            text = textToShow,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.onPrimary, fontSize = fontSize)
        )
    }
}
