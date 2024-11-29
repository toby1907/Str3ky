package com.example.str3ky.core.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.str3ky.R
import com.example.str3ky.data.CombinedData
import com.example.str3ky.data.CountdownTimerManager
import com.example.str3ky.di.ApplicationScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TimerService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob())


    @Inject
    lateinit var countdownTimerManager: CountdownTimerManager

    private lateinit var notificationManager: NotificationManagerCompat

    private lateinit var timerNotification: NotificationCompat.Builder


    override fun onCreate() {
        super.onCreate()
        notificationManager = NotificationManagerCompat.from(this)
        createNotificationChannel()
        timerNotification = NotificationCompat.Builder(this, TIMER_SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_timer_24)
            .setSilent(true)
            .setOnlyAlertOnce(true)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        startForeground(TIMER_SERVICE_NOTIFICATION_ID, timerNotification.build())

        serviceScope.launch {
            countdownTimerManager.combinedFlow.collect { timerStates ->

                updateTimerNotification(timerStates)

                /* if(it==CountdownTimerManager.Phase.COMPLETED){
                     stopForeground(STOP_FOREGROUND_REMOVE)
                     stopSelf()
                 }*/
            }
        }
        serviceScope.launch {
            countdownTimerManager.combinedFlow.collect { timerStates ->

                updateTimerNotification(timerStates)

                /* if(it==CountdownTimerManager.Phase.COMPLETED){
                     stopForeground(STOP_FOREGROUND_REMOVE)
                     stopSelf()
                 }*/
            }
        }
        return START_STICKY
    }

    private fun updateTimerNotification(timerState: CombinedData) {
        val minutes = timerState.timeLeftInMillis / 1000 / 60
        val seconds = timerState.timeLeftInMillis / 1000 % 60
        val formattedTime = String.format("%02d:%02d", minutes, seconds)

       val notificationUpdate = timerNotification
            .setContentTitle(timerState.currentPhase.name)
            .setContentText(timerState.timeLeftInMillis.toString())
           .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager.notify(TIMER_SERVICE_NOTIFICATION_ID,notificationUpdate)


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
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        countdownTimerManager.cancelCountdown()
    }
}

private const val TIMER_SERVICE_CHANNEL_ID = "timer_service_channel"
private const val TIMER_SERVICE_NOTIFICATION_ID = 123