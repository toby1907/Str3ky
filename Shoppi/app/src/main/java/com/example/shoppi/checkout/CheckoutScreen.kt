package com.example.shoppi.checkout

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.shoppi.R
import com.example.shoppi.screens.AppScreens


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(navController:NavController) {

    Scaffold(
        modifier = Modifier,
        topBar = {
                 TopAppBar(
                     title = {
                         Text(
                             modifier = Modifier.fillMaxWidth(),
                             text = "CHECKOUT",
                             textAlign = TextAlign.Center,
                             fontWeight = FontWeight.Bold
                         )
                     },
                     navigationIcon = {
                                      IconButton(onClick = { /*TODO*/ }) {
                                          Icon(
                                              imageVector = Icons.Default.ArrowBack,
                                              contentDescription = "Back Icon Button"
                                          )

                                      }
                     },
                     actions = {
                         IconButton(onClick = { /*TODO*/ }) {
                             Icon(
                                 imageVector = Icons.Outlined.Notifications,
                                 contentDescription = "Notifications icon"
                             )

                         }
                     },

                 )
        } ,
        content = { paddingValues ->
            Column(
                modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues)
            ) {

                CheckoutScreenSection(navController = navController)

            }
        }
    )


}

@Composable
fun CheckoutScreenSection(navController:NavController) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Text(
                text = "Choose a payment",
                fontWeight = FontWeight.Light,
                fontSize = 36.sp
            )
            Text(
                text = "Method",
                fontWeight = FontWeight.Normal,
                fontSize = 36.sp
            )

            Spacer(modifier = Modifier.padding(vertical = 32.dp))

            Row(modifier = Modifier) {

                Surface(
                    modifier = Modifier
                        .padding(start = 0.dp, end = 8.dp)
                        .size(width = 82.dp, height = 65.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.apple_pay),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(16.dp)
                            .size(width = 51.dp, height = 21.dp)
                    )

                }

                Surface(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .size(width = 82.dp, height = 65.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.visa_logo),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(16.dp)
                            .size(width = 51.dp, height = 21.dp)
                    )

                }

                Surface(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .size(width = 82.dp, height = 65.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.mastercard),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(16.dp)
                            .size(width = 51.dp, height = 21.dp)
                    )

                }

                Surface(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 0.dp)
                        .size(width = 82.dp, height = 65.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.add_),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(16.dp)
                            .size(width = 51.dp, height = 21.dp)
                    )

                }
            }

            Spacer(modifier = Modifier.padding(26.dp))

            Text(
                text = "Delivery address",
                fontWeight = FontWeight.Normal,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.padding(15.dp))

            Row() {


                    Image(
                        painter = painterResource(id = R.drawable.location),
                        contentScale = ContentScale.FillBounds,
                        contentDescription = null,
                        modifier = Modifier.size(width = 86.dp, height = 87.dp)
                    )

                Spacer(modifier = Modifier.padding( 8.dp))

                Column(
                    modifier = Modifier.padding()
                ) {

                    Text(
                        text = "Home address",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text(
                        text = "Toodely Benson Allentown, New \n Mexico 31134.",
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp
                    )
                    
                }

                Spacer(modifier = Modifier.padding(8.dp))

                Image(
                    painter = painterResource(id = R.drawable.location_edit),
                    contentDescription = "location_image",
                    modifier = Modifier
                        .padding(top = 36.dp)
                        .size(15.dp)

                )

            }

            Spacer(modifier = Modifier.padding(vertical = 26.dp))

            Row() {

                Text(
                    text = "SubTotal",
                    fontWeight = FontWeight.Light,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.padding(end = 228.dp))

                Text(
                    text = " $ 1375",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

            }

            Row() {

                Text(
                    text = "Shipping Cost",
                    fontWeight = FontWeight.Light,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.padding(end = 204.dp))

                Text(
                    text = " $ 80",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

            }

            Spacer(modifier = Modifier.padding(vertical = 13.dp))

            StraightLine()

            Spacer(modifier = Modifier.padding(vertical = 13.dp))

            Row() {

                Text(
                    text = "Total",
                    fontWeight = FontWeight.Light,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.padding(end = 256.dp))

                Text(
                    text = "$ 1455",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

            }

            Spacer(modifier = Modifier.padding(32.dp))

            Button(
                modifier = Modifier
                    .height(66.dp)
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.White,
                    containerColor = colorResource(id = R.color.sky_blue)
                ),
                onClick = { navController
                    .navigate(AppScreens.ORDER_SUCCESS.name)
                    {
                        launchSingleTop = true
                        popUpTo(AppScreens.CHECK_OUT.name) { inclusive = true }
                    }
                          },
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    text = "Pay Now",
                    fontSize = 20.sp
                )
            }



        }
    }


@Composable
fun StraightLine() {
    Canvas(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .background(colorResource(id = R.color.light_grey))
    ) {
        drawLine(
            color = Color.Black,
            start = Offset(x = 0f, y = 0f),
            end = Offset(x = size.width, y = size.height),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Butt
        )
    }
}


/*@Preview
@Composable
fun PreviewCheckoutScreen() {
    val context =
    Column(modifier = Modifier.fillMaxSize()) {
        CheckoutScreen (navController = NavController())
    }
}*/
