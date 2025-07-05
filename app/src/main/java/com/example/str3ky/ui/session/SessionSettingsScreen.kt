package com.example.str3ky.ui.session

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.str3ky.R
import com.example.str3ky.theme.Str3kyTheme
import com.example.str3ky.ui.nav.SESSION_SCREEN

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionSettingsScreen(nav: NavHostController) {
    val viewModel:FocusSessionViewModel = hiltViewModel()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Focus Session Settings") },
                actions = {
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack()  }) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back_icon),
                            contentDescription = ""
                        )
                    }
                }

            )
        },
        content = { it ->

            Column(
                modifier = Modifier
                    .padding(
                        it
                    )
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

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
                        BasicTextField(modifier = Modifier.width(96.dp),
                            textStyle = TextStyle(
                                textAlign = TextAlign.Center,
                                fontSize = 24.sp,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight(500),
                                color = Color(0xFFFFFFFF),
                                letterSpacing = 0.1.sp,
                            ),
                            singleLine = true,
                            value = viewModel.timerValue.value.toString(),
                            onValueChange = {

                            }
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
                    TrailingIcon(viewModel)
                }
                Spacer(modifier = Modifier.padding(24.dp))
                val displayText = when(viewModel.numBreaks.intValue){
                     0 -> {
                         "You'll have no break"
                     }
                    1 -> "You will have only ${viewModel.numBreaks.intValue} break."
                    in 2..8 ->  "You will have ${viewModel.numBreaks.intValue} no. of breaks."

                    else -> {   "You'll have no break"   }
                }
                Text(
                    text = if (viewModel.skipBreak.value) "You'll have no break" else displayText,
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight(500),
                        color = Color(0xFFFFFFFF),

                        textAlign = TextAlign.Center,
                        letterSpacing = 0.1.sp,
                    )
                )
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = viewModel.skipBreak.value,
                        onCheckedChange = { viewModel.toggleSkipBreak()},
                        enabled = viewModel.timerValue.value >= 30,
                    )

                    Text(
                        text = "Skip break.",
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight(500),
                            color = Color(0xFFFFFFFF),

                            textAlign = TextAlign.Center,
                            letterSpacing = 0.1.sp,
                        )
                    )
                }
Spacer(modifier = Modifier.padding(16.dp))
             Button(modifier = Modifier
                 .fillMaxWidth()
                 .padding(horizontal = 48.dp)
                 .defaultMinSize(minHeight =48.dp)
                 .background(
                     color = MaterialTheme.colorScheme.primary,
                     shape = RoundedCornerShape(size = 10.dp)
                 ),
                 onClick = {
                     viewModel.sessionDurationCalculation()
                     nav.navigate(SESSION_SCREEN+"?goalId=${viewModel.goalId.value}&totalSessions=${viewModel.numSessions.intValue}&sessionDuration=${viewModel.sessionDuration.value}&progressDate=${viewModel.progressDate.value}")

                 }) {
                 Row(
                     verticalAlignment = Alignment.CenterVertically,
                     horizontalArrangement = Arrangement.spacedBy(8.dp,Alignment.CenterHorizontally)
                 ) {
                     Icon(painter = painterResource(id = R.drawable.add_icon),
                         contentDescription ="add button" ,
                         modifier = Modifier.size(32.dp),
                         tint = MaterialTheme.colorScheme.onPrimary,)
                     Text(text = "Start focus session",style = TextStyle(
                         fontSize = 14.sp,
                         lineHeight = 20.sp,
                         fontWeight = FontWeight(500),
                         color = MaterialTheme.colorScheme.onPrimary,
                         textAlign = TextAlign.Center,
                         letterSpacing = 0.1.sp,
                     ))
                 }

             }
            }
        }
    )
}

@Composable
fun TrailingIcon(viewModel: FocusSessionViewModel
) {

    Row{

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(
                modifier = Modifier.background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(topEnd = 4.dp, topStart = 0.dp, bottomEnd = 4.dp, bottomStart = 0.dp)),
                onClick = { viewModel.increaseTimer()},
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface,
                )
            ) {
                
                Icon(
                    painter = painterResource(id = R.drawable.expand_less_icon),
                    contentDescription = "increase_time"
                )
            }
            Spacer(modifier = Modifier.padding(2.dp))

            IconButton(

                modifier = Modifier.background(MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(topEnd = 4.dp, topStart = 0.dp, bottomEnd = 4.dp, bottomStart = 0.dp)),
                onClick = { viewModel.decreaseTimer() },
                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
            ) {

                Icon(
                    painter = painterResource(id = R.drawable.expand_more_icon),
                    contentDescription = "decrease"
                )
            }
        }
    }
}
