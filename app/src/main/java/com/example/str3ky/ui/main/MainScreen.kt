package com.example.str3ky.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.str3ky.R
import com.example.str3ky.data.Goal
import com.example.str3ky.data.Occurrence
import com.example.str3ky.millisecondsToMinutes
import com.example.str3ky.ui.nav.ACHIEVEMENTS_SCREEN
import com.example.str3ky.ui.nav.PROGRESS_SCREEN
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddVoice: () -> Unit,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    viewModel:MainScreenViewModel = hiltViewModel()
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Str3ky") },
                actions = {
                 Row(verticalAlignment = Alignment.CenterVertically,
                     horizontalArrangement = Arrangement.End,
                     modifier = Modifier.padding(start = 4.dp)
                 )
                 {
                     IconButton(onClick = {onNavigateToAddVoice() }) {
                         Icon(
                             painter = painterResource(id = R.drawable.add_icon),
                             contentDescription = ""
                         )
                     }
                     IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.sorting_icon),
                            contentDescription = ""
                        )
                     }
                     IconButton(onClick = {
                         navController.navigate(ACHIEVEMENTS_SCREEN)
                     }) {


                         Icon(
                             painter = painterResource(id = R.drawable.more_vert_24px),
                             contentDescription = ""
                         )
                         
                     }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
                
                )   
        },
        content = { it ->

            val items = viewModel.goalList.collectAsState().value
LazyColumn(
    modifier = Modifier.padding(vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    contentPadding = it,
    content = {
        items(items){ item ->

ChallengListItem(item,navController)

        }
})
        }
    )

}


@Composable
fun ChallengListItem(item: Goal, navController: NavHostController) {
    val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    ListItem(modifier = Modifier.clickable {

        navController.navigate(PROGRESS_SCREEN+"?goalId=${item.id}")


                                           },
        headlineContent = {

        Text(
            text = item.title?:"",
            color = MaterialTheme.colorScheme.primary
        )

    },
        leadingContent = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    )
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(
                        width = 3.dp,
                        color = Color.Transparent,
                        shape = CircleShape
                    )

            ) {
                Text(
                    text = if(item.title!="") "${item.title[0].uppercaseChar()}" else ""
                )
            }
        },
        supportingContent = {
            Row (horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,){
              Row(
                 horizontalArrangement = Arrangement.spacedBy(8.dp,Alignment.Start),
                  verticalAlignment = Alignment.CenterVertically
              )  {

                    Icon(
                        painter = painterResource(id = R.drawable.arrow_up_circle_icon),
                        contentDescription = ""
                    )
                    Text(
                        text = millisecondsToMinutes(item.focusSet).toString()+" mins",
                        style = TextStyle(fontSize = 8.sp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Row( horizontalArrangement = Arrangement.spacedBy(8.dp,Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically)  {
                    Icon(
                        painter = painterResource(id = R.drawable.calendar_icon),
                        contentDescription = ""
                    )
                    val text = when(item.occurrence.dayOption.name){
                        Occurrence.DAILY.name -> "Daily"
                        Occurrence.DAILY_WITHOUT_WEEKEND.name -> ""
                        Occurrence.CUSTOM.name ->  "${item.occurrence.selectedDays.size} days weekly"
                        else -> ""
                    }
                    Text(
                        text = text ,
                        style = TextStyle(fontSize = 8.sp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Row( horizontalArrangement = Arrangement.spacedBy(8.dp,Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically)  {
                    Icon(
                        painter = painterResource(id = R.drawable.alert_icon),
                        contentDescription = ""
                    )

                    Text(
                        text =  if(item.alarmTime!=null){
                            val selectedDate = Date(item.alarmTime)
                            formatter.format(selectedDate) }else "Not Set",
                        style = TextStyle(fontSize = 8.sp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        )
}


