package com.example.str3ky.ui.notifications

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun NotificationHelpScreen(nav: NavHostController) {
    val ctx = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Notifications are required to receive timer and achievement updates.\n\nTo enable: Open App settings → Notifications → Allow",
            style = MaterialTheme.typography.bodyLarge
        )

        Button(onClick = {
            // Open app notification settings (use API-safe fallback)
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent().apply {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName)
                }
            } else {
                Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.parse("package:${ctx.packageName}")
                }
            }
            ctx.startActivity(intent)
        }, modifier = Modifier.padding(top = 16.dp)) {
            Text(text = "Open notification settings")
        }

        Button(onClick = { nav.popBackStack() }, modifier = Modifier.padding(top = 8.dp)) {
            Text(text = "Back")
        }
    }
}
















