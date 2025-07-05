package com.example.str3ky.ui.progress

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.str3ky.R
import com.example.str3ky.convertCalendarDayOfWeekToStrings
import com.example.str3ky.data.DayProgress
import com.example.str3ky.ui.nav.ADD_CHALLENGE_SCREEN
import com.example.str3ky.ui.nav.MAIN_SCREEN
import com.example.str3ky.ui.nav.SESSION_SETTINGS_SCREEN
import java.util.Calendar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(viewModel: ProgressScreenViewModel = hiltViewModel(), nav: NavHostController) {

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = viewModel.goalName.value.goalName) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            nav.navigate(MAIN_SCREEN)
                        }
                    ) {
                        Icon(modifier = Modifier.padding(8.dp),
                            painter = painterResource(id = R.drawable.arrow_back_icon),
                            contentDescription = "",)
                    }

                },
                actions = {
                    // Deactivated the Edit functionality cause its causing bug
                  /*  IconButton(onClick = {

                        nav.navigate(
                            "$ADD_CHALLENGE_SCREEN?goalId=${viewModel.goalId.value}&goalColor=${viewModel.goalColor.value}"
                        )
                        Log.d("ProgressScreen", "ProgressScreen: ${viewModel.goalId.value}")
                    }) {
                        Text("Edit")
                    }*/

                }

            )
        },
        content = {
            val currentDate = System.currentTimeMillis()

            val dummyProgress = listOf(
                DayProgress(currentDate, false,30), // Today (completed)
                DayProgress(
                    System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000,
                    true,
                    30
                ), // Two days ago (completed)
                DayProgress(
                    System.currentTimeMillis() - 4 * 24 * 60 * 60 * 1000,
                    false,
                    30
                ), // Four days ago (not completed)
                // ... add more dummy entries as needed
            ).sortedBy { dayProgres ->
                dayProgres.date
            }
            val progress = viewModel.progress.value.sortedBy { dayProgres ->
                dayProgres.date
            }

            TableProgress(
                progress = progress,
                modifier = Modifier.padding(it),
                currentDate = currentDate,
                nav = nav,
                viewModel = viewModel
            )
        }
    )
}

@Composable
fun TableProgress(
    progress: List<DayProgress>,
    modifier: Modifier,
    currentDate: Long,
    nav: NavHostController,
    viewModel: ProgressScreenViewModel
) {
    val gridSize = 6 // Number of rows and columns
    val tileSize = 56.dp
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.pulsing))
    val AnimationProgress by animateLottieCompositionAsState(composition = composition, iterations = LottieConstants.IterateForever)

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = "Daily Progress",
            style = TextStyle(
                fontSize = 18.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight(400),
                color = Color(0xFFFFFFFF)
            ),
            modifier = Modifier.padding(8.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            contentPadding = PaddingValues(8.dp), // Add padding around the grid
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(progress.size) { index ->
                val dayProgress = progress[index]

                val currentCalendar = Calendar.getInstance()
                currentCalendar.timeInMillis = currentDate

                val yourDateCalendar = Calendar.getInstance()
                yourDateCalendar.timeInMillis = dayProgress.date

                val isActive = (currentCalendar.get(Calendar.YEAR) == yourDateCalendar.get(Calendar.YEAR)
                        && currentCalendar.get(Calendar.MONTH) == yourDateCalendar.get(Calendar.MONTH)
                        && currentCalendar.get(Calendar.DAY_OF_MONTH) == yourDateCalendar.get(Calendar.DAY_OF_MONTH))


            //    val isActive = dayProgress.date == currentDate
                val tileColor =
                    if (isActive || dayProgress.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.inverseOnSurface
                val indicator = convertCalendarDayOfWeekToStrings(yourDateCalendar.get(Calendar.DAY_OF_WEEK)).take(2)
                   // if (isActive || dayProgress.completed) (index + 1).toString() else ""
                val showCheckMark = dayProgress.completed
                val textColor = if (isActive || dayProgress.completed){
                    MaterialTheme.colorScheme.background
                }
                else{
                    MaterialTheme.colorScheme.onPrimary
                }

                Box(
                    modifier = Modifier
                        .size(tileSize)
                        .clip(RoundedCornerShape(4.dp))
                        .background(tileColor)
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable {
                            if (isActive && !dayProgress.completed) {
                                nav.navigate(SESSION_SETTINGS_SCREEN+"?goalId=${viewModel.goalId.value}&focusTime=${viewModel.focusTime.value.totalTime}&progressDate=${dayProgress.date}")
                            }
                        }
                ) {
                    if(isActive&&!showCheckMark){

                        LottieAnimation(composition = composition, modifier = Modifier.size(120.dp),progress={AnimationProgress})
                    }
                    if (showCheckMark) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.Center)
                        )
                    }

                    Text(
                        text = indicator,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .background(Color.Transparent)
                            .padding(2.dp),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }
        }
    }
}





