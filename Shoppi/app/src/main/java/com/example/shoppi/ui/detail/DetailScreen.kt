package com.example.shoppi.ui.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.shoppi.R
import com.example.shoppi.screens.AppScreens
import com.example.shoppi.ui.theme.ShoppiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(navController: NavController,name: String, price: String, image: Int) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Detail Screen") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack()}) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = image),
                    contentDescription = "Air Max 270",
                    modifier = Modifier
                        .width(337.dp)
                        .height(193.dp)
                        .padding(top = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.padding(16.dp))
                    Column(
                        modifier = Modifier.padding(top = 16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {


                        Text(
                            text = name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        Text(
                            text = "Sneakers",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SelectorItem(Color.Red, 8.dp)
                        SelectorItem(Color.Green, 8.dp)
                        SelectorItem(Color.Blue, 8.dp)
                    }
                }
                Spacer(modifier = Modifier.padding(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column() {
                        Row() {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Text(
                                    text = "Structure",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Column(
                                    modifier = Modifier.padding(top = 8.dp),
                                    verticalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Outer Material : ",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                        Text(
                                            text = "Rubber 100%",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Column(
                                    modifier = Modifier.padding(top = 8.dp),
                                    verticalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Lining: ",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                        Text(
                                            text = "Rubber 100%",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Column(
                                    modifier = Modifier.padding(top = 8.dp),
                                    verticalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Sole: ",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                        Text(
                                            text = "Rubber 100%",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(Modifier.size(16.dp))
                                    Text(
                                        text = "...Details",
                                        fontSize = 20.sp,
                                            color = Color(0xFF69BCFC))
                                }

                            }
                            Surface(
                                modifier = Modifier
                                    .size(99.dp, 157.dp)
                                    .clickable { navController.navigate(AppScreens.CART.name+"?name=${name}&price=${price}&image=${image}")
                                    }
                                ,
                                color = Color(0xff69bcfc),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(  modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Image(painter = painterResource(id = R.drawable.cart_icon), contentDescription = "")
                                    Spacer(modifier = Modifier.padding(16.dp))
                                    Text(
                                        text = price,
                                        fontSize = 16.sp,
                                            fontWeight = FontWeight(600),
                                            color = Color(0xFFFFFFFF))
                                }

                            }
                        }
                    }
                }
            }

        }
    )
}

/* Surface(
                    modifier=Modifier.size(99.dp,157.dp),
                    color=Color(0xff69bcfc)
                )*/
@Composable
fun SelectorItem(color: Color, size: Dp) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = { /*TODO*/ })
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .background(color, CircleShape)
        )
    }
}
