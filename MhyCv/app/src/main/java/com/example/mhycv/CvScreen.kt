package com.example.mhycv

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mhycv.ui.theme.MhyCvTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Cvscreen(function: () -> Unit,viewModel: CvViewModel
) {
    Scaffold(
        floatingActionButton = {
            // Use a floating action button composable to display a save icon and save the changes made to the CV data
            FloatingActionButton(onClick = {
                // Save the CV data to a local or remote storage
                // ...
                // Navigate back to the previous screen
              function()
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.edit),
                    contentDescription = "Save icon"
                )
            }
        }
    ) {
    // Use a column to arrange the UI elements vertically
    Column(modifier = Modifier
        .padding(it)
        .clickable { function() }
        .verticalScroll(rememberScrollState())
    ) {
        // Use a box to create the header section with a dark gray background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            // Use a column to arrange the text elements inside the header section
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Use a text composable to display the first name with large size and white color
                Text(
                    text = viewModel.cvData.value.firstName,
                    fontSize = 32.sp,
                    color = Color.White
                )
                // Use a text composable to display the last name with large size and white color
                Text(
                    text = viewModel.cvData.value.lastName,
                    fontSize = 32.sp,
                    color = Color.White
                )
                // Use a text composable to display the job title with small size and white color
                Text(
                    text = viewModel.cvData.value.jobTitle,
                    fontSize = 16.sp,
                    color = Color(0xfff7f9fc)
                )
            }
        }
        // Use a divider composable to create a light gray line below the header section
        Divider(color = Color.White, thickness = 2.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(id = R.drawable.slack),
                        contentDescription = "Email icon"
                    )

                    Text(
                        text = viewModel.cvData.value.slack,
                        fontSize = 16.sp,
                        color = Color.Black,
                    )
                }
                // Use a text composable to display the location and country with normal size and black color
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(id = R.drawable.location),
                        contentDescription = "Email icon",
                    )
                    Text(
                        text = viewModel.cvData.value.location,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
                // Use a text composable to display the phone number with normal size and black color{{
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(id = R.drawable.github__1_),
                        contentDescription = "Email icon",
                    )
                    Text(
                        text = viewModel.cvData.value.github,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            }
        }
        ProfileSection(viewModel)
    }
}
}

@Composable
fun ProfileSection(viewModel: CvViewModel) {
    // Define some sample data for the personal bio section
    val bio =viewModel.cvData.value.bio

    // Use a column to arrange the UI elements vertically
    Column {
        // Add the other UI elements here
        // ...
        // Use a divider composable to create a white line below the previous section
        Divider(color = Color.White, thickness = 2.dp)
        // Use a column to arrange the text elements below the divider
        Column(modifier = Modifier.padding(16.dp)) {
            // Use a text composable to display the title of the personal bio section with large size and black color
            Text(
                text = "Personal Bio",
                fontSize = 28.sp,
                color = Color.Black
            )
            // Use a spacer composable to add some vertical space between the title and the bio
            Spacer(modifier = Modifier.height(8.dp))
            // Use the PersonalBio composable function to display the personal bio with normal size and black color
            PersonalBio(bio = bio)
        }
    }
}

@Composable
fun PersonalBio(bio: String) {
    // Use a text composable to display the personal bio with normal size and black color
    // Use the maxLines parameter to limit the number of lines to 10
    // Use the overflow parameter to show an ellipsis if the text exceeds the maxLines
    Text(
        text = bio,
        fontSize = 16.sp,
        color = Color.Black,
        maxLines = 10,
        overflow = TextOverflow.Ellipsis
    )
}

