package com.example.str3ky.ui.session

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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.str3ky.R
import com.example.str3ky.data.CountdownTimerManager
import kotlinx.coroutines.flow.collect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(nav: NavHostController, viewModel: SessionScreenViewModel = hiltViewModel()) {


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Focus Session") },
                actions = {
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back_icon),
                            contentDescription = ""
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
                Timer(nav = nav, viewModel = viewModel)
            }


        }
    )
}

@Composable
private fun Timer(
    modifier: Modifier = Modifier, nav: NavHostController, viewModel: SessionScreenViewModel
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        /*   val focusPeriod = if(viewModel.isSessionInProgress.value){
               "Focus period(${viewModel.currentSession.value.currentSession} of ${viewModel.totalNoOfSessions.value.totalSessions})"
           }
           else if (viewModel.isBreakInProgress.value){
               "Focus period(${viewModel.currentBreak.value.currentBreak} of ${viewModel.totalNoOfBreaks.value.totalBreaks})"
           } else {
               "Focus period(${viewModel.currentSession.value.currentSession} of ${viewModel.totalNoOfSessions.value.totalSessions})"

           }*/
        val pomoName = viewModel.countdownTimerManager.currentPhase.collectAsState().value.name
        val focusSet = viewModel.countdownTimerManager.focusSet.collectAsState(initial = 0)
        val totalFocusSet =
            viewModel.countdownTimerManager.totalFocusSet.collectAsState(initial = 0)
        val breakSet = viewModel.countdownTimerManager.breakSet.collectAsState(initial = 0)
        val totalBreakSet =
            viewModel.countdownTimerManager.totalBreakSet.collectAsState(initial = 0)

        val displayText = when (pomoName) {
            CountdownTimerManager.Phase.FOCUS_SESSION.name -> {
                "$pomoName Period (${focusSet.value} of ${totalFocusSet.value})"
            }

            CountdownTimerManager.Phase.BREAK.name -> {
                "$pomoName Period (${breakSet.value} of ${totalBreakSet.value})"
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
        ) {
            val myFlow =
                viewModel.countdownTimerManager.currentTimeTargetInMillis.collectAsState(initial = 0L)
            val progress =
                (viewModel.countdownTimerManager.timeLeftInMillis.collectAsState(initial = 0L).value.toFloat() / myFlow.value.toFloat())
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
                    color = MaterialTheme.colorScheme.primary.copy(alpha = .25f)
                )
            }
            val i =
                viewModel.countdownTimerManager.timeLeftInMillis.collectAsState(initial = 0L).value
            val minutes = i / 1000 / 60
            val seconds = i / 1000 % 60
            val formattedTime = String.format("%02d:%02d", minutes, seconds)
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
        TimerButton(nav = nav, viewModel = viewModel)
    }
}

@Composable
private fun TimerStartStopButton(
    timerRunning: Boolean,
    nav: NavHostController,
    viewModel: SessionScreenViewModel
) {
    val buttonState = remember {
        mutableStateOf(false)
    }
    IconButton(modifier = Modifier
        .padding(1.dp)
        .width(50.dp)
        .height(50.dp)
        .background(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
        onClick = {
            buttonState.value = !buttonState.value
            // viewModel.startSession()
            viewModel.pauseResumeCountdown(buttonState.value)

        }) {

        Icon(
            painter =
            if (buttonState.value) {
                painterResource(id = R.drawable.play_arrow_fill1_wght400_grad0_opsz24)
            } else{
                painterResource(id = R.drawable.pause_24)
            }
                ,
            contentDescription = "",
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )


    }
}

@Composable
private fun TimerRestartButton(timerRunning: Boolean, viewModel: SessionScreenViewModel) {
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
private fun TimerButton(nav: NavHostController, viewModel: SessionScreenViewModel) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TimerStartStopButton(true, nav = nav, viewModel = viewModel)
        TimerRestartButton(timerRunning = true, viewModel = viewModel)
    }
}


