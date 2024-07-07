package com.example.shoppi.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.shoppi.R
import com.example.shoppi.screens.AppScreens
import com.example.shoppi.ui.TopAppBarEnum
import com.example.shoppi.ui.components.AppBar
import com.example.shoppi.ui.components.BottomAppNav
import com.example.shoppi.ui.components.SegmentedControl
import com.example.shoppi.ui.theme.ShoppiTheme
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    menuOnClick: () -> Unit,
    navController: NavController,
    onSwitch: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    val shoeItems = listOf(
        ShoeItem("Air Zoom", "$19.99", R.drawable.shoe1,),
        ShoeItem("Air MaxZoom", "$29.99", R.drawable.shoe2),
        ShoeItem("Nike Club Shoe", "$39.99", R.drawable.shoe3),
        ShoeItem("NIke Dual Shoe", "$49.99", R.drawable.shoe4),
        ShoeItem("Reebok Shoe", "$59.99", R.drawable.shoe5),
    )
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AppBar(
                menuOnClick = menuOnClick,
                actionOnClick = { },
                topAppBarEnum = TopAppBarEnum.HOME,
                appBarTitle = "Home",
                actionIcon = null,
                navigationIcon = R.drawable.menu,
                scrollBehavior =scrollBehavior,
                onSwitch = onSwitch
            )
        },
        bottomBar = {
            BottomAppNav(navController)
        },
        content = { innerPadding ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(innerPadding),

                ) {
//Segmented buttons section
               Column(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)) {
                    Spacer(modifier = Modifier.size(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column() {
                            Text(
                                text = "Enjoy New Nike",
                                fontSize = 36.sp,
                                fontWeight = FontWeight(300),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                            Text(
                                text = "Products",
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(bottom = 16.dp, start = 16.dp)
                            )
                        }
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    sheetState.partialExpand()
                                }
                            },
                            shape = RoundedCornerShape(60.dp, 0.dp, 0.dp, 60.dp),
                            modifier = Modifier.size(height = 66.dp, width = 74.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xffff5545))
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.filter_8),
                                contentDescription = "filter",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    SegmentedControl()
                }


                LazyVerticalGrid(columns = GridCells.Fixed(2)) {
                    items(shoeItems.size) { shoeItem ->

                        Column {
                            Card(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clickable(onClick = {
                                        navController.navigate(AppScreens.DETAIL.name+"?name=${shoeItems[shoeItem].title}&price=${shoeItems[shoeItem].price}&image=${shoeItems[shoeItem].imageResId}")
                                    }),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Image(
                                    painter = painterResource(shoeItems[shoeItem % shoeItems.size].imageResId),
                                    contentDescription = "Shoe Image",
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(shape = RoundedCornerShape(8.dp))
                                )
                            }
                            Text(
                                text = shoeItems[shoeItem % shoeItems.size].title,
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(8.dp)
                            )
                            Text(
                                text = shoeItems[shoeItem % shoeItems.size].price,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            //To start the bottomSheet
            BottomSheet(
                sheetState = sheetState,
                onDismiss = {
                    coroutineScope.launch {
                        sheetState.hide()
                    }
                }
            )

        }
    )
}


data class ShoeItem(val title: String, val price: String, @DrawableRes val imageResId: Int)

