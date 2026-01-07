package com.example.str3ky.ui.session

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
 import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import com.example.str3ky.R
import com.example.str3ky.core.notification.TimerService
import com.example.str3ky.data.CountdownTimerManager
import com.example.str3ky.data.TimerState
import com.example.str3ky.ui.nav.DONE_SCREEN
import com.example.str3ky.ui.nav.SESSION_SCREEN
import java.util.Locale
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(
    nav: NavHostController,
    viewModel: SessionScreenViewModel = hiltViewModel(),
    openAndPopUp: (String, String) -> Unit,
) {

    val timerState by viewModel.timerState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    val view = LocalView.current
    // Use the app background color for both the status bar and the screen
    val screenBackground: Color = MaterialTheme.colorScheme.background
    val opaqueBackground = screenBackground.copy(alpha = 1f)
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as? Activity)?.window?.apply {
                try { WindowCompat.setDecorFitsSystemWindows(this, false) } catch (_: Throwable) {}
                statusBarColor = opaqueBackground.toArgb()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try { setStatusBarContrastEnforced(false) } catch (_: Throwable) {}
                }
            }
        }
    }

    // Notification permission request (Android 13+)
    val showPermissionDialog = remember { mutableStateOf(false) }
    val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.POST_NOTIFICATIONS else null

    val hasNotificationPermission = if (notificationPermission != null) {
        ContextCompat.checkSelfPermission(context, notificationPermission) == PackageManager.PERMISSION_GRANTED
    } else true

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (!granted) {
                // If user denied and we should NOT show rationale, it means "Don't ask again" -> navigate to in-app help
                val shouldShow = activity?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, notificationPermission ?: "") } ?: true
                if (!shouldShow) {
                    // navigate to in-app help screen
                    nav.navigate(com.example.str3ky.ui.nav.NOTIFICATIONS_HELP_SCREEN)
                }
            }
            showPermissionDialog.value = false
        }
    )

    LaunchedEffect(key1 = Unit) {
        // Removed automatic start on entering the screen. User must press play to start.
        // Prompt for notification permission once when entering the screen if not granted
        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            showPermissionDialog.value = true
        }

        viewModel.sessionCompleted.collectLatest { completed ->
            if (completed) {
                // Stop the service and navigate to done screen
                val intent = Intent(context, TimerService::class.java)
                context.stopService(intent)
                // Navigate/pop with same deep link args as before
                openAndPopUp(
                    DONE_SCREEN + "?goalId=${viewModel.countdownTimerManager.goalId.value}&sessionDuration=${viewModel.countdownTimerManager._sessionTotalDurationMillis.value}&progressDate=${viewModel.countdownTimerManager.progressDate.value}",
                    SESSION_SCREEN
                )
            }
        }
    }

    // Defensive navigation: when returning from PiP the sessionCompleted flow may not replay.
    // Use the persistent manager state to decide whether we should navigate to DONE_SCREEN.
    val lastFinishedAtState = viewModel.countdownTimerManager.lastFinishedAt.collectAsState(initial = 0L)
    val lastFinishedPhaseState = viewModel.countdownTimerManager.lastFinishedPhase.collectAsState(initial = null)
    val currentPhaseState = viewModel.countdownTimerManager.currentPhase.collectAsState(initial = CountdownTimerManager.Phase.FOCUS_SESSION).value
    // Remember a one-time navigation guard to avoid double navigation (survives simple recompositions, but resets when screen is recreated)
    val navigationHandled = rememberSaveable { mutableStateOf(false) }
    // One-time feedback guard (sound/vibration)
    val feedbackHandled = rememberSaveable { mutableStateOf(false) }
    // Track whether this screen instance was opened as a fresh navigation (user explicitly came here)
    // or as a resume from background/PiP. We treat the first ON_RESUME after onStart as initial entry.
    val isInitialEntry = rememberSaveable { mutableStateOf(true) }

    // Play a one-time sound & vibration when session completion is detected
    @SuppressLint("MissingPermission")
    fun playCompletionFeedback(ctx: Context) {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(ctx, uri)
            ringtone?.play()
        } catch (_: Throwable) {}
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ctx.getSystemService(Vibrator::class.java)
            } else null
            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(200)
                }
            }
        } catch (_: Throwable) {}
    }

    // Observe lifecycle resume (covers expanding from PiP). When resumed, check manager state and navigate if needed.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val lastFinishedAt = lastFinishedAtState.value
                val lastFinishedPhase = lastFinishedPhaseState.value
                val finishedByManager = lastFinishedAt != 0L && lastFinishedPhase != null
                val finishedByPhase = currentPhaseState == CountdownTimerManager.Phase.COMPLETED
                val managerCompletedNow = viewModel.countdownTimerManager.isCompleted.value

                // Log state for debugging
                Log.d(
                    "SessionScreen",
                    "onResume: isInitialEntry=${isInitialEntry.value} managerCompleted=$managerCompletedNow finishedByManager=$finishedByManager finishedByPhase=$finishedByPhase navigationHandled=${navigationHandled.value}"
                )

                if (isInitialEntry.value) {
                    // Mark that we've handled the initial resume for this screen instance.
                    // If this is a PiP/notification resume (service already running & session completed),
                    // we still want to redirect to DONE_SCREEN. For a genuine fresh navigation to this
                    // screen after completion (user wants another session), timerState should be Initial
                    // and managerCompletedNow/finished flags should be false.
                    isInitialEntry.value = false

                    // If this very first resume already reports a completed session, treat it as a
                    // PiP/background resume and redirect to DONE_SCREEN. This avoids the case where
                    // expanding PiP shows a stale Session screen.
                    if (!navigationHandled.value && (managerCompletedNow || finishedByManager || finishedByPhase)) {
                        try {
                            context.stopService(Intent(context, TimerService::class.java))
                        } catch (_: Throwable) {}
                        openAndPopUp(
                            DONE_SCREEN + "?goalId=${viewModel.countdownTimerManager.goalId.value}&sessionDuration=${viewModel.countdownTimerManager._sessionTotalDurationMillis.value}&progressDate=${viewModel.countdownTimerManager.progressDate.value}",
                            SESSION_SCREEN
                        )
                        navigationHandled.value = true
                    }
                } else {
                    // Subsequent resumes: if the manager reports completion and we haven't
                    // navigated yet, redirect to DONE_SCREEN. This covers later PiP expands
                    // or returning from background while the same screen instance is alive.
                    if (!navigationHandled.value && (managerCompletedNow || finishedByManager || finishedByPhase)) {
                        try {
                            context.stopService(Intent(context, TimerService::class.java))
                        } catch (_: Throwable) {}
                        openAndPopUp(
                            DONE_SCREEN + "?goalId=${viewModel.countdownTimerManager.goalId.value}&sessionDuration=${viewModel.countdownTimerManager._sessionTotalDurationMillis.value}&progressDate=${viewModel.countdownTimerManager.progressDate.value}",
                            SESSION_SCREEN
                        )
                        navigationHandled.value = true
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // When lastFinishedAt/lastFinishedPhase change, only play feedback and log — navigation is handled on lifecycle resume
    LaunchedEffect(lastFinishedAtState.value, lastFinishedPhaseState.value) {
        val lastFinishedAt = lastFinishedAtState.value
        val lastFinishedPhase = lastFinishedPhaseState.value
        Log.d("SessionScreen", "LaunchedEffect (no-nav): currentPhase=$currentPhaseState lastFinishedAt=$lastFinishedAt lastFinishedPhase=$lastFinishedPhase navigationHandled=${navigationHandled.value}")
        val finishedByManager = lastFinishedAt != 0L && lastFinishedPhase != null
        val managerCompletedNow = viewModel.countdownTimerManager.isCompleted.value
        val finishedConfirmed = managerCompletedNow || finishedByManager || currentPhaseState == CountdownTimerManager.Phase.COMPLETED
        if (finishedConfirmed && !feedbackHandled.value) {
            playCompletionFeedback(context)
            feedbackHandled.value = true
        }
    }

    if (showPermissionDialog.value && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog.value = false },
            title = { Text(text = context.getString(R.string.notifications_permission_title)) },
            text = { Text(text = context.getString(R.string.notifications_permission_message)) },
            confirmButton = {
                TextButton(onClick = {
                    notificationPermission?.let { permissionLauncher.launch(it) }
                }) {
                    Text(text = context.getString(R.string.allow))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog.value = false }) {
                    Text(text = context.getString(R.string.later))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .background(opaqueBackground)
                    .statusBarsPadding(),
                title = { Text(text = "Focus Session") },
                actions = {
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = opaqueBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    IconButton(onClick = { nav.navigateUp() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back_icon),
                            contentDescription = "Back"
                        )
                    }
                }

            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(screenBackground)
                    .padding(it),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Timer(
                    viewModel = viewModel,
                    openAndPopUp = openAndPopUp,
                    timerState = timerState
                )
            }


        }
    )
}

