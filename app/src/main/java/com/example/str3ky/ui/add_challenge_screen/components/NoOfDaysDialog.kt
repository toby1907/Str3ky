package com.example.str3ky.ui.add_challenge_screen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Card
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.str3ky.data.Goal
import com.example.str3ky.ui.add_challenge_screen.AddScreenViewModel

@Composable
fun NoOfDaysDialog(
    viewModel: AddScreenViewModel,
    onCancel: () -> Unit,
    onOk: () -> Unit
) {
    val (selectedOption, onOptionSelected) = remember { mutableIntStateOf(30) }

    // A dialog with a custom content
    Dialog(onDismissRequest = onCancel) {
        // Use a card as the content of the dialog
        Card(
            modifier = Modifier
                .padding(16.dp)
                .size(width = 280.dp, height = 400.dp)

        ) {
            // Add your UI elements here
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = "Select for how long the challenge will be for:")

                    Goal.no_of_days.forEach { no ->
                        Column(Modifier.selectableGroup()) {

                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .selectable(
                                        selected = (no == viewModel.noOfDays.value.noOfDays),
                                        onClick = {
                                            onOptionSelected(no)
                                            viewModel.onNoOfDaysSelected(no)
                                        },
                                        role = Role.RadioButton
                                    ),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically

                            )
                            {
                                RadioButton(
                                    selected = (no == viewModel.noOfDays.value.noOfDays),
                                    onClick = null // null recommended for accessibility with screenreaders
                                )
                                Text(
                                    text = if (no == 1) "$no day" else "$no days",
                                    modifier = Modifier.clickable {
                                        viewModel.onNoOfDaysSelected(no)

                                    }
                                )
                            }

                        }

                    }
                 Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End)   {
                        TextButton(onClick = { onOk() }) {
                            Text(text = "Ok")
                        }
                    }


            }
        }
    }
}