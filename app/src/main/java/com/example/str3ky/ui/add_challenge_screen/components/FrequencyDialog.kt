package com.example.str3ky.ui.add_challenge_screen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.str3ky.data.Goal
import com.example.str3ky.data.Occurrence
import com.example.str3ky.ui.add_challenge_screen.AddChallengeEvent
import com.example.str3ky.ui.add_challenge_screen.AddScreenViewModel

@Composable
fun FrequencyDialog(
    viewModel: AddScreenViewModel,
    onCancel: () -> Unit,
    onOk: () -> Unit
) {


    // A dialog with a custom content
    Dialog(onDismissRequest = onCancel) {
        // Use a card as the content of the dialog
        Card(
            modifier = Modifier
                .padding(16.dp)
                .size(width = 280.dp, height = 434.dp)

        ) {
            // Add your UI elements here
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Occurrence.entries.forEach { occurrence ->
                    Text(text = occurrence.name,
                        modifier = Modifier.clickable {
                            viewModel.onEvent(AddChallengeEvent.Frequency(occurrence))
                            onOk()
                        }
                        )

                }

            }
        }
    }
}