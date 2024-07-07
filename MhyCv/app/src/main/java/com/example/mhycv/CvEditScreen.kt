package com.example.mhycv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mhycv.ui.theme.MhyCvTheme

// Define a composable function that creates an edit screen for the CV with a top app bar and a floating action button
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CvEditScreen(navController: NavController,viewModel: CvViewModel) {



    // Use a scaffold composable to create a screen with a top app bar and a floating action button
    Scaffold(
        topBar = {
            // Use an app bar composable to display the title of the screen and an icon button to navigate back
            TopAppBar(
                title = { Text(text = "Edit CV") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back icon"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // Use a floating action button composable to display a save icon and save the changes made to the CV data
            FloatingActionButton(onClick = {
                // Save the CV data to a local or remote storage
                // ...
                // Navigate back to the previous screen
                navController.navigateUp()
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.save),
                    contentDescription = "Save icon"
                )
            }
        }
    ) {
        // Use a scrollable column composable to display the UI elements of the CV screen inside the scaffold content
        Column(verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(it)
                .verticalScroll(rememberScrollState())
            ,
        ) {
            // Use text field composables to display and edit the text elements of the CV data, and bind them to the view model state
            TextField(value = viewModel.cvData.value.firstName,
                onValueChange = {  viewModel.updateFirstName(it) },
                label = { Text(text = "First Name") })
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = viewModel.cvData.value.lastName,
                onValueChange = { viewModel.updateLastName(it) },
                label = { Text(text = "Last Name") })
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = viewModel.cvData.value.jobTitle,
                onValueChange = { viewModel.updateJobTitle(it) },
                label = { Text(text = "Job Title") })
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = viewModel.cvData.value.slack,
                onValueChange = { viewModel.updateEmail(it) },
                label = { Text(text = "Slack Username") })
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = viewModel.cvData.value.location,
                onValueChange = { viewModel.updateLocation(it) },
                label = { Text(text = "Location") })
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = viewModel.cvData.value.github,
                onValueChange = { viewModel.updatePhone(it) },
                label = { Text(text = "GitHub Link") })
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = viewModel.cvData.value.bio,
                onValueChange = { viewModel.updateBio(it) },
                label = { Text(text = "Personal Bio") },
                maxLines = 10
            )
        }
    }
}