@Composable
private fun Timer(
    modifier: Modifier = Modifier,
    viewModel: SessionScreenViewModel,
    openAndPopUp: (String,String) -> Unit,
    timerState: TimerState
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val currentPhaseEnum = viewModel.countdownTimerManager.currentPhase.collectAsState().value
        // Determine completion from manager fields. Split checks so we don't include `currentPhaseEnum` in the
        // `isFinishedByManager` flag (that would make the effectivePhaseEnum branch tautological).
        val lastFinishedAt = viewModel.countdownTimerManager.lastFinishedAt.collectAsState(initial = 0L).value
        val lastFinishedPhase = viewModel.countdownTimerManager.lastFinishedPhase.collectAsState(initial = null).value
        val managerCompleted = viewModel.countdownTimerManager.isCompleted.value
        // finishedByManager = explicit timestamp/phase recorded by manager/service
        val finishedByManager = lastFinishedAt != 0L && lastFinishedPhase != null
        // For UI purposes, consider session finished if managerCompleted OR finishedByManager OR the current phase is COMPLETED
        val isFinishedConfirmed = managerCompleted || finishedByManager || (currentPhaseEnum == CountdownTimerManager.Phase.COMPLETED)

        // effectivePhaseEnum: if the manager reports the phase is COMPLETED but the manager hasn't yet recorded
        // finished timestamp/flag, treat it as FOCUS_SESSION to avoid showing completed prematurely.
        val effectivePhaseEnum = if (currentPhaseEnum == CountdownTimerManager.Phase.COMPLETED) {
            if (managerCompleted || finishedByManager) CountdownTimerManager.Phase.COMPLETED else CountdownTimerManager.Phase.FOCUS_SESSION
        } else currentPhaseEnum
        val pomoName = effectivePhaseEnum.name
        val focusSet = viewModel.countdownTimerManager.focusSet.collectAsState(initial = 0)
        // Collect totalFocusSet first so we can use it in computedInitialSessions without calling .value on flows directly
        val totalFocusSetState = viewModel.countdownTimerManager.totalFocusSet.collectAsState(initial = viewModel.initialTotalSessions)
        val managerTotalSessions = totalFocusSetState.value
        val computedInitialSessions = maxOf(viewModel.initialTotalSessions, managerTotalSessions)

        val totalFocusSet = totalFocusSetState
        // Initialize breakSet using the already-collected public focusSet state instead of accessing a private flow
        val breakSet = viewModel.countdownTimerManager.breakSet.collectAsState(initial = focusSet.value)
        // Derive the displayed total breaks directly from the total sessions (totalSessions - 1)
        val displayedTotalBreaks = if (totalFocusSet.value > 1) totalFocusSet.value - 1 else 0

        // Log the computed seeds once at composition to help debug transient UI seeds
        LaunchedEffect(key1 = computedInitialSessions) {
            Log.d("SessionScreen", "computedInitialSessions=$computedInitialSessions displayedTotalBreaks=${if (computedInitialSessions>1) computedInitialSessions-1 else 0} managerTotalSessions=${managerTotalSessions} saved=${viewModel.initialTotalSessions}")
        }

        // Use timerState as the primary source of truth for whether a session/break is active to
        // avoid a small race where the manager's isSessionInProgress flow may update slightly later.
        val isActiveByTimerState = timerState is TimerState.Running || timerState is TimerState.Paused

        // Compute displayed current index: if the phase is active (by timerState) treat it as current (completed+1)
        val currentFocusIndex = if (effectivePhaseEnum == CountdownTimerManager.Phase.FOCUS_SESSION && isActiveByTimerState) {
            focusSet.value + 1
        } else {
            focusSet.value
        }

        val currentBreakIndex = if (effectivePhaseEnum == CountdownTimerManager.Phase.BREAK && isActiveByTimerState) {
            breakSet.value + 1
        } else {
            breakSet.value
        }

        val displayText = when (pomoName) {
            CountdownTimerManager.Phase.FOCUS_SESSION.name -> {
                // Shorter label: "Focus X/Y"
                "Focus $currentFocusIndex/${totalFocusSet.value}"
            }

            CountdownTimerManager.Phase.BREAK.name -> {
                "Break $currentBreakIndex/$displayedTotalBreaks"
            }

            else -> {
                "Focus Session Completed"
            }
        }

        Text(
            text = displayText, style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight(400),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center,
            )
        )
        Box(
            modifier
                .padding(0.dp)
                .width(235.dp)
                .height(235.dp),
            contentAlignment = Alignment.Center
        )
        {
            val myFlow =
                viewModel.countdownTimerManager.currentTimeTargetInMillis.collectAsState(initial = 0L)
            val timeLeft = viewModel.countdownTimerManager.timeLeftInMillis.collectAsState(initial = 0L).value
            val progress = if (myFlow.value > 0L) (timeLeft.toFloat() / myFlow.value.toFloat()) else 0f
            Box() {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = modifier
                        .fillMaxSize()
                        .scale(scaleX = -1f, scaleY = 1f),
                    strokeWidth = 16.dp,
                    strokeCap = StrokeCap.Round
                )
                //background
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = modifier
                        .fillMaxSize()
                        .scale(scaleX = -1f, scaleY = 1f),
                    strokeWidth = 16.dp,
                    strokeCap = StrokeCap.Round,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = .25f)
                )
            }
            val i = timeLeft
            val minutes = i / 1000 / 60
            val seconds = i / 1000 % 60
            val formattedTime = String.format(Locale.US, "%02d:%02d", minutes, seconds)
            if (isFinishedConfirmed) {
                Text(
                    text = "Session completed!",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center,
                    )
                )
            } else {
                Text(
                    text = formattedTime,
                    style = TextStyle(
                        fontSize = 48.sp,
                        fontWeight = FontWeight(400),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center,
                    )
                )
            }
        }
        // Pass a boolean to indicate whether the timer is running so the button shows correct icon
        TimerButton(viewModel = viewModel, openAndPopUp = openAndPopUp, timerRunning = (timerState is TimerState.Running))
    }
}

