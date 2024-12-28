package com.example.str3ky.ui.nav

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.str3ky.ui.add_challenge_screen.AddChallengeScreen
import com.example.str3ky.ui.add_challenge_screen.AddScreenViewModel
import com.example.str3ky.ui.done.CompletedScreen
import com.example.str3ky.ui.main.HomeScreen
import com.example.str3ky.ui.progress.ProgressScreen
import com.example.str3ky.ui.session.SessionScreen
import com.example.str3ky.ui.session.SessionSettingsScreen


const val MY_URI = "https://www.incentivetimer.com/timer" // TODO: Update me
const val MY_ARG = "sessionCompleted"
@Composable
fun rememberAppNavState(
    navController: NavHostController = rememberNavController(),

    ) = remember(navController) {
    AppNavState(
        navController
    )
}
@Composable
fun MyAppNavHost(
    modifier: Modifier = Modifier,
    startDestination: String = "main",
    viewModel: AddScreenViewModel = hiltViewModel()

) {
    val snackbarHostState = remember { SnackbarHostState() }



    val appState = rememberAppNavState()

    NavHost(
        modifier = modifier,
        navController = appState.navController,
        startDestination = startDestination
    ) {

        composable(MAIN_SCREEN) {
            HomeScreen(
                onNavigateToAddVoice = {
                    appState.navigate(ADD_CHALLENGE_SCREEN)
                },

                navController = appState.navController,
                snackbarHostState = snackbarHostState
            )
        }
        composable(
            route = ADD_CHALLENGE_SCREEN +
                    "?noteId={noteId}&noteColor={noteColor}",
            arguments = listOf(
                navArgument(
                    name = "noteId"
                ) {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument(
                    name = "noteColor"
                ) {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument(
                    name = "note"
                ) {
                    type = NavType.StringType
                    defaultValue = ""
                },
            )
        ) { entry ->
            val color = entry.arguments?.getInt("noteColor") ?: -1

            AddChallengeScreen(
                onNavigateToAddVoice = {
                    appState.navigate(ADD_CHALLENGE_SCREEN)
                },
                navController = appState.navController,
                snackbarHostState = snackbarHostState,
                noteColor = color
            ) { route, popUp -> appState.navigateAndPopUp(route, popUp) }
        }
        composable(route = "$PROGRESS_SCREEN?goalId={goalId}",
            arguments = listOf(
                navArgument(
                    name = "goalId"
                ) {
                    type = NavType.IntType
                    defaultValue = -1
                },)
            ){
           ProgressScreen(nav = appState.navController)
        }
        composable(route="$SESSION_SETTINGS_SCREEN?goalId={goalId}&focusTime={focusTime}&progressDate={progressDate}",
            arguments = listOf(
                navArgument(
                    name = "goalId"
                ) {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument(
                    name = "focusTime"
                ) {
                    type = NavType.LongType
                    defaultValue = 0L
                },
                navArgument(
                    name = "progressDate"
                ) {
                    type = NavType.LongType
                    defaultValue = 0L
                },

                )
            ,
            ){
            SessionSettingsScreen(nav =appState.navController)
        }
        composable(route = "$SESSION_SCREEN?goalId={goalId}&totalSessions={totalSessions}&sessionDuration={sessionDuration}&progressDate={progressDate}",
            arguments = listOf(
                navArgument(
                    name = "goalId"
                ) {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument(
                    name = "totalSessions"
                ) {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument(
                    name = "sessionDuration"
                ) {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument(
                    name = "progressDate"
                ) {
                    type = NavType.LongType
                    defaultValue = 0L
                },
            )
            ){
            SessionScreen(
                nav = appState.navController,
                openAndPopUp = { route, popUp -> appState.navigateAndPopUp(route, popUp) }
                )
        }
        composable(route ="$DONE_SCREEN?goalId={goalId}&sessionDuration={sessionDuration}",
            arguments = listOf(
                navArgument(
                    name = "goalId"
                ) {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument(
                    name = MY_ARG
                ) {
                    type = NavType.LongType
                    defaultValue = 0L
                },
                ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = MY_URI
                }
            )

            ){entry ->
            val sessionDuration = entry.arguments?.getLong(MY_ARG) ?: 0L
            CompletedScreen(sessionDuration = sessionDuration)
        }
    }

}
