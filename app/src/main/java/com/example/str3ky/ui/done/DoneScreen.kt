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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.navigation.NavHostController
import com.example.str3ky.R
import com.example.str3ky.theme.Str3kyTheme
import com.example.str3ky.toMinutes
import com.example.str3ky.ui.nav.PROGRESS_SCREEN
import com.example.str3ky.ui.nav.SESSION_SCREEN
import kotlin.text.count
import kotlin.text.toFloat


@Composable
fun CompletedScreen(
    viewModel: DoneScreenViewModel = hiltViewModel(),
    sessionDuration:Long,
    nav: NavHostController
) {
    val nameText = remember {
        mutableStateOf("30")
    }
    val selected = remember {
        mutableStateOf(true)
    }
    Scaffold(
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
                        color = colorScheme.onPrimaryContainer,
                    )
                )

                Spacer(modifier = Modifier.padding(24.dp))

                Timer(viewModel = viewModel,sessionDuration = sessionDuration,nav = nav)
            }


        }
    )
}

@Composable
private fun Timer(
    modifier: Modifier = Modifier,
    viewModel: DoneScreenViewModel,
    sessionDuration: Long,
    nav: NavHostController
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

                val daysCompleted = viewModel.goal.value.goal?.progress?.count { it.completed }?:0
                val totalDays =viewModel.goal.value.goal?.noOfDays?:0
                val progress = if (totalDays > 0) daysCompleted.toFloat() / totalDays.toFloat() else 0f

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
                        color = colorScheme.onPrimary,
                        shape = RoundedCornerShape(size = 8.dp)
                    )
            ) {


                val dayHourSpent = viewModel.dayHourSpent.collectAsState().value.toMinutes()
                val textValue = if (((viewModel.goal.value.goal?.focusSet?.toMinutes()
                        ?.minus(dayHourSpent)) ?: 0) <= 5
                ){
                    "You've met your daily goal!"
                }
                else "${viewModel.goal.value.goal?.focusSet?.toMinutes()?.minus(dayHourSpent)?:0} remaining to meet your daily goal"
                Text(
                    text = textValue,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight(500),
                        color = colorScheme.primary,
                        textAlign = TextAlign.Center,
                    ),
                    maxLines = 3,
                )
            }
        }
        Button(modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(size = 4.dp)
            ),
            onClick = { nav.navigate(PROGRESS_SCREEN+"?goalId=${viewModel.currentGoalId.value}&progressDate=${viewModel.progressDate.value}") }) {
            Text(text = "Continue",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                )
                )
        }

    }
}

