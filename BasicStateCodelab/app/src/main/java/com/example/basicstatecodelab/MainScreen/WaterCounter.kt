package com.example.basicstatecodelab.MainScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun StatefulCounter(modifier: Modifier = Modifier) {
    var count by rememberSaveable {
        mutableStateOf(0)
    }
    var showTask by remember {
        mutableStateOf(true)
    }
    StatelessCounter(
        count = count,
        showTask = showTask,
        onIncrement = {
            count++
            showTask = true
        },
        onshowTask = { showTask = false }
    )
}

@Composable
fun StatelessCounter(
    count: Int, showTask: Boolean,
    onIncrement: () -> Unit, onshowTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = Modifier.padding(16.dp)) {
        if (count > 0) {

            if (showTask) {
                var onChecked by rememberSaveable {
                    mutableStateOf(false)
                }
                WellnessTaskItem(
                    taskName = "Have you taken your 15 minute walk today?",
                    onClose = onshowTask,
                    onChecked = onChecked,
                    onCheckedChanged = {
                        onChecked = it
                    }
                )
            }

            Text(
                text = "You've had $count glasses."
            )
        }


        Button(
            onClick = onIncrement,
            Modifier.padding(top = 8.dp),
            enabled = count < 10
        ) {
            Text(text = "Add one")
        }


    }
}

@Composable
fun WellnessTaskItem(
    taskName: String,
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCloseTask: () -> Unit
) {
    var checkedState by rememberSaveable { mutableStateOf(false) }
    WellnessTaskItem(
        taskName = taskName,
        onClose = onCloseTask,
        onChecked = checkedState,
        onCheckedChanged = { checkedState = it }
    )
}

@Composable
fun WellnessTasksList(
    list: List<WellnessTask>,
    modifier: Modifier = Modifier,
    onCheckedTask: (WellnessTask, Boolean) -> Unit,
    onCloseTask: (WellnessTask) -> Unit
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(
            items = list,
            key = { task -> task.id }
        ) { task ->
            WellnessTaskItem(
                taskName = task.label,
                checked = task.checked,
                onCloseTask = {onCloseTask(task) })

        }
    }
}

@Composable
@Preview
fun WaterCounterPreview() {
    StatefulCounter()

}