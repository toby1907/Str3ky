package com.example.explorr.ui

sealed class Screen(val route: String) {
    object MainScreen: Screen("main")
    object DetailScreen: Screen("detail_screen")
}