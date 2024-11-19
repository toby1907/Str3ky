package com.example.str3ky.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.str3ky.R
import com.example.str3ky.data.CountdownTimerManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TimerService : Service(){

    @Inject
    lateinit var countdownTimerManager:CountdownTimerManager

    private lateinit var notificationManager:NotificationManagerCompat



    override fun onCreate() {
        super.onCreate()
        notificationManager=NotificationManagerCompat.from(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, TIMER_SERVICE_CHANNEL_ID)
            .setContentTitle("Timer Service")
            .setContentText("Timer running")
            .setSmallIcon(R.drawable.baseline_timer_24)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .build()

            startForeground(TIMER_SERVICE_NOTIFICATION_ID, notification)
        return START_STICKY
    }
    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val timerServiceChannel = NotificationChannel(
                TIMER_SERVICE_CHANNEL_ID,
                getString(R.string.timer_service_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT

            )

            notificationManager.createNotificationChannel(timerServiceChannel)

        }

    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}

private const val TIMER_SERVICE_CHANNEL_ID = "timer_service_channel"
private const val TIMER_SERVICE_NOTIFICATION_ID = 123