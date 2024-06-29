package com.example.str3ky.ui.done

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.str3ky.R
import com.example.str3ky.theme.Str3kyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedScreen(
    viewModel: DoneScreenViewModel = hiltViewModel(),
    sessionDuration:Long,
) {
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
                    .padding(it),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.padding(24.dp))

                Text(
                    text = "Daily Task Complete! +10xp",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight(700),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                )

                Spacer(modifier = Modifier.padding(24.dp))

                Timer(viewModel = viewModel,sessionDuration = sessionDuration)
            }


        }
    )
}

@Composable
private fun Timer(
    modifier: Modifier = Modifier,
    viewModel: DoneScreenViewModel,
    sessionDuration: Long
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier
                .padding(0.dp)
                .width(235.dp)
                .height(235.dp),
            contentAlignment = Alignment.Center
        ) {

            Box() {

                val myFlow =
                    viewModel.focusTime.value.focusTime.countdownTime
                val progress =
                    (sessionDuration.toFloat() / myFlow.toFloat())

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
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 70.dp, height = 84.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.main_flame_img),
                        contentDescription = "",
                        modifier = Modifier
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
                val streakValue = viewModel.completedStreak.collectAsState()
                val text = if (streakValue.value > 1) {
                    "${streakValue.value} days streak"
                } else {
                    "${streakValue.value} day streak"
                }
                Text(

                    text,
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight(700),
                        color = MaterialTheme.colorScheme.primary,
                    )
                )
            }
        }


        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.blob),
                contentDescription = ""
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .width(203.dp)
                    .height(76.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(size = 8.dp)
                    )
            ) {
                Text(
                    text = "You've met your daily \ngoal!",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight(500),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                    )
                )
            }
        }
        Button(modifier = Modifier
            .width(97.dp)
            .height(40.dp)
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(size = 10.dp)
            ),
            onClick = { /*TODO*/ }) {
            Text(text = "Continue")
        }

    }
}


@Preview(showBackground = true)
@Composable
fun CompletePreview() {

    Str3kyTheme {
        CompletedScreen(sessionDuration = 10000L)
    }
}