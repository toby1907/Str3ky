package com.example.str3ky.ui.achievements

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
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
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.border

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementScreen(navController: NavHostController, viewModel: AchievementViewModel = hiltViewModel()) {

    var showNotifications by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedAchievement by remember { mutableStateOf<Achievement?>(null) }
    val unseen by viewModel.unseenCount.collectAsState()

    // Helper to open an achievement from anywhere (notifications or lists)
    val openAchievement: (Achievement) -> Unit = { a ->
        selectedAchievement = a
        showDialog = true
    }

    Scaffold(
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
                },
                actions = {
                    // Notification bell with unread count
                    IconButton(onClick = { showNotifications = true }) {
                        Box {
                            Icon(imageVector = Icons.Filled.Notifications, contentDescription = "Notifications", tint = colorScheme.onPrimary)
                            if (unseen > 0) {
                                // keep the pip compact but increase text for readability
                                Box(modifier = Modifier
                                    .size(18.dp) // keep compact dot
                                    .clip(CircleShape)
                                    .background(color = Color(0xFFEF4444))
                                    .align(Alignment.TopEnd)) {
                                    Text(
                                        text = if (unseen > 9) "9+" else unseen.toString(),
                                        color = Color.White,
                                        fontSize = 16.sp, // larger text inside compact pip
                                        modifier = Modifier.align(Alignment.Center),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        AchievementScreenContent(modifier = Modifier.padding(padding), viewModel = viewModel, onOpenAchievement = openAchievement)

        if (showNotifications) {
            val recent by viewModel.recentUnlocked.collectAsState()
            NotificationsDialog(
                recent = recent,
                onDismiss = { showNotifications = false },
                onOpen = { ach ->
                    showNotifications = false
                    openAchievement(ach)
                },
                onMarkAllRead = {
                    viewModel.markAllSeen()
                    showNotifications = false
                }
            )
        }

        // Render the Rewards dialog at the top level so notifications can open it
        if (showDialog && selectedAchievement != null) {
            RewardsDialog(
                onDismissRequest = { showDialog = false },
                onConfirmation = {
                    viewModel.markAchievementSeen(selectedAchievement!!.name)
                    showDialog = false
                },
                dialogTitle = selectedAchievement!!.name,
                dialogText = selectedAchievement!!.description,
                 icon = selectedAchievement!!.iconKey.rewardIcon,
                 reward = selectedAchievement!!
            )
        }
    }
}

@Composable
fun AchievementScreenContent(modifier: Modifier = Modifier,
    viewModel: AchievementViewModel = hiltViewModel(),
    onOpenAchievement: (Achievement) -> Unit
) {

    val user by viewModel.user.collectAsState()
    val achievements by viewModel.achievements.collectAsState()


    // Dialog state moved to parent; use onOpenAchievement to request opening an achievement

    if (user != null) {
        // Claimed (seen) badges — only these appear in the Badges section
        val claimedBadges = achievements.filter { it.isUnlocked && it.isSeen }
        // Newly unlocked but not yet claimed — show in a separate "New" section so user can claim them
        val newlyUnlocked = achievements.filter { it.isUnlocked && !it.isSeen }
        // Still locked
        val locked = achievements.filter { !it.isUnlocked }

        // Use a vertical list with two sections and card-based items for a cleaner UI
        androidx.compose.foundation.lazy.LazyColumn(modifier = modifier.padding(12.dp)) {
            item {
                // Header: stats
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProgressDisplayComponent(
                        innertext = String.format(Locale.getDefault(), "%.1f", user!!.totalHoursSpent)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                   Column(
                          horizontalAlignment = Alignment.CenterHorizontally,
                          verticalArrangement = Arrangement.spacedBy(8.dp)
                   ) {
                       Text(
                            text = "Longest Streak",
                            style = MaterialTheme.typography.bodyLarge.copy(color = colorScheme.onPrimary)
                       )
                          Text(
                             text = "${user!!.longestStreak} day${if (user!!.longestStreak != 1) "s" else ""}",
                             style = MaterialTheme.typography.headlineMedium.copy(color = colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                          )
                   }
                }
            }

            // Badges section (replaces previous "Unlocked" listing)
            item {
                Spacer(modifier = Modifier.size(24.dp))
                Text(
                    text = "Badges",
                    style = MaterialTheme.typography.titleMedium.copy(color = colorScheme.onPrimary),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Badges: only show claimed (isSeen) badges here
            if (claimedBadges.isNotEmpty()) {
                item {
                    BoxWithConstraints {
                        // For small widths show a horizontal scrollable row of badges
                        if (maxWidth < 700.dp) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(claimedBadges.size) { idx ->
                                    val a = claimedBadges[idx]

                                    // badge visuals don't need local progress here

                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = true,
                                        enter = fadeIn(animationSpec = tween(220, delayMillis = idx * 40)) + scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = idx * 40)),
                                        exit = fadeOut() + scaleOut()
                                    ) {
                                        // Claimed badges are clickable to view details
                                        Badge(achievement = a, size = 56.dp, onClick = { onOpenAchievement(a) })
                                    }
                                }
                            }
                        } else {
                            // On wide screens show a grid-like arrangement
                            LazyVerticalGrid(columns = GridCells.Adaptive(96.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(4.dp)) {
                                items(claimedBadges.size) { idx ->
                                    val a = claimedBadges[idx]

                                    // badge visuals don't need local progress here

                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = true,
                                        enter = fadeIn(animationSpec = tween(220, delayMillis = idx * 40)) + scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = idx * 40)),
                                        exit = fadeOut() + scaleOut()
                                    ) {
                                        Badge(achievement = a, size = 72.dp, onClick = { onOpenAchievement(a) })
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    Text(text = "No badges yet. Keep going to earn your first badge!", modifier = Modifier.padding(8.dp), color = colorScheme.onPrimary, style = MaterialTheme.typography.bodyLarge)
                }
            }

            // Newly unlocked (unclaimed) section
            if (newlyUnlocked.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "New", style = MaterialTheme.typography.titleMedium.copy(color = colorScheme.onPrimary), modifier = Modifier.padding(vertical = 8.dp))
                }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(newlyUnlocked.size) { idx ->
                            val a = newlyUnlocked[idx]
                            androidx.compose.animation.AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = tween(220, delayMillis = idx * 30)) + scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = idx * 30)),
                                exit = fadeOut() + scaleOut()
                            ) {
                                // Newly unlocked items are clickable so the user can view and claim them
                                RewardCard(a, width = 140.dp, progress = null, remainingText = null, onClick = { onOpenAchievement(a) })
                            }
                        }
                    }
                }
            }

            // Locked section (unchanged other than spacing)
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
                            // Map to a canonical achievement to compute thresholds/progress
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
                                RewardCard(a, width = 160.dp, progress = progress, remainingText = remainingText, enabled = false, onClick = { /* disabled */ })
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

    // dialog rendering handled by parent via onOpenAchievement
}

