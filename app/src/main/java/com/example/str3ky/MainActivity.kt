@file:Suppress("unused")

package com.example.str3ky

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.example.str3ky.repository.GoalRepositoryImpl
import com.example.str3ky.theme.Str3kyTheme
import com.example.str3ky.ui.nav.MyAppNavHost
import com.example.str3ky.ui.achievements.AchievementBanner
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.str3ky.ui.snackbar.SnackbarController
import com.example.str3ky.ui.snackbar.SnackbarEvent
import javax.inject.Inject
import androidx.navigation.compose.rememberNavController

private const val USER_PREFERENCES_NAME = "user_preferences"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = USER_PREFERENCES_NAME)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var goalRepository: GoalRepositoryImpl

     // Track three states: not checked, granted, denied
     private var permissionState by mutableStateOf<PermissionState>(PermissionState.NotChecked)

    sealed class PermissionState {
        object NotChecked : PermissionState()
        object Granted : PermissionState()
        object Denied : PermissionState()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shouldRequestNotifications = intent.getBooleanExtra(GoalRepositoryImpl.EXTRA_REQUEST_POST_NOTIFICATIONS, false)

        setContent {
            Str3kyTheme {
                val coroutineScope = rememberCoroutineScope()

                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                     contract = ActivityResultContracts.RequestPermission(),
                     onResult = { isGranted ->
                         permissionState = if (isGranted) PermissionState.Granted else PermissionState.Denied
                         if (!isGranted) {
                             coroutineScope.launch {
                                SnackbarController.sendEvent(
                                    SnackbarEvent(
                                        message = "Notification permission denied. Some features may not work.",
                                    )
                                )
                             }
                         }
                     }
                )

                LaunchedEffect(shouldRequestNotifications) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val permission = android.Manifest.permission.POST_NOTIFICATIONS
                        val currentStatus = ContextCompat.checkSelfPermission(applicationContext, permission)
                        permissionState = if (currentStatus == PackageManager.PERMISSION_GRANTED) {
                            PermissionState.Granted
                        } else {
                            PermissionState.Denied
                        }
                        if (shouldRequestNotifications && permissionState == PermissionState.Denied) {
                            notificationPermissionLauncher.launch(permission)
                        }
                    } else {
                        permissionState = PermissionState.Granted
                    }
                }

                when (permissionState) {
                    PermissionState.NotChecked -> {
                        // Show loading or nothing while checking
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    PermissionState.Granted -> {
                        // Show main app
                        val navController = rememberNavController()
                        Scaffold() {
                            // Show achievement banner at the top of the scaffold content
                            Column(modifier = Modifier.padding(it)) {
                                AchievementBanner()
                                MyAppNavHost(navController = navController, modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    }
                    PermissionState.Denied -> {
                        // Show permission request UI
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