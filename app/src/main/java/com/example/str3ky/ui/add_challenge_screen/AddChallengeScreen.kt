package com.example.str3ky.ui.add_challenge_screen

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.str3ky.R
import com.example.str3ky.millisecondsToMinutes
import com.example.str3ky.ui.add_challenge_screen.components.ColorsDialog
import com.example.str3ky.ui.add_challenge_screen.components.FocusDurationDialog
import com.example.str3ky.ui.add_challenge_screen.components.FrequencyDialog
import com.example.str3ky.ui.add_challenge_screen.components.NoOfDaysDialog
import com.example.str3ky.ui.nav.ADD_CHALLENGE_SCREEN
import com.example.str3ky.ui.nav.MAIN_SCREEN
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChallengeScreen(
    viewModel: AddScreenViewModel = hiltViewModel(),
    onNavigateToAddVoice: () -> Unit,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    noteColor: Int,
    openAndPopUp: (String, String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val showDialog = remember { mutableStateOf(false) }
    val showNoOfDaysDialog = remember { mutableStateOf(false) }
    val showFrequencyDialog = remember {
        mutableStateOf(false)
    }
    val showfocusDurationDialog = remember {

        mutableStateOf(false)
    }

    // A function to show the dialog
    fun showDialog() {
        showDialog.value = true
    }

    // A function to hide the dialog
    fun hideDialog() {
        showDialog.value = false
    }

    val context = LocalContext.current
    val mCalendar = Calendar.getInstance()
    val mHour = mCalendar[Calendar.HOUR_OF_DAY]
    val mMinute = mCalendar[Calendar.MINUTE]
    val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val mTimePickerDialog = TimePickerDialog(
        context,
        {_, mHour : Int, mMinute: Int ->
            val selectedTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, mHour)
                set(Calendar.MINUTE, mMinute)
                // Set the AM/PM marker based on the hour
                if (mHour >= 12) {
                    // PM
                    set(Calendar.AM_PM, Calendar.PM)
                } else {
                    // AM
                    set(Calendar.AM_PM, Calendar.AM)
                }
            }.timeInMillis
            viewModel.onEvent(AddChallengeEvent.AlarmTime(selectedTime))
        }, mHour, mMinute, false
    )

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                UiEvent.SaveNote -> {
                    openAndPopUp(MAIN_SCREEN,ADD_CHALLENGE_SCREEN,)
                }
                is UiEvent.ShowSnackbar -> scope.launch {
                    viewModel.showSnackbar()
                }
            }

        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Add Task",
                    color = MaterialTheme.colorScheme.onPrimary,) },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    {
                        IconButton(
                            onClick = {
                                viewModel.onEvent(AddChallengeEvent.SaveNote)
                                      },
                        ) {
                            Text(text = "Save",
                                color = MaterialTheme.colorScheme.onPrimary,)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back_icon),
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }

            )
        },
        content = { it ->
            Column(
                modifier = Modifier
                    .padding(it)
                    .padding(start = 8.dp, end = 8.dp),

                ) {

                Spacer(modifier = Modifier.padding(top = 24.dp))
                Text(text = "Enter Task Details",
                    style = TextStyle(
                        fontSize = 20.sp,
                        lineHeight = 28.sp,
                        color = MaterialTheme.colorScheme.onPrimary,

                        )
                    )
                Spacer(modifier = Modifier.padding(top = 8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        modifier = Modifier.size(
                            width = 240.dp, height = 56.dp
                        ),
                        value = viewModel.goalName.value.goalName,
                        onValueChange = {
                            viewModel.onEvent(AddChallengeEvent.EnteredName(it))
                        },
                        label = {
                            Text("Task")
                        },

                        )
                    Box {
                        OutlinedTextField(modifier = Modifier.size(
                            width = 240.dp, height = 56.dp
                        ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Transparent,
                                unfocusedTextColor = Color.Transparent
                            ),
                            value = "c",
                            onValueChange = {

                            },
                            label = {
                                Text("Color")
                            },
                            readOnly = true,
                            leadingIcon = {
                                // Spacer(modifier = Modifier.padding(start = 18.dp))
                                Row(horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.clickable {
                                        showDialog()
                                    }
                                ) {
                                    Spacer(modifier = Modifier.padding(start = 18.dp))
                                    Column {
                                        Spacer(modifier = Modifier.padding(top = 8.dp))
                                        Box(

                                            modifier = Modifier
                                                .background(
                                                    color = Color(viewModel.goalColor.value),
                                                    shape = MaterialTheme.shapes.small
                                                )
                                                .size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.padding(bottom = 8.dp))
                                    }

                                }
                            },
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.expand_more_icon),
                                    contentDescription = "expand_color_picker",
                                    modifier = Modifier.clickable {
                                        showDialog()
                                    }
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.padding(top = 16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)

                ) {

                    val calendarTime = Calendar.getInstance()
                 if(viewModel.alarmTime.value.alarmTime!=null)  {
                     calendarTime.timeInMillis = viewModel.alarmTime.value.alarmTime!!
                     val selectedDate = Date(viewModel.alarmTime.value.alarmTime!!)
                 }

                    val noOfDays = viewModel.frequency.value.frequency.selectedDays.size
                    OutlinedTextField(
                        modifier = Modifier.size(
                            width = 240.dp, height = 56.dp
                        ),
                        value = if (viewModel.frequency.value.frequency.selectedDays.size==7||viewModel.frequency.value.frequency.selectedDays.isEmpty()) {
                           "Daily"
                        } else {
                            "$noOfDays days weekly"
                        },
                        onValueChange = {
                            // viewModel.onEvent(AddChallengeEvent.Frequency(it))
                        },
                        label = {
                            Text("Frequency")
                        },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.expand_more_icon),
                                contentDescription = "",
                                modifier = Modifier.clickable {
                                    showFrequencyDialog.value = true
                                },
                            )
                        },
                    )
                    OutlinedTextField(
                        modifier = Modifier.size(
                            width = 240.dp, height = 56.dp
                        ),
                        value = if(viewModel.alarmTime.value.alarmTime!=null){
                            val selectedDate = Date(viewModel.alarmTime.value.alarmTime!!)
                            formatter.format(selectedDate) }else "Not Set",
                        onValueChange = {
                        },

                        label = {
                            Text("Reminder")
                        },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.expand_more_icon),
                                contentDescription = "",
                                modifier = Modifier.clickable {
                                    mTimePickerDialog.show()

                                },
                            )
                        },
                    )
                }
                Spacer(modifier = Modifier.padding(top = 16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)

                ) {
                    OutlinedTextField(
                        modifier = Modifier.size(
                            width = 240.dp, height = 56.dp
                        ),
                        value = millisecondsToMinutes(viewModel.focusTime.value.focusTime).toString() + " mins",
                        onValueChange = {

                        },
                        label = {
                            Text("focus duration")
                        },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.expand_more_icon),
                                contentDescription = "expand focus duration",
                                modifier = Modifier.clickable {
                                    showfocusDurationDialog.value = true
                                },
                            )
                        },

                        )
                    OutlinedTextField(
                        modifier = Modifier.size(
                            width = 240.dp, height = 56.dp
                        ),
                        value = viewModel.noOfDays.value.noOfDays.toString(),
                        onValueChange = {
                        },

                        label = {
                            Text("No. of days")
                        },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.expand_more_icon),
                                contentDescription = "",
                                modifier = Modifier.clickable {
                                    showNoOfDaysDialog.value = true
                                },
                            )
                        },
                    )
                }
                Spacer(modifier = Modifier.padding(top = 16.dp))
                TransparentHintTextField(
                    text = viewModel.description.value.description,
                    hint = "Add details",
                    onValueChange = {
                        viewModel.onEvent(AddChallengeEvent.EnteredDescription(it))
                    },
                    onFocusChange = {

                    },
                    isHintVisible = false
                )

            }


        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    )

    if (showDialog.value) {

        ColorsDialog(
            viewModel = viewModel,
            onColorClicked = viewModel::onColorSelected,
            onCancel = {

                hideDialog()

            },
            onOk = {
                hideDialog()/* do something with the selected tags */
            }
        )
    }
    if (showNoOfDaysDialog.value) {

        NoOfDaysDialog(
            viewModel = viewModel,
            onCancel = {
                showNoOfDaysDialog.value = false
            },
            onOk = {
                showNoOfDaysDialog.value = false
            }
        )
    }
    if (showFrequencyDialog.value) {
        FrequencyDialog(
            viewModel = viewModel,
            onCancel = {
                showFrequencyDialog.value = false
            },
            onOk = {
                showFrequencyDialog.value = false

            }
        )

    }

    if (showfocusDurationDialog.value) {
        FocusDurationDialog(
            viewModel = viewModel,
            onCancel = {
                showfocusDurationDialog.value = false
            },
            onOk = {
                showfocusDurationDialog.value = false

            }
        )

    }


}

