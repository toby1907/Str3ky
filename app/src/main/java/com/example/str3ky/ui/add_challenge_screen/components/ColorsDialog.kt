package com.example.str3ky.ui.add_challenge_screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.str3ky.data.Goal
import com.example.str3ky.ui.add_challenge_screen.AddScreenViewModel

val colorPalette = listOf(
    listOf(
        Color(0xFFE0E0E0),
        Color(0xFF2196F3),
        Color(0xFF8BC34A),
        Color(0xFF9C27B0)
    ),
    listOf(
        Color(0xFF9E9E9E),
        Color(0xFF1976D2),
        Color(0xFF689F38),
        Color(0xFF8F24A2)
    ),
    listOf(
        Color(0xFFF5F5F5),
        Color(0xFF64B5F6),
        Color(0xFF4CAF50),
        Color(0xFFE91E63)
    ),
    listOf(
        Color(0xFF455A64),
        Color(0xFF0277BD),
        Color(0xFF2E7D32),
        Color(0xFF9A66A2)
    ),
    listOf(
        Color(0xFF212121),
        Color(0xFF0D47A1),
        Color(0xFF1B5E20),
        Color(0xFF8B0A1A)
    )
)
@Composable
fun ColorsDialog(
    viewModel:AddScreenViewModel,
    onColorClicked: (Int) -> Unit, // The function to call when a tag is checked or unchecked
    onCancel: () -> Unit, // The function to call when the cancel button is clicked
    onOk: () -> Unit // The function to call when the ok button is clicked
) {
    val colors = Goal.goalColors
    // A state variable to store the text entered in the edit text field
    val colorChoice = remember { mutableIntStateOf(0) }


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
               ColorGrid(colorPalette = colorPalette, onClick =onColorClicked, onOk = onOk )

            }
        }
    }
}

@Composable
fun ColorGrid(colorPalette: List<List<Color>>, onClick: (Int) -> Unit,onOk: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.TopStart)
        ) {
            colorPalette.forEach { colorRow ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp)
                ) {
                    colorRow.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(color, CircleShape)
                                .clickable {
                                    onClick(color.toArgb())
                                    onOk()
                                }
                        )
                    }
                }
            }
        }
    }
}


