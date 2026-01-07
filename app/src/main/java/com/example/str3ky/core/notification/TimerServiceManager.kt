package com.example.str3ky.core.notification

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.str3ky.data.TimerActions
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class TimerServiceManager @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
){

     fun startTimerService(suppressNextFinished: Boolean = false) {
        val serviceIntent = Intent(applicationContext, TimerService::class.java)
        if (suppressNextFinished) {
            serviceIntent.action = TimerService.ACTION_SUPPRESS_NEXT_FINISHED
        }
        ContextCompat.startForegroundService(applicationContext, serviceIntent)
    }

     fun stopTimerService() {
        val serviceIntent = Intent(applicationContext, TimerService::class.java)
        applicationContext.stopService(serviceIntent)
    }

}
















