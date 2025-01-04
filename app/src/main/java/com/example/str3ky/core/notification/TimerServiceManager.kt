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
/*fun startTimerService() {
    val serviceIntent = Intent(applicationContext,TimerService::class.java).
        also {
           it.action = TimerActions.START.name
        }
    ContextCompat.startForegroundService(applicationContext,serviceIntent)
}
    fun stopTimerService() {
       *//* val serviceIntent = Intent(applicationContext,TimerService::class.java).also {
            it.action = TimerActions.STOP.name
        }
        applicationContext.stopService(serviceIntent)*//*
        val serviceIntent = Intent(applicationContext,TimerService::class.java)
            .also {
                it.action = TimerActions.STOP.name
            }
        ContextCompat.startForegroundService(applicationContext,serviceIntent)
    }
    fun onSessionCompleted() {
        *//* val serviceIntent = Intent(applicationContext,TimerService::class.java).also {
             it.action = TimerActions.STOP.name
         }
         applicationContext.stopService(serviceIntent)*//*
        val serviceIntent = Intent(applicationContext,TimerService::class.java)
            .also {
                it.action = TimerActions.COMPLETED.name
            }
        ContextCompat.startForegroundService(applicationContext,serviceIntent)
    }
    fun sessionCompletedNotificationCancel() {

        val serviceIntent = Intent(applicationContext,TimerService::class.java)
            .also {
                it.action = TimerActions.CANCEL.name
            }
        ContextCompat.startForegroundService(applicationContext,serviceIntent)
    }*/

     fun startTimerService() {
        val serviceIntent = Intent(applicationContext, TimerService::class.java)
        ContextCompat.startForegroundService(applicationContext, serviceIntent)
    }

     fun stopTimerService() {
        val serviceIntent = Intent(applicationContext, TimerService::class.java)
        applicationContext.stopService(serviceIntent)
    }

}

