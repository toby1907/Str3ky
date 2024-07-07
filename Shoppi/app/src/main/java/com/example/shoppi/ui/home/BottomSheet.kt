package com.example.shoppi.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.RangeSliderState
import androidx.compose.material3.SheetState
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit
) {

    when {

        sheetState.isVisible -> {

            ModalBottomSheet(
                modifier = Modifier.fillMaxSize(),
                onDismissRequest = onDismiss,
                sheetState = sheetState
            ) {

                BottomSheet(sheetState)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(sheetState: SheetState) {
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = "Filters",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Price Range",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        AmountSlider()

        Text(
            text = "Type",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow() {
            items(5) { index ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Gray,
                    modifier = Modifier
                        .size(73.dp)
                        .padding(end = 8.dp)
                ) {
                    // Icon for the item type
                }
            }
        }
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "Categories",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow() {
            items(5) { index ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Gray,
                    modifier = Modifier
                        .size(73.dp)
                        .padding(end = 8.dp)
                ) {
                    // Icon for the item type
                }
            }
        }
        Spacer(modifier = Modifier.size(24.dp))
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Button(colors = ButtonColors(containerColor =  Color(0xFF69BCFC),
                contentColor = Color.White,
                disabledContainerColor = Color.Gray,
                disabledContentColor = Color.Black),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.size(width = 364.dp, height = 66.dp), onClick = {
                coroutineScope.launch {
                    sheetState.hide()
                }
            }) {
                Text(text = "Apply")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmountSlider() {

    val rangeSliderState = remember {
        RangeSliderState(
            0f,
            100f,
            valueRange = 0f..100f,
            onValueChangeFinished = {
                // launch some business logic update with the state you hold
                // viewModel.updateSelectedSliderValue(sliderPosition)
            }
        )
    }
    val startInteractionSource = remember { MutableInteractionSource() }
    val endInteractionSource = remember { MutableInteractionSource() }
    val startThumbAndTrackColors = SliderDefaults.colors(
        thumbColor = Color.Blue,
        activeTrackColor = Color.Red
    )
    val endThumbColors = SliderDefaults.colors(thumbColor = Color.Green)
    Column {
        Text(text = (rangeSliderState.activeRangeStart..rangeSliderState.activeRangeEnd).toString())
        RangeSlider(
            state = rangeSliderState,
            modifier = Modifier.semantics { contentDescription = "Localized Description" },
            startInteractionSource = startInteractionSource,
            endInteractionSource = endInteractionSource,
            startThumb = {
                SliderDefaults.Thumb(
                    interactionSource = startInteractionSource,
                    colors = startThumbAndTrackColors
                )
            },
            endThumb = {
                SliderDefaults.Thumb(
                    interactionSource = endInteractionSource,
                    colors = endThumbColors
                )
            },
            track = { rangeSliderState ->
                SliderDefaults.Track(
                    colors = startThumbAndTrackColors,
                    rangeSliderState = rangeSliderState
                )
            }
        )
    }
}