@Composable
private fun ProgressDisplayComponent(innertext: String) {
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
            text = "Total Hours",
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
fun Badge(
    achievement: Achievement = DEFAULT,
    size: Dp = 72.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        val badgeRes = achievement.badgeDrawableRes
        if (badgeRes != null) {
            // When a custom badge drawable is present, show only that graphic
            Image(
                painter = painterResource(id = badgeRes),
                contentDescription = achievement.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            // Fallback background + iconKey-based vector when no badge image is available
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (achievement.isUnlocked) colorScheme.primaryContainer
                        else colorScheme.surfaceVariant
                    )
            ) {
                val colorFilter = if (achievement.isUnlocked) null
                else ColorFilter.tint(colorScheme.onSurface.copy(alpha = 0.45f))
                Image(
                    imageVector = achievement.iconKey.rewardIcon,
                    contentDescription = "Badge: ${achievement.name}",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size((size.value * 0.5).dp),
                    colorFilter = colorFilter
                )
            }
        }
    }
}

// Insert RewardCard composable (small, self-contained)
@Composable
fun RewardCard(
    reward: Achievement = DEFAULT,
    width: Dp = 160.dp,
    iconSize: Dp = 56.dp,
    containerColor: Color = colorScheme.surfaceVariant,
    borderColor: Color? = null,
    progress: Float? = null,
    remainingText: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    val baseModifier = if (enabled) Modifier.width(width).clickable { onClick() } else Modifier.width(width)
    val cardModifier = if (borderColor != null) baseModifier.border(width = 2.dp, color = borderColor, shape = RoundedCornerShape(12.dp)) else baseModifier

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(12.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = containerColor),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp).testTag("reward_${reward.name}"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                val badgeRes = reward.badgeDrawableRes
                val colorFilter = if (reward.isUnlocked) null else ColorFilter.tint(colorScheme.onSurface.copy(alpha = 0.35f))
                if (badgeRes != null) {
                    Image(painter = painterResource(id = badgeRes), contentDescription = reward.name, modifier = Modifier.size(iconSize), contentScale = ContentScale.Fit, colorFilter = colorFilter)
                } else {
                    Image(imageVector = reward.iconKey.rewardIcon, contentDescription = reward.name, modifier = Modifier.size(iconSize), colorFilter = colorFilter)
                }
            }

            val titleColor = if (reward.isUnlocked) colorScheme.onPrimary else colorScheme.onSurface.copy(alpha = 0.6f)
            Text(text = reward.name, style = MaterialTheme.typography.titleMedium.copy(color = titleColor))

            if (!remainingText.isNullOrEmpty()) {
                val remainingColor = if (reward.isUnlocked) colorScheme.onPrimary.copy(alpha = 0.85f) else colorScheme.onSurface.copy(alpha = 0.6f)
                Text(text = remainingText, style = MaterialTheme.typography.bodyLarge.copy(color = remainingColor, fontSize = 13.sp))
            }

            if (!reward.isUnlocked) {
                val p = progress ?: 0f
                androidx.compose.material3.LinearProgressIndicator(progress = { p }, modifier = Modifier.height(6.dp))
                if (progress != null) {
                    Text(text = "${(p * 100).toInt()}%", style = TextStyle(fontSize = 12.sp), color = colorScheme.onPrimary.copy(alpha = 0.8f))
                }
            } else {
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
                        // Place the Lottie animation first so it appears behind the dialog content
                        LottieAnimation(composition = composition,
                            modifier = Modifier
                                .size(200.dp)
                                .fillMaxSize()
                                .align(Alignment.Center),
                            progress = { AnimationProgress })

                        Column(verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier =Modifier.align(Alignment.Center)){
                            Box(
                                Modifier
                                    .padding(2.dp)
                                    .width(81.dp)
                                    .height(81.dp)

                            ) {
                                val colorFilter = if (reward.isUnlocked) {
                                    null // No color filter for unlocked achievements
                                } else {
                                    ColorFilter.tint(Color.Gray) // Gray tint for locked achievements
                                }

                                // If the achievement has a drawable badge, show it as the dialog image background
                                val badgeRes = reward.badgeDrawableRes
                                if (badgeRes != null) {
                                    Image(
                                        painter = painterResource(id = badgeRes),
                                        contentDescription = dialogTitle,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(80.dp),
                                        contentScale = ContentScale.Fit,
                                        colorFilter = colorFilter
                                    )
                                } else {
                                    Image(
                                        imageVector = icon,
                                        contentDescription = dialogTitle,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(80.dp),
                                        colorFilter = colorFilter
                                    )
                                }

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
                    }

                }

                // Confirmation button
                val haptic = LocalHapticFeedback.current
                // Show Claim button only when the achievement is unlocked but not yet seen/claimed
                if (reward.isUnlocked && !reward.isSeen) {
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
                } else {
                    // Already claimed or locked — show a Close button
                    Button(
                        onClick = { onDismissRequest() },
                        modifier = Modifier.testTag("close_button")
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = colorScheme.surfaceVariant,
                            contentColor = colorScheme.onPrimary
                        )
                    ) {
                        Text(text = "Close", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold))
                    }
                }
             }
         }


    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsDialog(
    recent: List<Achievement>,
    onDismiss: () -> Unit,
    onOpen: (Achievement) -> Unit,
    onMarkAllRead: () -> Unit
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.background(color = colorScheme.surface).padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Notifications", style = MaterialTheme.typography.titleLarge.copy(color = colorScheme.onPrimary))
            Spacer(modifier = Modifier.height(8.dp))
            if (recent.isEmpty()) {
                Text(text = "No recent notifications", color = colorScheme.onPrimary.copy(alpha = 0.8f))
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(recent.size) { idx ->
                        val a = recent[idx]
                        Card(modifier = Modifier.clickable { onOpen(a) }) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(text = a.name, style = MaterialTheme.typography.bodyLarge)
                                if (a.description.isNotBlank()) Text(text = a.description, style = MaterialTheme.typography.bodySmall, color = colorScheme.onPrimary.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onMarkAllRead) { Text(text = "Mark all read") }
                Button(onClick = onDismiss) { Text(text = "Close") }
            }
        }
    }
}
