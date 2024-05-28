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
import androidx.navigation.NavHostController
import com.example.str3ky.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(nav: NavHostController) {
    val nameText = remember {
        mutableStateOf("30")
    }
    val selected = remember {
        mutableStateOf(true)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Add") },
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
                    .padding(it)
                ,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Timer(nav = nav)
            }




        }
    )
}
@Composable
private fun Timer(
    modifier: Modifier = Modifier, nav: NavHostController
){
    Column(verticalArrangement = Arrangement.spacedBy(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally){
        Box(
            modifier
                .padding(0.dp)
                .width(235.dp)
                .height(235.dp),
            contentAlignment = Alignment.Center
        ) {
            Box() {
                CircularProgressIndicator(
                    progress = .6f,
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
            Text(
                "25:00",
                style = TextStyle(
                    fontSize = 48.sp,
                    fontWeight = FontWeight(400),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,

                    textAlign = TextAlign.Center,
                )
            )
        }
        TimerButton(nav = nav)
    }
}
@Composable
private fun TimerStartStopButton(
    timerRunning: Boolean,
    nav: NavHostController,
){
 IconButton(modifier = Modifier
     .padding(1.dp)
     .width(50.dp)
     .height(50.dp)
     .background(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
     onClick = {  }) {

     Icon(painter = painterResource(id = R.drawable.play_arrow_fill1_wght400_grad0_opsz24),
         contentDescription = "",
         tint = MaterialTheme.colorScheme.onPrimaryContainer
         )


 }
}
@Composable
private fun TimerRestartButton( timerRunning: Boolean){
    IconButton(modifier = Modifier
        .padding(1.dp)
        .width(50.dp)
        .height(50.dp)
        .background(
            color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape
        ),
        onClick = { /*TODO*/ }) {

        Icon(painter = painterResource(id = R.drawable.refresh_icon),
            contentDescription = "",
            tint = MaterialTheme.colorScheme.onPrimaryContainer )


    }
}
@Composable
private fun TimerButton(nav: NavHostController){
   Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        TimerStartStopButton(true,nav = nav)
        TimerRestartButton(timerRunning = true)
    }
}


