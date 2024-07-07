package com.example.basicstatecodelab.MainScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WellnessTaskItem (
    taskName: String,
    onClose: () -> Unit,
    onChecked:Boolean,
    onCheckedChanged:(Boolean) -> Unit,
    modifier: Modifier = Modifier){
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),text =taskName
        )
        Checkbox(
            checked =onChecked ,
            onCheckedChange =onCheckedChanged
        )
        IconButton(onClick = onClose) {
            Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")

        }
    }
}