package com.example.str3ky.ui.add_challenge_screen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.str3ky.data.Occurrence
import com.example.str3ky.data.OccurrenceSelection
import com.example.str3ky.ui.add_challenge_screen.AddChallengeEvent
import com.example.str3ky.ui.add_challenge_screen.AddScreenViewModel

@Composable
fun FrequencyDialog(
    viewModel: AddScreenViewModel,
    onCancel: () -> Unit,
    onOk: () -> Unit
) {

    val (selectedOption, onOptionSelected) = remember { mutableStateOf("Daily") }

    // A dialog with a custom content
    Dialog(onDismissRequest = onCancel) {
        // Use a card as the content of the dialog
        Card(
            modifier = Modifier
                .padding(16.dp)
                .size(width = 240.dp, height = 258.dp)

        ) {
            // Add your UI elements here
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                /* Occurrence.entries.forEach { occurrence ->
                     Text(text = occurrence.name,
                         modifier = Modifier.clickable {
                             viewModel.onEvent(AddChallengeEvent.Frequency(occurrence))
                             onOk()
                         }
                         )

                 }*/

// Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior
                Column(Modifier.selectableGroup()) {

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = ("DAILY" == viewModel.frequency.value.frequency.dayOption.name),
                                onClick = {
                                    onOptionSelected("DAILY")
                                    /*    val dailyPreferences = UserPreferences(DayOption.DAILY, emptySet())
                                        val dailyWithoutWeekendPreferences = UserPreferences(DayOption.DAILY_WITHOUT_WEEKEND, emptySet())
                                        val customPreferences = UserPreferences(DayOption.CUSTOM, setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))*/
                                    val occurrence =
                                        OccurrenceSelection(Occurrence.DAILY, emptySet())
                                    viewModel.onEvent(AddChallengeEvent.Frequency(occurrence))
                                },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = ("DAILY" == viewModel.frequency.value.frequency.dayOption.name),
                            onClick = null // null recommended for accessibility with screenreaders
                        )
                        Text(
                            text = "Daily",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = ("CUSTOM" == viewModel.frequency.value.frequency.dayOption.name),
                                onClick = {
                                   /* val customSelectedDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)

                                    val occurrence =
                                        OccurrenceSelection(Occurrence.CUSTOM, customSelectedDays)
                                    viewModel.onEvent(AddChallengeEvent.Frequency(occurrence))*/
                                    onOptionSelected("CUSTOM")
                                          },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = ("CUSTOM" == viewModel.frequency.value.frequency.dayOption.name),
                            onClick = null // null recommended for accessibility with screenreaders
                        )
                        Text(
                            text = "Custom",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                    if (selectedOption=="CUSTOM"||viewModel.frequency.value.frequency.dayOption.name == "CUSTOM") {
                        DayOfWeekSelectionScreen(
                            viewModel =viewModel
                        )
                    }

                }

            }
        }
    }
}

@Composable
fun DayOfWeekSelectionRow(
    dayOfWeek: String,
    checkedState: Boolean,
    onStateChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .toggleable(
                value = checkedState,
                onValueChange = { onStateChange(!checkedState) },
                role = Role.Checkbox
            )
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checkedState,
            onCheckedChange = null // null recommended for accessibility with screenreaders
        )
        Text(
            text = dayOfWeek,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun DayOfWeekSelectionScreen(viewModel: AddScreenViewModel) {
    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val selectedDays = viewModel.selectedDays.value

/*if (viewModel.frequency.value.frequency.selectedDays.isNotEmpty()){
    selectedDays.addAll(viewModel.frequency.value.frequency.selectedDays.map { it.name })
}*/


    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(daysOfWeek) { day ->
            DayOfWeekSelectionRow(
                dayOfWeek = day,
                checkedState = selectedDays.contains(day),
                onStateChange = { isChecked ->
                    if (isChecked) {
                        viewModel.addDay(day)

                    } else {
                        viewModel.removeDay(day)
                    }
                }
            )
        }
    }
}


