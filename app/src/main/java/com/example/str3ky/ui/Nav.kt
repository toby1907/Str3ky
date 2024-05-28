package com.example.str3ky.ui

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
import com.example.str3ky.ui.add_challenge_screen.AddChallengeScreen
import com.example.str3ky.ui.add_challenge_screen.AddScreenViewModel
import com.example.str3ky.ui.main.HomeScreen
import com.example.str3ky.ui.progress.ProgressScreen
import com.example.str3ky.ui.session.SessionScreen
import com.example.str3ky.ui.session.SessionSettingsScreen

@Composable
fun MyAppNavHost(
    modifier: Modifier = Modifier,
    startDestination: String = "main",
    viewModel: AddScreenViewModel = hiltViewModel()

) {
    val snackbarHostState = remember { SnackbarHostState() }

    val navController: NavHostController = rememberNavController()

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {

        composable("main") {
            HomeScreen(
                onNavigateToAddVoice = {
                    navController.navigate("add")
                },

                navController = navController,
                snackbarHostState = snackbarHostState
            )
        }
        composable(
            route = "add" +
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
                    navController.navigate("add")
                },
                navController = navController,
                snackbarHostState = snackbarHostState,
                noteColor = color,
            )
        }
        composable("progress"){
           ProgressScreen(nav = navController)
        }
        composable("session_settings"){
            SessionSettingsScreen(nav =navController)
        }
        composable("session"){
            SessionScreen(nav = navController)
        }
    }

}
