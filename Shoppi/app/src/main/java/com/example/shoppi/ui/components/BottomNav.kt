package com.example.shoppi.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.shoppi.R
import com.example.shoppi.screens.AppScreens
import com.example.shoppi.ui.theme.ShoppiTheme

@Composable
fun BottomAppNav(navController: NavController){

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                BottomAppBar(
                    containerColor = Color(0xFF152354),
                    modifier = Modifier

                        .align(Alignment.BottomCenter)
                        .size(width = 396.dp, height = 86.dp)
                        .clip(RoundedCornerShape(60.dp, 60.dp, 60.dp, 60.dp)),
                    tonalElevation = 8.dp
                )
                {
                    Row(
                        modifier = Modifier.fillMaxWidth(),

                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {

                        // BottomNavigationItem(s) here
                        IconButton(
                            onClick = { navController.navigate(AppScreens.HOMESCREEN.name) },
                            /*  modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = rememberRipple(color = Color.Gray, radius = 24.dp)
                            )*/
                        ) {
                            Icon(
                                painterResource(id = R.drawable.home), contentDescription = "Home",
                                tint = Color.White
                            )
                        }

                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                painterResource(id = R.drawable.favorite),
                                contentDescription = "Favorite",
                                tint = Color.White
                            )
                        }
                        val shoeItem = com.example.shoppi.ui.home.ShoeItem(
                            "Air Zoom",
                            "$19.99",
                            R.drawable.shoe1,
                        )
                        IconButton(onClick = {
                            navController.navigate(AppScreens.CART.name+"?name=${shoeItem.title}&price=${shoeItem.price}&image=${shoeItem.imageResId}")
                        })
                        {
                            Icon(
                                painterResource(id = R.drawable.cart_icon),
                                contentDescription = "Message",
                                tint = Color.White
                            )
                        }

                    }
                }
            }
        }
    }
}

/*
* @Composable
fun BottomAppBarWithIcons() {
    BottomAppBar(
        backgroundColor = Color.White,
        contentColor = Color.Black,
        elevation = 8.dp,
        cutoutShape = CircleShape
    ) {
        IconButton(onClick = { /* Do something */ }) {
            Icon(Icons.Default.Home, contentDescription = "Home")
        }
        IconButton(onClick = { /* Do something */ }) {
            Icon(Icons.Default.Search, contentDescription = "Discovery")
        }
        IconButton(onClick = { /* Do something */ }) {
            Icon(Icons.Default.Favorite, contentDescription = "Favourite")
        }
        IconButton(onClick = { /* Do something */ }) {
            Icon(Icons.Default.Message, contentDescription = "Message")
        }
        IconButton(onClick = { /* Do something */ }) {
            Icon(Icons.Default.Person, contentDescription = "Profile")
        }
    }
}*/

@Preview(showBackground = true)
@Composable
fun BottomAppPreview() {
    ShoppiTheme {
        BottomAppNav(navController = NavController(LocalContext.current))
    }
}