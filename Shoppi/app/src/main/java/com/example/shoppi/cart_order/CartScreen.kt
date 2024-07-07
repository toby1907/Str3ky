package com.example.shoppi.cart_order

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.shoppi.ui.home.ShoeItem


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(nav:NavController,name: String, price: String, image: Int) {

    Scaffold(
        modifier = Modifier,
        topBar = {
                 TopAppBar(
                     title = {
                         Text(
                             modifier = Modifier.fillMaxWidth(),
                             text = "CART",
                             textAlign = TextAlign.Center,
                             fontWeight = FontWeight.Bold
                         )
                     },
                     navigationIcon = {
                         IconButton(onClick = { nav.navigateUp() }) {
                             Icon(imageVector = Icons.Default.ArrowBack,
                                 contentDescription = "Back icon button")
                             
                         }
                     },
                     actions = {
                         IconButton(onClick = { /*TODO*/ }) {
                             Icon(
                                 imageVector = Icons.Outlined.Notifications,
                                 contentDescription = "Notifications icon"
                             )
                             
                         }
                     }
                 )
        },
        content = { paddingValue ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = paddingValue)
            ) {

                CartScreenSection(nav,name, price, image)

            }


        }
    )


}

@Composable
fun CartScreenSection(navController:NavController,name: String, price: String, image: Int) {
   val hideButton = remember {
       mutableStateOf(false)
   }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Text(
                text = "Your Cart",
                fontWeight = FontWeight.Medium,
                fontSize = 36.sp
            )

            Text(
                text = "List(1)",
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp
            )

            Spacer(modifier = Modifier.padding(24.dp))

            AddedItemColumn(name = name, price =  price, image =  image,
                hideButton = {
                    hideButton.value = true
                }
                )
            if(!hideButton.value) {
            Text(
                modifier = Modifier.padding(start = 110.dp),
                text = "Do you have any voucher?",
                fontWeight = FontWeight.Light,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.padding(36.dp))

            Row(
                modifier = Modifier
            ) {

                Text(
                    text = "Total",
                    fontWeight = FontWeight.Light,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.padding(end = 257.dp))
                Text(
                    text = "$ 1375",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )

            }

            Spacer(modifier = Modifier.padding(24.dp))



                Button(
                    modifier = Modifier
                        .height(66.dp)
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.White,
                        containerColor = colorResource(id = R.color.sky_blue)
                    ),
                    onClick = { navController.navigate(AppScreens.CHECK_OUT.name) },
                    shape = MaterialTheme.shapes.large
                ) {
                    Text(
                        text = "Checkout",
                        fontSize = 20.sp
                    )
                }
            }

        }
    }


@Composable
fun AddedItemCard(
    @DrawableRes image: Int,
    name: String?,
    price: String?,
    onDelete:() ->Unit
) {
    var number by remember { mutableStateOf(1) }
    Row() {

        Surface(
            modifier = Modifier.size(height = 164.dp, width = 132.dp),
            shape = MaterialTheme.shapes.medium,
            color = Color(0xFFF8F8FA)
        ) {
            Image(
                painter = painterResource(id = image),
                contentDescription = null
            )
        }

       Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start) {
            Column(
                modifier = Modifier.padding(vertical = 22.dp, horizontal = 16.dp)
            ) {

                Text(
                    text = name!!,
                    fontWeight = FontWeight.Normal,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "$ 650",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                Row() {

                    Icon(
                        painter = painterResource(id = R.drawable.baseline_add),
                        contentDescription = null,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colorResource(id = R.color.sky_blue))
                            .clickable {
                                number++
                            },
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    Text(
                        text = " $number",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_remove_24),
                        contentDescription = null,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable {
                                if (number > 1) {
                                    number--
                                }
                            },
                        tint = colorResource(id = R.color.sky_blue)
                    )

                }


            }

            Spacer(modifier = Modifier.padding(vertical = 32.dp))

            Surface(
                modifier = Modifier
                    .padding(start = 8.dp, top = 12.dp)
                    .size(width = 32.dp, height = 48.dp)
                    .clickable {
                        onDelete()
                    },
                color = colorResource(id = R.color.red),
                shape = MaterialTheme.shapes.large
            ) {

                Icon(
                    painter = painterResource(id = R.drawable.baseline_delete),
                    contentDescription = null,
                    tint = Color.White
                )

            }
        }
    }
}



@Composable
fun AddedItemColumn(
    modifier: Modifier = Modifier,
    name: String, price: String, image: Int,
    hideButton: () -> Unit
) {/*  val shoeItems = mutableListOf(
        ShoeItem("Air Zoom", "$19.99", R.drawable.shoe1,),
        ShoeItem("Air MaxZoom", "$29.99", R.drawable.shoe2),
        ShoeItem("Nike Club Shoe", "$39.99", R.drawable.shoe3),
        ShoeItem("NIke Dual Shoe", "$49.99", R.drawable.shoe4),
        ShoeItem("Reebok Shoe", "$59.99", R.drawable.shoe5),
    )*/
    val state = remember {
        mutableStateOf(true)
    }


    val shoeItems = remember { mutableStateListOf<ShoeItem>() }
    fun addShoeItem(name: String, price: String, image: Int) {
        val newShoe = ShoeItem(name, price, image)
        shoeItems.add(newShoe)
    }
    fun deleteShoeItem(name: String) {
        val shoeToRemove = shoeItems.find { it.title == name }
        shoeToRemove?.let { shoeItems.remove(it) }
        hideButton()
    }

   if (state.value) {
       addShoeItem(name, price, image)
   }

    LazyColumn(
        modifier = modifier.requiredHeight(380.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        if (shoeItems.isEmpty()){
           item { Text(text = "Your cart is empty") }
        }
        items(shoeItems.size) { shoeItem ->


            AddedItemCard(
                image = shoeItems[shoeItem].imageResId,
                name = shoeItems[shoeItem].title,
                price = shoeItems[shoeItem].price,
                onDelete = {
                    deleteShoeItem(shoeItems[shoeItem].title)
                    state.value = false
                }


            )
           /* addItem(
                image = R.drawable.sneaker_one,
                name = "Air Zoom",
                price = "$ 650"
            )

            addItem(
                image = R.drawable.sneaker,
                name = "Air Max Zoom",
                price = "$ 728"
            )*/
        }
    }

}

@Composable
fun AlertDialogButton() {
    val openDialog = remember { mutableStateOf(true) }

    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = {
                // Dismiss the dialog when the user clicks outside the dialog or on the back
                // button. If you want to disable that functionality, simply use an empty
                // onDismissRequest.
                openDialog.value = false
            },
            title = {
                Text(
                    text = "Are you sure you want to remove this item?",
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(text = "")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDialog.value = false
                    }
                ) {
                    Text("Remove")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        openDialog.value = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}