@Composable
private fun TimerStartStopButton(
    timerRunning: Boolean,
    viewModel: SessionScreenViewModel,
    openAndPopUp: (String, String) -> Unit
) {
    // Button visual and behaviour derive from the actual running state passed in
    IconButton(modifier = Modifier
        .padding(1.dp)
        .width(50.dp)
        .height(50.dp)
        .background(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
        onClick = {
            // Use viewmodel's single API to toggle based on current running state
            viewModel.pauseResumeCountdown(openAndPopUp)
        }) {

        Icon(
            painter = if (timerRunning) {
                painterResource(id = R.drawable.pause_24)
            } else {
                painterResource(id = R.drawable.play_arrow_fill1_wght400_grad0_opsz24)
            },
            contentDescription = "",
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )


    }
}

@Composable
private fun TimerRestartButton(viewModel: SessionScreenViewModel) {
    IconButton(modifier = Modifier
        .padding(1.dp)
        .width(50.dp)
        .height(50.dp)
        .background(
            color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape
        ),
        onClick = {
            viewModel.cancelCountdown()

        }) {

        Icon(
            painter = painterResource(id = R.drawable.refresh_icon),
            contentDescription = "",
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )


    }
}

@Composable
private fun TimerButton(
    viewModel: SessionScreenViewModel,
    openAndPopUp: (String, String) -> Unit,
    timerRunning: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TimerStartStopButton(timerRunning = timerRunning, viewModel = viewModel, openAndPopUp = openAndPopUp)
        TimerRestartButton(viewModel = viewModel)
    }
}
