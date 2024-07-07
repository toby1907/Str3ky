package com.example.goalgetta.utils

import java.text.SimpleDateFormat
import java.util.*

object DateConverter {
    fun convertMillisToString(timeMillis: Long): String{
        val calender = Calendar.getInstance()
        calender.timeInMillis =timeMillis
        val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())

        return sdf.format(calender.time)
    }

}