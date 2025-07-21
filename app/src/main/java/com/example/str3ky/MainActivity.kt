package com.example.str3ky

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.compose.rememberNavController
import com.example.str3ky.repository.GoalRepositoryImpl
import com.example.str3ky.theme.Str3kyTheme
import com.example.str3ky.ui.MainViewModel
import com.example.str3ky.ui.nav.MyAppNavHost
import com.example.str3ky.ui.snackbar.ObserveAsEvents
import com.example.str3ky.ui.snackbar.SnackbarController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val USER_PREFERENCES_NAME = "user_preferences"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = USER_PREFERENCES_NAME)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var goalRepository: GoalRepositoryImpl

    private val mainViewModel: MainViewModel by viewModels()
    // private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001 // Not strictly needed with ActivityResultLauncher

    // State to track permission status
    private var hasNotificationPermission by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Str3kyTheme {
                val coroutineScope = rememberCoroutineScope()
                val snackbarHostState = remember { SnackbarHostState() }

                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted ->
                        hasNotificationPermission = isGranted
                        if (!isGranted) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Notification permission denied. Some features may not work.",
                                    duration = SnackbarDuration.Long
                                )
                            }
                        }
                    }
                )

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val permission = android.Manifest.permission.POST_NOTIFICATIONS
                        val currentStatus = ContextCompat.checkSelfPermission(applicationContext, permission)
                        if (currentStatus != PackageManager.PERMISSION_GRANTED) {
                            notificationPermissionLauncher.launch(permission)
                        } else {
                            hasNotificationPermission = true
                        }
                    } else {
                        hasNotificationPermission = true // Permission not needed below API 33
                    }
                }

                if (hasNotificationPermission) {
                    // Only render app UI if permission is granted or not required
                    val navController = rememberNavController()
                    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
                        MyAppNavHost(navController = navController, modifier = Modifier.padding(it))
                    }
                } else {
                    // Optional: show fallback UI when permission not granted
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Please grant notification permission to proceed.")
                        Button(onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }) {
                            Text("Grant Permission")
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun PermissionDialog(
    title: String,
    description: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title)
        },
        text = {
            Text(text = description)
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(text = stringResource(id = R.string.ok))
            }
        }
    )
}