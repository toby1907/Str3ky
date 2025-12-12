package com.example.str3ky.ui.session

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.str3ky.R
import com.example.str3ky.core.notification.TimerService
import com.example.str3ky.data.CountdownTimerManager
import com.example.str3ky.data.TimerState
import com.example.str3ky.ui.nav.DONE_SCREEN
import com.example.str3ky.ui.nav.SESSION_SCREEN
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

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
                title = { Text(text = "Focus Session") },
                actions = {
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.primaryContainer,
                    actionIconContentColor = colorScheme.onSurface,
                    titleContentColor = colorScheme.onSurface
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
        // Only show COMPLETED when the manager explicitly reports completion, the timer state is Finished,
        // and we have a non-zero finished timestamp. This avoids showing a stale 'Completed' label during navigation/reset races.
        val isManagerCompleted = viewModel.countdownTimerManager.isCompleted.value
        // Use the already-collected timerState parameter instead of calling .value inside composition
        val isTimerFinishedState = timerState == TimerState.Finished
        val lastFinishedAt = viewModel.countdownTimerManager.lastFinishedAt.collectAsState(initial = 0L).value
        val lastFinishedPhase = viewModel.countdownTimerManager.lastFinishedPhase.collectAsState(initial = null).value
        val isFinishedConfirmed = isManagerCompleted && isTimerFinishedState && lastFinishedAt != 0L && lastFinishedPhase != null

        val effectivePhaseEnum = when {
            currentPhaseEnum == CountdownTimerManager.Phase.COMPLETED && isFinishedConfirmed -> CountdownTimerManager.Phase.COMPLETED
            currentPhaseEnum == CountdownTimerManager.Phase.COMPLETED -> CountdownTimerManager.Phase.FOCUS_SESSION
            else -> currentPhaseEnum
        }
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
                color = colorScheme.onPrimaryContainer,
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
                    progress = progress,
                    modifier = modifier
                        .fillMaxSize()
                        .scale(scaleX = -1f, scaleY = 1f),
                    strokeWidth = 16.dp,
                    strokeCap = StrokeCap.Round
                )
                //background
                CircularProgressIndicator(
                    progress = 1f,
                    modifier = modifier
                        .fillMaxSize()
                        .scale(scaleX = -1f, scaleY = 1f),
                    strokeWidth = 16.dp,
                    strokeCap = StrokeCap.Round,
                    color = colorScheme.primary.copy(alpha = .25f)
                )
            }
            val i = timeLeft
            val minutes = i / 1000 / 60
            val seconds = i / 1000 % 60
            val formattedTime = String.format(Locale.US, "%02d:%02d", minutes, seconds)
            Text(
                text = formattedTime,
                style = TextStyle(
                    fontSize = 48.sp,
                    fontWeight = FontWeight(400),
                    color = colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                )
            )
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
        .background(color = colorScheme.primaryContainer, shape = CircleShape),
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
            tint = colorScheme.onPrimaryContainer
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
            color = colorScheme.primaryContainer, shape = CircleShape
        ),
        onClick = {
            viewModel.cancelCountdown()

        }) {

        Icon(
            painter = painterResource(id = R.drawable.refresh_icon),
            contentDescription = "",
            tint = colorScheme.onPrimaryContainer
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
