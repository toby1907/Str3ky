package com.example.explorr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.explorr.ui.ExplorrNavHost
import com.example.explorr.ui.MainViewModel
import com.example.explorr.ui.main.TopSection
import com.example.explorr.ui.theme.ExplorrTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var darkmode by remember {
                mutableStateOf(false)
            }
            val viewModel: MainViewModel by viewModels()
            val scope = rememberCoroutineScope()
            LaunchedEffect(key1 = true){
                viewModel.stateFlow.collectLatest { event ->
                    darkmode = when (event) {
                        is MainViewModel.UiState.DarkMode -> {

                            true
                        }
                        is MainViewModel.UiState.LightMode -> {
                            false
                        }
                    }
                }
            }
            ExplorrTheme(darkmode) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController= rememberNavController()

                 ExplorrNavHost(viewModel = viewModel)
                }
            }
        }
    }
}

