package com.example.str3ky.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.str3ky.R
import com.example.str3ky.theme.Str3kyTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(){

    Scaffold(
        topBar = {
           TopAppBar(title = { Text(text = "Goals Name") },
               navigationIcon = {
                   Icon(painter = painterResource(id = R.drawable.arrow_back_icon), contentDescription = "" )
               }
               )
        },
        content = {

            TableProgress(completedDays = 10, totalDays = 30, modifier = Modifier.padding(it))
        }
    )
}
@Composable
fun TableProgress(
    completedDays: Int,
    totalDays: Int,
    modifier:Modifier
) {
    val gridSize = 6 // Number of rows and columns
    val tileSize = 56.dp

    Column(modifier = modifier
        .background(color = MaterialTheme.colorScheme.primaryContainer)
    ){
        Text(text = "Challenge Progress",
            style = TextStyle(
                fontSize = 22.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight(400),
                color = Color(0xFFFFFFFF),

                ),
            modifier = Modifier.padding(8.dp)

            )
        Spacer(modifier = Modifier.padding(8.dp))
        Column(

            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.primaryContainer)

        ) {
            repeat(gridSize) { row ->
                Row(
                 //   modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(gridSize) { col ->
                        val day = row * gridSize + col
                        val isActive = day < completedDays
                        val tileColor = if (isActive) Color(0xFFF7E388) else MaterialTheme.colorScheme.inverseOnSurface
                        val indicator = if (isActive) (day + 1).toString() else ""
                        val showCheckMark = isActive && day == completedDays - 1

                        Box(
                            modifier = Modifier
                                .size(tileSize)
                                .clip(RoundedCornerShape(4.dp))
                                .background(tileColor)
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                        ) {


                            if (showCheckMark) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.Center)
                                )
                            }

                            Text(
                                text = indicator,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .background(Color.Transparent)
                                    .padding(2.dp),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Bold
                            )
                        }

                    }
                }
                Spacer(modifier = Modifier.padding(4.dp))
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ChallengeProgressScreenPreview() {

    Str3kyTheme {
        ProgressScreen()
    }
}