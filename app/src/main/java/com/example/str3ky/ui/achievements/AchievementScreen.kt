package com.example.str3ky.ui.achievements

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.str3ky.ui.nav.MAIN_SCREEN
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.str3ky.R
import com.example.str3ky.data.Achievement
import com.example.str3ky.data.User.Companion.DEFAULT
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementScreen(navController: NavHostController,) {

    Scaffold (
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Achievements",
                        style = MaterialTheme.typography.titleLarge.copy(color = colorScheme.onPrimary)
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

    if (user != null) {
        val unlocked = achievements.filter { it.isUnlocked }
        val locked = achievements.filter { !it.isUnlocked }

        // Use a vertical list with two sections and card-based items for a cleaner UI
        androidx.compose.foundation.lazy.LazyColumn(modifier = modifier.padding(12.dp)) {
            item {
                // Header: stats
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProgressDisplayComponent(
                        title = "Total Hours",
                        innertext = String.format(Locale.getDefault(), "%.1f", user!!.totalHoursSpent)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    ProgressDisplayComponent(
                        title = "Best Streak",
                        innertext = user!!.longestStreak.toString()
                    )
                }
            }

            // Unlocked section
            item {
                Text(
                    text = "Unlocked",
                    style = MaterialTheme.typography.titleMedium.copy(color = colorScheme.onPrimary),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (unlocked.isNotEmpty()) {
                item {
                    BoxWithConstraints {
                        if (maxWidth < 700.dp) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(unlocked.size) { idx ->
                                    val a = unlocked[idx]
                                    // determine original threshold from Achievements constants
                                    val original = Achievements.allAchievements.firstOrNull { it.name == a.name }
                                    val progress = when {
                                        original?.hoursRemaining != null -> (user!!.totalHoursSpent / original.hoursRemaining.toDouble()).toFloat().coerceIn(0f, 1f)
                                        original?.daysRemaining != null -> (user!!.longestStreak.toDouble() / original.daysRemaining.toDouble()).toFloat().coerceIn(0f, 1f)
                                        else -> null
                                    }
                                    val remainingText = when {
                                        original?.hoursRemaining != null -> {
                                            val rem = (original.hoursRemaining - user!!.totalHoursSpent).coerceAtLeast(0.0)
                                            if (rem > 0) String.format(Locale.getDefault(), "%.1f h to go", rem) else ""
                                        }
                                        original?.daysRemaining != null -> {
                                            val rem = (original.daysRemaining - user!!.longestStreak).coerceAtLeast(0)
                                            if (rem > 0) "$rem day${if (rem!=1) "s" else ""} to go" else ""
                                        }
                                        else -> null
                                    }

                                    // animate each card when entering
                                    val containerColor = when {
                                        // If this achievement is a top-tier (original thresholds), give it a golden tint
                                        original?.hoursRemaining == 100 || original?.daysRemaining == 30 -> Color(0xFFFFD700).copy(alpha = 0.12f)
                                        a.isUnlocked -> colorScheme.primaryContainer
                                        else -> colorScheme.surfaceVariant
                                    }
                                    val borderColor = when {
                                        // If this achievement is a top-tier (original thresholds), give it a golden tint
                                        original?.hoursRemaining == 100 || original?.daysRemaining == 30 -> Color(0xFFFFD700)
                                        a.isUnlocked -> colorScheme.primary
                                        else -> null
                                    }
                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = true,
                                        enter = fadeIn(animationSpec = tween(260, delayMillis = idx * 80)) + scaleIn(initialScale = 0.92f, animationSpec = tween(260, delayMillis = idx * 80)),
                                        exit = fadeOut() + scaleOut()
                                    ) {
                                        RewardCard(a, width = 160.dp, iconSize = 64.dp, containerColor = containerColor, borderColor = borderColor, progress = progress, remainingText = remainingText, onClick = {
                                            selectedAchievement = a
                                            showDialog = true
                                        })
                                    }
                                }
                            }
                        } else {
                            LazyVerticalGrid(columns = GridCells.Adaptive(160.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(4.dp)) {
                                items(unlocked.size) { idx ->
                                    val a = unlocked[idx]
                                    val original = Achievements.allAchievements.firstOrNull { it.name == a.name }
                                    val progress = when {
                                        original?.hoursRemaining != null -> (user!!.totalHoursSpent / original.hoursRemaining.toDouble()).toFloat().coerceIn(0f, 1f)
                                        original?.daysRemaining != null -> (user!!.longestStreak.toDouble() / original.daysRemaining.toDouble()).toFloat().coerceIn(0f, 1f)
                                        else -> null
                                    }
                                    val remainingText = when {
                                        original?.hoursRemaining != null -> {
                                            val rem = (original.hoursRemaining - user!!.totalHoursSpent).coerceAtLeast(0.0)
                                            if (rem > 0) String.format(Locale.getDefault(), "%.1f h to go", rem) else ""
                                        }
                                        original?.daysRemaining != null -> {
                                            val rem = (original.daysRemaining - user!!.longestStreak).coerceAtLeast(0)
                                            if (rem > 0) "$rem day${if (rem!=1) "s" else ""} to go" else ""
                                        }
                                        else -> null
                                    }

                                    // animate each card when entering
                                    val containerColor = when {
                                        // If this achievement is a top-tier (original thresholds), give it a golden tint
                                        original?.hoursRemaining == 100 || original?.daysRemaining == 30 -> Color(0xFFFFD700).copy(alpha = 0.12f)
                                        a.isUnlocked -> colorScheme.primaryContainer
                                        else -> colorScheme.surfaceVariant
                                    }
                                    val borderColor = when {
                                        // If this achievement is a top-tier (original thresholds), give it a golden tint
                                        original?.hoursRemaining == 100 || original?.daysRemaining == 30 -> Color(0xFFFFD700)
                                        a.isUnlocked -> colorScheme.primary
                                        else -> null
                                    }
                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = true,
                                        enter = fadeIn(animationSpec = tween(260, delayMillis = idx * 60)) + scaleIn(initialScale = 0.92f, animationSpec = tween(260, delayMillis = idx * 60)),
                                        exit = fadeOut() + scaleOut()
                                    ) {
                                        RewardCard(a, width = 160.dp, iconSize = 64.dp, containerColor = containerColor, borderColor = borderColor, progress = progress, remainingText = remainingText, onClick = {
                                            selectedAchievement = a
                                            showDialog = true
                                        })
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    Text(text = "No unlocked achievements yet.", modifier = Modifier.padding(8.dp), color = colorScheme.onPrimary, style = MaterialTheme.typography.bodyLarge)
                }
            }

            // Locked section
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Locked",
                    style = MaterialTheme.typography.titleMedium.copy(color = colorScheme.onPrimary),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (locked.isNotEmpty()) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(locked.size) { idx ->
                            val a = locked[idx]
                            val original = Achievements.allAchievements.firstOrNull { it.name == a.name }
                            val progress = when {
                                original?.hoursRemaining != null -> {
                                    val threshold = original.hoursRemaining.toDouble()
                                    (user!!.totalHoursSpent / threshold).toFloat().coerceIn(0f, 1f)
                                }
                                original?.daysRemaining != null -> {
                                    val threshold = original.daysRemaining.toDouble()
                                    (user!!.longestStreak.toDouble() / threshold).toFloat().coerceIn(0f, 1f)
                                }
                                else -> null
                            }

                            val remainingText = when {
                                original?.hoursRemaining != null -> {
                                    val rem = (original.hoursRemaining - user!!.totalHoursSpent).coerceAtLeast(0.0)
                                    if (rem > 0) String.format(Locale.getDefault(), "%.1f h to go", rem) else ""
                                }
                                original?.daysRemaining != null -> {
                                    val rem = (original.daysRemaining - user!!.longestStreak).coerceAtLeast(0)
                                    if (rem > 0) "$rem day${if (rem!=1) "s" else ""} to go" else ""
                                }
                                else -> null
                            }

                            androidx.compose.animation.AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.92f, animationSpec = tween(220)),
                                exit = fadeOut() + scaleOut()
                            ) {
                                RewardCard(a, width = 160.dp, progress = progress, remainingText = remainingText, onClick = {
                                    selectedAchievement = a
                                    showDialog = true
                                })
                            }
                        }
                    }
                }
            } else {
                item {
                    Text(text = "All achievements unlocked — great job!", modifier = Modifier.padding(8.dp), color = colorScheme.onPrimary, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    } else {
        Text(text = "Loading...", modifier = Modifier.padding(12.dp))
    }

    if (showDialog && selectedAchievement != null) {
        RewardsDialog(
            onDismissRequest = {
                showDialog = false
            },
            onConfirmation = {
                viewModel.markAchievementSeen(selectedAchievement!!.name)
                showDialog = false
            },
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
            style = MaterialTheme.typography.bodyLarge.copy(color = colorScheme.onPrimary)
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
fun RewardCard(
    reward: Achievement = DEFAULT,
    width: Dp = 160.dp,
    iconSize: Dp = 56.dp,
    containerColor: Color = colorScheme.surfaceVariant,
    borderColor: Color? = null,
     progress: Float? = null,
     remainingText: String? = null,
     onClick: () -> Unit
) {
    val baseModifier = Modifier
        .width(width)
        .clickable { onClick() }

    val cardModifier = if (borderColor != null) baseModifier.border(width = 2.dp, color = borderColor, shape = RoundedCornerShape(12.dp)) else baseModifier

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp).testTag("reward_${reward.name}"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                val colorFilter = if (reward.isUnlocked) null else ColorFilter.tint(colorScheme.onSurface.copy(alpha = 0.35f))
                Image(
                    imageVector = reward.iconKey.rewardIcon,
                    contentDescription = reward.iconKey.name,
                    modifier = Modifier.size(iconSize),
                    colorFilter = colorFilter
                )
            }

            Text(text = reward.name, style = MaterialTheme.typography.titleMedium.copy(color = colorScheme.onPrimary))

            // Show remaining text (friendly) if provided
            if (!remainingText.isNullOrEmpty()) {
                Text(text = remainingText, style = MaterialTheme.typography.bodyLarge.copy(color = colorScheme.onPrimary.copy(alpha = 0.85f), fontSize = 13.sp),)
            }

            // subtle progress indicator using computed progress when available
            if (!reward.isUnlocked) {
                val p = progress ?: 0f
                LinearProgressIndicator(progress = { p }, modifier = Modifier.height(6.dp))
                if (progress != null) {
                    Text(text = "${(p * 100).toInt()}%", style = TextStyle(fontSize = 12.sp), color = colorScheme.onPrimary.copy(alpha = 0.8f))
                }
            } else {
                // Show NEW badge if unlocked but not seen
                if (!reward.isSeen) {
                    Box(modifier = Modifier
                        .background(color = colorScheme.primary, shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(text = "NEW", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, color = colorScheme.onPrimary))
                    }
                } else {
                    Text(text = "Unlocked", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold), color = colorScheme.primary)
                }
            }
        }
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
                        text = dialogTitle,
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
                                    imageVector = icon,
                                    contentDescription = dialogTitle,
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
                            if (dialogText.isNotBlank()) {
                                Text(
                                    text = dialogText,
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp,
                                        color = colorScheme.onPrimary.copy(alpha = 0.9f),
                                    ),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        LottieAnimation(composition = composition,
                            modifier = Modifier
                                .size(200.dp)
                                .fillMaxSize()
                                .align(Alignment.Center),
                            progress = { AnimationProgress })
                    }

                }

                // Confirmation button
                val haptic = LocalHapticFeedback.current
                Button(
                    onClick = {
                        // small haptic feedback on claim
                        try {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        } catch (_: Throwable) {
                        }
                        onConfirmation()
                    },
                    modifier = Modifier.testTag("claim_button")
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    )
                ) {
                    Text(text = "Claim Reward", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold))
                }
            }
        }


    }
}
