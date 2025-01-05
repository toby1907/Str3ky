package com.example.str3ky

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.str3ky.repository.GoalRepositoryImpl
import com.example.str3ky.theme.Str3kyTheme
import com.example.str3ky.ui.MainViewModel
import com.example.str3ky.ui.nav.MyAppNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val USER_PREFERENCES_NAME = "user_preferences"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = USER_PREFERENCES_NAME)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var goalRepository: GoalRepositoryImpl

    private val mainViewModel: MainViewModel by viewModels()
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Request notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_REQUEST_CODE)
            }


            // ... rest of your activity code ...

            setContent {
                Str3kyTheme {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {

                        MyAppNavHost()

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