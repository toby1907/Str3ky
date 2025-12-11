@file:Suppress("unused")

package com.example.str3ky.ui.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AchievementBanner(viewModel: AchievementViewModel = hiltViewModel()) {
    val unlocked by viewModel.recentUnlocked.collectAsState()
    if (unlocked.isNotEmpty()) {
        Column(modifier = Modifier
            .background(MaterialTheme.colorScheme.primary)
            .padding(8.dp)) {
            unlocked.forEach { ach ->
                Row(modifier = Modifier.padding(4.dp)) {
                    Text(text = "🏆 ${ach.name}", style = MaterialTheme.typography.bodyLarge)
                    // Add a small dismiss button
                    Button(onClick = { viewModel.clear() }, modifier = Modifier.padding(start = 8.dp)) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}
