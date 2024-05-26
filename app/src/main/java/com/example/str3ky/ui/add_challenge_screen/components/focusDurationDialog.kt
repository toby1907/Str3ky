package com.example.str3ky.ui.add_challenge_screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.str3ky.R
import com.example.str3ky.ui.add_challenge_screen.AddChallengeEvent
import com.example.str3ky.ui.add_challenge_screen.AddScreenViewModel
import com.example.str3ky.ui.add_challenge_screen.millisecondsToMinutes
import com.example.str3ky.ui.add_challenge_screen.minutesToMilliseconds

@Composable
fun FocusDurationDialog(
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
                .size(width = 280.dp, height = 280.dp)

        ) {
            // Add your UI elements here

            Column(modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
                ){
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .width(138.dp)
                        .height(96.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(size = 4.dp)
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center

                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        BasicTextField(
                            modifier = Modifier.width(96.dp),
                            textStyle = TextStyle(
                                textAlign = TextAlign.Center,
                                fontSize = 24.sp,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight(500),
                                color = Color(0xFFFFFFFF),
                                letterSpacing = 0.1.sp,
                            ),
                            singleLine = true,
                            value = millisecondsToMinutes(viewModel.focusTime.value.focusTime.countdownTime).toString(),
                            onValueChange = {
                            },
                            readOnly = true

                        )
                        Text(
                            text = "mins",
                            style = TextStyle(
                                fontSize = 10.sp,
                                lineHeight = 24.sp,
                                fontWeight = FontWeight(400),
                                color = MaterialTheme.colorScheme.primary,

                                textAlign = TextAlign.Center,
                                letterSpacing = 0.1.sp,
                            )
                        )
                    }
                    DialogTrailingIcon(viewModel)
                }
            }

        }
    }
}
@Composable
fun DialogTrailingIcon(viewModel: AddScreenViewModel) {

    Row{

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(
                modifier = Modifier.background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(topEnd = 4.dp, topStart = 0.dp, bottomEnd = 4.dp, bottomStart = 0.dp)),
                onClick = {
                    if (millisecondsToMinutes(viewModel.focusTime.value.focusTime.countdownTime)<240) {
                         viewModel.timerIncrement()
                    }
                },
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface,
                )
            ) {

                Icon(
                    painter = painterResource(id = R.drawable.expand_less_icon),
                    contentDescription = "decrease"
                )
            }
            Spacer(modifier = Modifier.padding(2.dp))

            IconButton(

                modifier = Modifier.background(MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(topEnd = 4.dp, topStart = 0.dp, bottomEnd = 4.dp, bottomStart = 0.dp)),
                onClick = {
                    if (millisecondsToMinutes(viewModel.focusTime.value.focusTime.countdownTime)>10) {
                        viewModel.timerDecrement()
                    }
                          },
                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
            ) {

                Icon(
                    painter = painterResource(id = R.drawable.expand_more_icon),
                    contentDescription = "increase_time"
                )
            }
        }
    }
}