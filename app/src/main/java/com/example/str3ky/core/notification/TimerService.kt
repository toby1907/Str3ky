package com.example.str3ky.core.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.example.str3ky.MainActivity
import com.example.str3ky.R
import com.example.str3ky.data.CombinedData
import com.example.str3ky.data.CountdownTimerManager
import com.example.str3ky.data.TimerActions
import com.example.str3ky.ui.nav.MY_ARG
import com.example.str3ky.ui.nav.MY_URI
import com.florianwalther.incentivetimer.core.notification.DefaultNotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TimerService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob())

    @Inject
    lateinit var notificationHelper: DefaultNotificationHelper


    @Inject
    lateinit var countdownTimerManager: CountdownTimerManager

    private lateinit var notificationManager: NotificationManagerCompat

    private lateinit var timerNotification: NotificationCompat.Builder

    private val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }





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

        when (intent?.action) {
            TimerActions.START.name -> start()
            TimerActions.STOP.name -> stop()
            TimerActions.COMPLETED.name -> sessionCompletedNotification()
        }



        return START_STICKY
    }

    fun start() {
        serviceScope.launch {
            countdownTimerManager.combinedFlow.collect { timerStates ->

                updateTimerNotification(timerStates)

                /* if(it==CountdownTimerManager.Phase.COMPLETED){
                     stopForeground(STOP_FOREGROUND_REMOVE)
                     stopSelf()
                 }*/

            }
        }
    }

    private fun sessionCompletedNotification() {
        val openTimerIntent = Intent(
            Intent.ACTION_VIEW,
            MY_URI.toUri(),
            applicationContext,
            MainActivity::class.java
        )
     val openTimerPendingIntent = PendingIntent.getActivity(
            applicationContext, 0, openTimerIntent, pendingIntentFlags
        )

        val timerCompletedNotification =
            NotificationCompat.Builder(applicationContext, TIMER_COMPLETED_CHANNEL_ID)
                .setContentTitle("Session Completed")
                .setContentText("Day Goal Reached, Bravo!")
                .setSmallIcon(R.drawable.baseline_timer_24)
                .setContentIntent(openTimerPendingIntent)
                .setAutoCancel(true)
                .build()
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
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
        startForeground(TIMER_COMPLETED_NOTIFICATION_ID, timerCompletedNotification)

    }

    /*fun stop() {
        serviceScope.cancel()
        countdownTimerManager.cancelCountdown()
        stopSelf()
    }*/
    fun stop() {
        notificationManager.cancel(TIMER_SERVICE_NOTIFICATION_ID)
    }

    private fun updateTimerNotification(timerState: CombinedData) {
        val minutes = timerState.timeLeftInMillis / 1000 / 60
        val seconds = timerState.timeLeftInMillis / 1000 % 60
        val formattedTime = String.format("%02d:%02d", minutes, seconds)

        val notificationUpdate = timerNotification
            .setContentTitle(timerState.currentPhase.name)
            .setContentText(formattedTime)
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
        //  startForeground(TIMER_SERVICE_NOTIFICATION_ID, notificationUpdate)
        notificationManager.notify(TIMER_SERVICE_NOTIFICATION_ID, notificationUpdate)


    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            /*   val timerServiceChannel = NotificationChannel(
                   TIMER_SERVICE_CHANNEL_ID,
                   getString(R.string.timer_service_channel_name),
                   NotificationManager.IMPORTANCE_DEFAULT

               )*/
            val timerServiceChannel = NotificationChannelCompat.Builder(
                TIMER_SERVICE_CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_HIGH
            ).setName(getString(R.string.timer_service_channel_name))
                .setDescription("Shows a notification as countdown occurs")
                .build()

            val timerCompletedChannel = NotificationChannelCompat.Builder(
                TIMER_COMPLETED_CHANNEL_ID,
                NotificationManagerCompat.IMPORTANCE_HIGH
            )
                .setName(applicationContext.getString(R.string.timer_completed_channel_name))
                .setDescription(applicationContext.getString(R.string.timer_completed_channel_description))
                .build()

            notificationManager.createNotificationChannelsCompat(
                listOf(
                    timerServiceChannel,
                    timerCompletedChannel,
                    //  rewardUnlockedChannel,
                )
            )
            //  notificationManager.createNotificationChannel(timerServiceChannel)

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

private const val TIMER_COMPLETED_CHANNEL_ID = "timer_completed_notification_channel"
private const val TIMER_SERVICE_CHANNEL_ID = "timer_service_channel"
private const val TIMER_SERVICE_NOTIFICATION_ID = 123
private const val TIMER_COMPLETED_NOTIFICATION_ID = -3