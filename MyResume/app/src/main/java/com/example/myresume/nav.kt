package com.example.voicejournal.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myresume.ui.SplashScreen
import com.example.myresume.ui.profile.Profile
import com.example.myresume.ui.profile.ProfileViewModel

@Composable
fun MyAppNavHost(
    modifier: Modifier = Modifier,
    startDestination: String = "Splash",
    viewModel: ProfileViewModel

){
    val navController: NavHostController = rememberNavController()
    NavHost(
        modifier = modifier,
        navController = navController ,
        startDestination = startDestination

    ){
        composable("Splash"){
            SplashScreen() {
                navController.navigate("Profile")
            }
        }
        composable("Profile"){
            Profile(viewModel)
        }

    }

}