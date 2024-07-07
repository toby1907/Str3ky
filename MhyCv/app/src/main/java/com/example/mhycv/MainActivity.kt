package com.example.mhycv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mhycv.ui.theme.MhyCvTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {


            MhyCvTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                  NavGraph()
                }
            }
        }
    }
}


@Composable
fun NavGraph(
) {
    val navController = rememberNavController()
    val viewModel= CvViewModel()
    NavHost(navController = navController, startDestination = "cvScreen") {
        composable("cvScreen") {
            Cvscreen(viewModel=viewModel, function ={navController.navigate("cvEditScreen")} )
        }
        composable("cvEditScreen") {
            CvEditScreen(navController = navController,viewModel=viewModel)
        }
        /*...*/
    }
}
