package com.example.str3ky.use_case.util

sealed class OrderType {
    object Ascending: OrderType()
    object Descending: OrderType()
}