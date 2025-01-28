package com.example.str3ky.use_case.util

sealed class GoalOrder(val orderType: OrderType) {
    class Title(orderType: OrderType): GoalOrder(orderType)
    class Date(orderType: OrderType): GoalOrder(orderType)
    class Color(orderType: OrderType): GoalOrder(orderType)

    fun copy(orderType: OrderType): GoalOrder {
        return when(this) {
            is Title -> Title(orderType)
            is Date -> Date(orderType)
            is Color -> Color(orderType)
        }
    }

}