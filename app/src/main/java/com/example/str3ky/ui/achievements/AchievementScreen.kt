package com.example.str3ky.ui.achievements

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
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
import com.example.str3ky.data.Achievement
import com.example.str3ky.data.User.Companion.DEFAULT
import com.example.str3ky.toMinutes
import com.example.str3ky.ui.nav.MAIN_SCREEN

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementScreen(navController: NavHostController,) {

    Scaffold (
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Achievements",
                        style = TextStyle(
                            fontSize = 24.sp,
                            color = colorScheme.onPrimary,
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigate(MAIN_SCREEN) }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back_icon),
                            contentDescription = "Back arrow"
                            )

                        }
                }
            )
        }

    ){ padding ->
        AchievementScreenContent( modifier = Modifier.padding(padding))
    }




}

@Composable
fun AchievementScreenContent(modifier: Modifier = Modifier,
    viewModel: AchievementViewModel = hiltViewModel()) {

    val user by viewModel.user.collectAsState()
    val achievements by viewModel.achievements.collectAsState()


    var showDialog by remember { mutableStateOf(false) }
    var selectedAchievement by remember { mutableStateOf<Achievement?>(null) }

    if(user!=null) {

              LazyVerticalGrid(
                  columns = GridCells.Fixed(3),
                  modifier = modifier
                      .padding(top = 8.dp, start = 4.dp, bottom = 8.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                  item(span = {
                      GridItemSpan(maxLineSpan)
                  }){
                     Column(horizontalAlignment = Alignment.CenterHorizontally,
                         verticalArrangement = Arrangement.Center)  {
                          ProgressDisplayComponent(
                              title = "Total Hour Spent",
                              innertext = (user!!.totalHoursSpent).toString()
                          )
                          Spacer(modifier = Modifier.size(16.dp))
                          ProgressDisplayComponent(
                              title = "Highest Streak",
                              innertext = user!!.longestStreak.toString()
                          )
                         Spacer(modifier = Modifier.size(16.dp))
                      }
                  }
                  item(span = {
                      GridItemSpan(maxLineSpan)
                  }){

                          Text(
                              text = "Achievements UnLocked",
                              style = TextStyle(
                                  fontSize = 16.sp,
                                  lineHeight = 24.sp,
                                  color = colorScheme.onPrimary,
                              )
                          )


                  }
                  items(
                      achievements.size,
                  ) { user ->
                      RewardItems(achievements[user],
                          onClick = {
                              selectedAchievement = achievements[user]
                              showDialog = true
                          }
                      )

                  }
                  item(span = {
                      GridItemSpan(maxLineSpan)
                  }){

                    Spacer(modifier = Modifier.size(16.dp))


                  }
                  item(span = {
                      GridItemSpan(maxLineSpan)
                  }){

                          Text(
                              text = "Achievements Locked",
                              style = TextStyle(
                                  fontSize = 16.sp,
                                  lineHeight = 24.sp,
                                  color = colorScheme.onPrimary,
                              )
                          )


                  }

                  items(
                      achievements.size,
                  ) { user ->
                      RewardItems(achievements[user],
                          onClick = {
                              selectedAchievement = achievements[user]
                              showDialog = true
                          }
                      )
                  }
              }



    }
    else{
        Text(text = "Loading...")
    }

    if (showDialog && selectedAchievement != null) {
        RewardsDialog(
            onDismissRequest = { showDialog = false },
            onConfirmation = { showDialog = false },
            dialogTitle = "Reward",
            dialogText = "You have unlocked a new reward!",
            icon = selectedAchievement!!.iconKey.rewardIcon,
            reward = selectedAchievement!!
        )
    }

}

@Composable
private fun ProgressDisplayComponent(title: String, innertext: String) {
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.progress))
    val animationProgress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = true, // Ensure the animation plays
        iterations = 1, // Play only once
        restartOnPlay = false,// Prevent restart when recomposed
        speed = 2f
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(13.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = colorScheme.onPrimary,
            )
        )

        Box {
            LottieAnimation(
                composition = composition,
                modifier = Modifier.size(120.dp),
                progress = { animationProgress }
            )

            androidx.compose.animation.AnimatedVisibility(
                visible = animationProgress == 1f,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = innertext,
                    style = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = colorScheme.onPrimary,
                    )
                )
            }
        }
    }
}


@Composable
fun RewardItems(reward: Achievement = DEFAULT, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.clickable { onClick() }
    ) {


        Box(
            Modifier
                .border(width = 2.dp, color = colorScheme.secondaryContainer)
                .padding(4.dp)
                .width(81.dp)
                .height(81.dp)
        ) {
            val colorFilter = if (reward.isUnlocked) {
                null // No color filter for unlocked achievements
            } else {
                ColorFilter.tint(colorScheme.secondaryContainer) // Gray tint for locked achievements
            }

            Image(
                imageVector = reward.iconKey.rewardIcon,
                contentDescription = reward.iconKey.name,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(80.dp),
                colorFilter = colorFilter
            )
        }
        Text(
            text = reward.name,
            style = TextStyle(
                fontSize = 12.sp,

                color = colorScheme.secondaryContainer,
            )
        )
        Text(
            text = reward.chanceInPercent.toString()+"/100",
            style = TextStyle(
                fontSize = 12.sp,

                color = colorScheme.secondaryContainer,

            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
    reward: Achievement
) {

    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.congrats))
    val AnimationProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BasicAlertDialog(
            onDismissRequest = {
                onDismissRequest()
            }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(color = colorScheme.primaryContainer)


            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.weight(1f)) // Pushes the icon to the end
                    Text(
                        text = "Bravo!",
                        style = TextStyle(
                            fontSize = 18.sp,
                            lineHeight = 28.sp,
                            color = colorScheme.onPrimary,
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onDismissRequest() }) {
                        Icon(
                            imageVector = Icons.Outlined.Cancel,
                            contentDescription = "Close Dialog",
                            tint = colorScheme.onPrimary,
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = colorScheme.surface)

                ) {


                    Box(
                        Modifier

                            .padding(2.dp)
                            .width(200.dp)
                            .height(200.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier =Modifier.align(Alignment.Center)){
                            Box(
                                Modifier
                                    .border(width = 2.dp, color = colorScheme.onPrimary)
                                    .padding(2.dp)
                                    .width(81.dp)
                                    .height(81.dp)

                            ) {
                                val colorFilter = if (reward.isUnlocked) {
                                    null // No color filter for unlocked achievements
                                } else {
                                    ColorFilter.tint(Color.Gray) // Gray tint for locked achievements
                                }

                                Image(
                                    imageVector = reward.iconKey.rewardIcon,
                                    contentDescription = reward.iconKey.name,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(80.dp),
                                    colorFilter = colorFilter
                                )

                            }
                            Text(
                                text = reward.name,
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    lineHeight = 28.sp,
                                    color = colorScheme.onPrimary,
                                ),

                            )
                            Text(
                                text = reward.chanceInPercent.toString(),
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    lineHeight = 28.sp,
                                    color = colorScheme.onPrimary,
                                ),
                                modifier = Modifier

                            )
                        }

                        LottieAnimation(composition = composition,
                            modifier = Modifier
                                .size(200.dp)
                                .fillMaxSize()
                                .align(Alignment.Center),
                            progress = { AnimationProgress })
                    }

                }

            }
        }


    }
}
