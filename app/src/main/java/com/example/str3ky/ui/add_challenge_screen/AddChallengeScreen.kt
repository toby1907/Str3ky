package com.example.str3ky.ui.add_challenge_screen

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.str3ky.R
import com.example.str3ky.theme.Str3kyTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChallengeScreen(
    viewModel: AddScreenViewModel = hiltViewModel(),
    onNavigateToAddVoice: () -> Unit,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    noteColor: Int
) {
    val scope = rememberCoroutineScope()
    val showDialog = remember { mutableStateOf(false) }

    // A function to show the dialog
    fun showDialog() {
        showDialog.value = true
    }

    // A function to hide the dialog
    fun hideDialog() {
        showDialog.value = false
    }

    LaunchedEffect(key1 = true)   {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                UiEvent.SaveNote -> TODO()
                is UiEvent.ShowSnackbar -> scope.launch {
                    snackbarHostState.showSnackbar(
                        message = event.message
                    )
                }
            }

        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Add") },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    {
                        OutlinedButton(
                            onClick = { /*TODO*/ },
                            shape = RoundedCornerShape(0.dp)
                        ) {
                            Text(text = "Save")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(painter = painterResource(id = R.drawable.arrow_back_icon), contentDescription ="" )
                    }
                }

            )
        },
        content = { it ->
            Column(
                modifier = Modifier
                    .padding(it)
                    .padding(start = 8.dp, end = 8.dp)
                ,

            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(value = viewModel.goalName.value.goalName, onValueChange = {
                        viewModel.onEvent(AddChallengeEvent.EnteredName(it))
                    },
                        label = {
                            Text("Name")
                        },

                    )
                    Box(modifier = Modifier.clickable {

                    }){
                        OutlinedTextField(
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Transparent,
                                unfocusedTextColor = Color.Transparent
                            ),
                            value ="c",
                            onValueChange = {

                            },
                            label = {
                                Text("Color")
                            },
                            readOnly = true,
                            leadingIcon = {
                                Row(horizontalArrangement = Arrangement.Center) {
                                    Spacer(modifier = Modifier.padding(start = 18.dp))
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    shape = MaterialTheme.shapes.small
                                                )
                                                .size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.padding(bottom = 8.dp))
                                    }

                                }
                            },
                        )
                    }
                }

                OutlinedTextField(value = "Everyday", onValueChange = {
                   // viewModel.onEvent(AddChallengeEvent.Frequency(it))
                },
                    label = {
                        Text("Frequency")
                    },
                    readOnly = true
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)

                ) {
                    OutlinedTextField(value = viewModel.focusTime.value.focusTime.toString(), onValueChange = {

                    },
                        label = {
                            Text("focus duration")
                        }
                    )
                    OutlinedTextField(
                        value = viewModel.,
                        onValueChange = {
                            nameText.value = it
                        },
                        label = {
                            Text("No. of days")
                        },
                    )
                }

            }

        }
    )
}

