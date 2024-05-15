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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.str3ky.R
import com.example.str3ky.theme.Str3kyTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChallengeScreen() {

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

            ) { // val isImportant = remember { mutableStateOf(false) }
                val nameText = remember {
                    mutableStateOf("")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(value = nameText.value, onValueChange = {
                        nameText.value = it
                    },
                        label = {
                            Text("Name")
                        },

                    )
                    Box(modifier = Modifier.clickable {

                    }){
                        OutlinedTextField(
                            value = nameText.value,
                            onValueChange = {
                                nameText.value = it
                            },
                            label = {
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
                            enabled = false,
                        )
                    }
                }

                OutlinedTextField(value = nameText.value, onValueChange = {
                    nameText.value = it
                },
                    label = {
                        Text("Frequency")
                    }
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)

                ) {
                    OutlinedTextField(value = nameText.value, onValueChange = {
                        nameText.value = it
                    },
                        label = {
                            Text("focus duration")
                        }
                    )
                    OutlinedTextField(
                        value = nameText.value,
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

@Preview(showBackground = true)
@Composable
fun AddChallengeScreenPreview() {

    Str3kyTheme {
        AddChallengeScreen()
    }
}