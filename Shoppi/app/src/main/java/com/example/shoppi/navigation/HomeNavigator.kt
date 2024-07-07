package com.example.shoppi.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.shoppi.cart_order.CartScreen
import com.example.shoppi.checkout.CheckoutScreen
import com.example.shoppi.screens.AppScreens.*
import com.example.shoppi.successful_order.OrderSuccessfulScreen
import com.example.shoppi.ui.detail.DetailScreen
import com.example.shoppi.ui.home.DrawerNav

@Composable
fun AppNavigator(onSwitch:()->Unit) {

    val navController = rememberNavController()

    //mapping the screens

    NavHost(navController = navController, startDestination = HOMESCREEN.name){

        composable(route = HOMESCREEN.name){
DrawerNav(
    navController,onSwitch
)

        }

        composable(route = ONBOARDING.name){

        }



        composable(route = "${DETAIL.name}?name={name}&price={price}&image={image}",
            arguments = listOf(
                navArgument(
                    name = "name"
                ) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(
                    name = "price"
                ) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(
                    name = "image"
                ) {
                    type = NavType.IntType
                    defaultValue = 0
                },
            )
            ){entry ->
            val name = entry.arguments?.getString("name") ?: ""
            val price = entry.arguments?.getString("price") ?: ""
            val imageId = entry.arguments?.getInt("image") ?: 0


DetailScreen(navController, name = name, price = price, image = imageId)
        }
        composable(route = "${CART.name}?name={name}&price={price}&image={image}",
            arguments = listOf(
                navArgument(
                    name = "name"
                ) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(
                    name = "price"
                ) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(
                    name = "image"
                ) {
                    type = NavType.IntType
                    defaultValue = 0
                },
            )
            ){entry ->
            val name = entry.arguments?.getString("name") ?: ""
            val price = entry.arguments?.getString("price") ?: ""
            val imageId = entry.arguments?.getInt("image") ?: 0
            CartScreen(navController, name = name,price=price, image = imageId)
        }

        composable(route = CHECK_OUT.name){
            CheckoutScreen (navController)
        }
        composable(route = ORDER_SUCCESS.name){
            OrderSuccessfulScreen()
        }

    }
}