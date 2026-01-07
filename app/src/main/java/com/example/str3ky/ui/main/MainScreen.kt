package com.example.str3ky.ui.main

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.core.view.WindowCompat
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import com.example.str3ky.R
import com.example.str3ky.data.Goal
import com.example.str3ky.data.Occurrence
import com.example.str3ky.millisecondsToMinutes
import com.example.str3ky.ui.main.components.OrderSection
import com.example.str3ky.ui.nav.ACHIEVEMENTS_SCREEN
import com.example.str3ky.ui.nav.PROGRESS_SCREEN
import com.example.str3ky.use_case.GoalsEvent
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.str3ky.ui.achievements.AchievementViewModel
import com.example.str3ky.ui.achievements.AchievementBanner
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddVoice: () -> Unit,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    viewModel:MainScreenViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    // Read the theme color in composable scope and reuse in SideEffect
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val backgroundColor = MaterialTheme.colorScheme.background
    val opaque = primaryContainerColor.copy(alpha = 1f)
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as? Activity)?.window?.apply {
                // Draw behind system bars so the TopAppBar background can cover the status bar area
                try { WindowCompat.setDecorFitsSystemWindows(this, false) } catch (_: Throwable) {}
                statusBarColor = backgroundColor.toArgb()
                // Match the window background to the app bar color to avoid any visible seam
                try { setBackgroundDrawable(ColorDrawable(opaque.toArgb())) } catch (_: Throwable) {}
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try { setStatusBarContrastEnforced(false) } catch (_: Throwable) {}
                }
            }
        }
    }
    val achievementVM: AchievementViewModel = hiltViewModel()
    val unseen by achievementVM.unseenCount.collectAsState()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(0.dp),
                title = {
                    Text(
                        text = "Challenges",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                actions = {
                    // Trophy icon with badge
                    IconButton(onClick = { navController.navigate(ACHIEVEMENTS_SCREEN); achievementVM.markAllSeen() }) {
                        if (unseen > 0) {
                            BadgedBox(badge = { Badge { Text(text = unseen.toString()) } }) {
                                Icon(
                                    imageVector = Icons.Filled.EmojiEvents,
                                    contentDescription = "Achievements"
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Filled.EmojiEvents,
                                contentDescription = "Achievements"
                            )
                        }
                    }

                    // Add challenge
                    IconButton(onClick = { onNavigateToAddVoice() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.add_icon),
                            contentDescription = "Add challenge"
                        )
                    }

                    // Sort
                    IconButton(onClick = { viewModel.onEvent(GoalsEvent.ToggleOrderSection) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.sorting_icon),
                            contentDescription = "Sort challenges"
                        )
                    }

                    // Overflow menu
                    var menuExpanded by remember { mutableStateOf(false) }
                    val menuScrollState = rememberScrollState()

                    Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                painter = painterResource(id = R.drawable.more_vert_24px),
                                contentDescription = "More options"
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            scrollState = menuScrollState,
                        ) {
                            DropdownMenuItem(
                                text = { Text("Achievements") },
                                onClick = {
                                    menuExpanded = false
                                    navController.navigate(ACHIEVEMENTS_SCREEN)
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.arrow_up_circle_icon),
                                        contentDescription = ""
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = {
                                    menuExpanded = false
                                    // TODO: Navigate to settings
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.Edit,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        content = { it ->

          Column(
               modifier = Modifier
                   .fillMaxWidth()
                   .padding(it)
           ) {
                // Global achievement banner (shows recently unlocked trophies)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.TopCenter)
                        .padding(horizontal = 16.dp)
                        .clickable {
                            navController.navigate(ACHIEVEMENTS_SCREEN)
                            achievementVM.markAllSeen()
                        }
                        .semantics { contentDescription = "Achievement notification" }
                ) {
                    AchievementBanner(viewModel = achievementVM)
                }

                 AnimatedVisibility(
                     visible = state.isOrderSectionVisible,
                     enter = fadeIn() + slideInVertically(),
                     exit = fadeOut() + slideOutVertically()
                 ) {
                    OrderSection(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        noteOrder = state.goalOrder,
                        onOrderChange = {
                            viewModel.onEvent(GoalsEvent.Order(it))
                        }
                    )
                }
                LazyColumn(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    content = {
                        items(state.goals) { goal ->


                                ChallengListItem(
                                    goal,
                                    navController,
                                    onDeleteClick = {
                                        viewModel.onEvent(GoalsEvent.DeleteGoal(goal))
                                        scope.launch {
                                            viewModel.showDeleteSnackbar()
                                        }
                                    }
                                )

                        }
                    })
            }
        }
    )


}


@Composable
fun ChallengListItem(item: Goal, navController: NavHostController, onDeleteClick: () -> Unit) {
    val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
 Column (verticalArrangement = Arrangement.spacedBy(4.dp),
     horizontalAlignment = Alignment.CenterHorizontally)   {
        ListItem(
            modifier = Modifier.clickable {

                navController.navigate(PROGRESS_SCREEN + "?goalId=${item.id}")


            },
            headlineContent = {
                Text(
                    text = item.title,
                    color = MaterialTheme.colorScheme.primary
                )

            },
            leadingContent = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        )
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(
                            width = 3.dp,
                            color = Color.Transparent,
                            shape = CircleShape
                        )

                ) {
                    Text(
                        text = if (item.title.isNotEmpty()) "${item.title[0].uppercaseChar()}" else ""
                    )
                }
            },
            supportingContent = {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Icon(
                                painter = painterResource(id = R.drawable.arrow_up_circle_icon),
                                contentDescription = ""
                            )
                            Text(
                                text = millisecondsToMinutes(item.focusSet).toString() + " mins",
                                style = TextStyle(fontSize = 8.sp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.calendar_icon),
                                contentDescription = ""
                            )
                            val text = when (item.occurrence.dayOption.name) {
                                Occurrence.DAILY.name -> "Daily"
                                Occurrence.DAILY_WITHOUT_WEEKEND.name -> ""
                                Occurrence.CUSTOM.name -> "${item.occurrence.selectedDays.size} days weekly"
                                else -> ""
                            }
                            Text(
                                text = text,
                                style = TextStyle(fontSize = 8.sp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.alert_icon),
                                contentDescription = ""
                            )

                            Text(
                                text = if (item.alarmTime != null) {
                                    val selectedDate = Date(item.alarmTime)
                                    formatter.format(selectedDate)
                                } else "Not Set",
                                style = TextStyle(fontSize = 8.sp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    // Show description (if available) under the supporting row
                    val desc = item.description
                    if (desc.isNotBlank()) {
                        Text(
                            text = desc,
                            style = TextStyle(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            ),
            trailingContent = {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = ""
                    )
                }
            }
        )
     HorizontalDivider(color = MaterialTheme.colorScheme.onSurface, thickness = 0.25.dp)
    }
}
