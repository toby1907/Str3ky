package com.example.str3ky.core.notification

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.str3ky.data.CountdownTimerManager
import com.example.str3ky.di.ApplicationScope
import com.example.str3ky.millisecondsToMinutes
import com.florianwalther.incentivetimer.core.notification.DefaultNotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TimerService : Service() {

    @Inject
    lateinit var serviceScope: CoroutineScope

    @Inject
    lateinit var notificationHelper: DefaultNotificationHelper

    @Inject
    lateinit var countdownTimerManager: CountdownTimerManager
    private val coroutineScope  = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var timerJob: Job? = null

/*    private lateinit var notificationManager: NotificationManagerCompat

    private lateinit var timerNotification: NotificationCompat.Builder*/
/*

    private val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }
*/





    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(TIMER_SERVICE_NOTIFICATION_ID, notificationHelper.getBaseTimerServiceNotification().build())

        timerJob?.cancel()
        timerJob = coroutineScope.launch {
            countdownTimerManager.combinedFlow.collectLatest { timerStates ->

                notificationHelper.
                updateTimerServiceNotification(timerStates.currentPhase,timerStates.timeLeftInMillis,true,timerStates.goalId,timerStates.totalFocusSet,millisecondsToMinutes(timerStates.timeLeftInMillis),timerStates.progressDate)

                /* if(it==CountdownTimerManager.Phase.COMPLETED){
                     stopForeground(STOP_FOREGROUND_REMOVE)
                     stopSelf()
                 }*/

            }
        }
       /* notificationManager = NotificationManagerCompat.from(this)
        createNotificationChannel()
        timerNotification = NotificationCompat.Builder(this, TIMER_SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_timer_24)
            .setSilent(true)
            .setOnlyAlertOnce(true)*/

 /*goalId = countdownTimerManager.goalId
        val sessionDuration = countdownTimerManager.timeLeftInMillisFlow.value
        val progressDate = countdownTimerManager.progressDate.value
        when (intent?.action) {
            TimerActions.START.name -> {
                start()
                stopCompletedNotification()
            }
            TimerActions.STOP.name -> stop()
            TimerActions.COMPLETED.name -> {
                stop()
                sessionCompletedNotification(goalId, progressDate, sessionDuration)
            }
            TimerActions. CANCEL.name -> stopCompletedNotification()
        }*/



        return START_STICKY
    }

  /*  fun start() {

        serviceScope.launch {
            countdownTimerManager.combinedFlow.collect { timerStates ->

                updateTimerNotification(timerStates)

                *//* if(it==CountdownTimerManager.Phase.COMPLETED){
                     stopForeground(STOP_FOREGROUND_REMOVE)
                     stopSelf()
                 }*//*

            }
        }
    }*/

 /*   private fun sessionCompletedNotification( goalId: Int, progressDate: Long,sessionDuration: Long) {
        *//*val openTimerIntent = Intent(
            Intent.ACTION_VIEW,
            MY_URI.toUri(),
            applicationContext,
            MainActivity::class.java
        )*//*
        stop()
        val deepLink = Uri.parse("myapp://donescreen?goalId=$goalId&sessionDuration=$sessionDuration&progressDate=$progressDate")

        val openTimerIntent = Intent(
            Intent.ACTION_VIEW,
          deepLink
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
        notificationManager.notify(TIMER_COMPLETED_NOTIFICATION_ID, timerCompletedNotification)

    }
*/
    /*fun stop() {
        serviceScope.cancel()
        countdownTimerManager.cancelCountdown()
        stopSelf()
    }*/
   /* fun stop() {
        notificationManager.cancel(TIMER_SERVICE_NOTIFICATION_ID)
    }
    fun stopCompletedNotification(){
        notificationManager.cancel(TIMER_COMPLETED_NOTIFICATION_ID)
    }*/

    /*private fun updateTimerNotification(timerState: CombinedData) {

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


    }*/

    /*private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            *//*   val timerServiceChannel = NotificationChannel(
                   TIMER_SERVICE_CHANNEL_ID,
                   getString(R.string.timer_service_channel_name),
                   NotificationManager.IMPORTANCE_DEFAULT

               )*//*
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

    }*/

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
       // countdownTimerManager.cancelCountdown()
        notificationHelper.removeTimerServiceNotification()
    }
}

private const val TIMER_COMPLETED_CHANNEL_ID = "timer_completed_notification_channel"
private const val TIMER_SERVICE_CHANNEL_ID = "timer_service_channel"
private const val TIMER_SERVICE_NOTIFICATION_ID = 123
private const val TIMER_COMPLETED_NOTIFICATION_ID = -3