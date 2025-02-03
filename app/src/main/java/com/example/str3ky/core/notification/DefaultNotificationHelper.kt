package com.florianwalther.incentivetimer.core.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.example.str3ky.MainActivity
import com.example.str3ky.R
import com.example.str3ky.core.notification.EXTRA_POMODORO_PHASE
import com.example.str3ky.core.notification.EXTRA_TIMER_RUNNING
import com.example.str3ky.core.notification.EXTRA_TIME_LEFT_IN_MILLIS
import com.example.str3ky.core.notification.TimerNotificationBroadcastReceiver
import com.example.str3ky.data.CountdownTimerManager
import com.example.str3ky.formatMillisecondsToTimeString
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultNotificationHelper @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
) : NotificationHelper {
    private val notificationManager = NotificationManagerCompat.from(applicationContext)

    private val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }


    private val openRewardListIntent = Intent(
        Intent.ACTION_VIEW,
        "https://www.incentivetimer.com/reward_list".toUri(),
        applicationContext,
        MainActivity::class.java
    )
    private val openRewardListPendingIntent = PendingIntent.getActivity(
        applicationContext, 0, openRewardListIntent, pendingIntentFlags
    )

    init {
        createNotificationChannels()
    }

    override fun getBaseTimerServiceNotification() =
        NotificationCompat.Builder(applicationContext, TIMER_SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_timer_24)
            .setSilent(true)
            .setOnlyAlertOnce(true)

    override fun updateTimerServiceNotification(
        currentPhase: CountdownTimerManager.Phase,
        timeLeftInMillis: Long,
        timerRunning: Boolean,
        goalId: Int,
        totalSessions: Int,
        sessionDuration:Int,
        progressDate: Long
    ) {
     //   val deepLink2 = Uri.parse("myapp://sessionscreen")
        val deepLink = Uri.parse("myapp://sessionscreen?goalId=${goalId}&totalSessions=${totalSessions}&sessionDuration=${sessionDuration}&progressDate=${progressDate}")
        val openTimerIntent = Intent(
            Intent.ACTION_VIEW,
            deepLink,
            applicationContext,
            MainActivity::class.java
        )
         val openTimerPendingIntent = PendingIntent.getActivity(
            applicationContext, 0, openTimerIntent, pendingIntentFlags
        )

        val actionIntent = getTimerNotificationActionIntent(
            currentPhase, timeLeftInMillis, timerRunning
        )

        val notificationUpdate = getBaseTimerServiceNotification()
            .setContentIntent(openTimerPendingIntent)
            .setContentTitle(currentPhase.name)
            .setContentText(formatMillisecondsToTimeString(timeLeftInMillis))
            .addAction(
                R.drawable.pause_24,
                applicationContext.getString(R.string.pause),
                actionIntent
            )
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
        notificationManager.notify(TIMER_SERVICE_NOTIFICATION_ID, notificationUpdate)
    }

    override fun showResumeTimerNotification(
        currentPhase: CountdownTimerManager.Phase,
        timeLeftInMillis: Long,
    ) {
        val actionIntent = getTimerNotificationActionIntent(
            currentPhase, timeLeftInMillis, timerRunning = false
        )

        val title = currentPhase.name +
                " (" + applicationContext.getString(R.string.paused) + ")"

        val notificationUpdate = getBaseTimerServiceNotification()
            .setContentTitle(title)
            .setContentText(formatMillisecondsToTimeString(timeLeftInMillis))
            .addAction(
                R.drawable.play_arrow_fill1_wght400_grad0_opsz24,
                applicationContext.getString(R.string.resume),
                actionIntent
            )
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
        notificationManager.notify(RESUME_TIMER_NOTIFICATION_ID, notificationUpdate)
    }

    private fun getTimerNotificationActionIntent(
        currentPhase: CountdownTimerManager.Phase,
        timeLeftInMillis: Long,
        timerRunning: Boolean,
    ): PendingIntent {
        val broadcastIntent =
            Intent(applicationContext, TimerNotificationBroadcastReceiver::class.java).apply {
                putExtra(EXTRA_POMODORO_PHASE, currentPhase)
                putExtra(EXTRA_TIME_LEFT_IN_MILLIS, timeLeftInMillis)
                putExtra(EXTRA_TIMER_RUNNING, timerRunning)
            }
        return PendingIntent.getBroadcast(
            applicationContext,
            0,
            broadcastIntent,
            pendingIntentFlags
        )
    }

    override fun showTimerCompletedNotification(finishedPhase: CountdownTimerManager.Phase,goalId: Int, progressDate: Long,sessionDuration: Long) {
        val title: Int
        val text: Int

        when (finishedPhase) {
            CountdownTimerManager.Phase.FOCUS_SESSION -> {
                title = R.string.pomodoro_completed_title
                text = R.string.pomodoro_completed_message
            }
            CountdownTimerManager.Phase.BREAK -> {
                title = R.string.break_over_title
                text = R.string.break_over_message
            }

            CountdownTimerManager.Phase.COMPLETED -> {
                title = R.string.pomodoro_completed_title
                text = R.string.pomodoro_completed_message

            }
        }

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
                .setContentTitle(applicationContext.getString(title))
                .setContentText(applicationContext.getString(text))
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

/*    override fun showRewardUnlockedNotification(reward: Reward) {
        val rewardUnlockedNotification =
            NotificationCompat.Builder(applicationContext, REWARD_UNLOCKED_CHANNEL_ID)
                .setContentTitle(applicationContext.getString(R.string.reward_unlocked))
                .setContentText(applicationContext.getString(R.string.reward_unlocked) + ": ${reward.name}")
                .setSmallIcon(R.drawable.ic_star)
                .setContentIntent(openRewardListPendingIntent)
                .setAutoCancel(true)
                .build()
        notificationManager.notify(reward.id.toInt(), rewardUnlockedNotification)
    }*/

    override fun removeTimerServiceNotification() {
        notificationManager.cancel(TIMER_SERVICE_NOTIFICATION_ID)
    }

    override fun removeTimerCompletedNotification() {
        notificationManager.cancel(TIMER_COMPLETED_NOTIFICATION_ID)
    }

    override fun removeResumeTimerNotification() {
        notificationManager.cancel(RESUME_TIMER_NOTIFICATION_ID)
    }

    private fun createNotificationChannels() {
        val timerServiceChannel = NotificationChannelCompat.Builder(
            TIMER_SERVICE_CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_DEFAULT
        )
            .setName(applicationContext.getString(R.string.timer_service_channel_name))
            .setDescription(applicationContext.getString(R.string.timer_service_channel_description))
            .setSound(null, null)
            .build()

        val timerCompletedChannel = NotificationChannelCompat.Builder(
            TIMER_COMPLETED_CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_HIGH
        )
            .setName(applicationContext.getString(R.string.timer_completed_channel_name))
            .setDescription(applicationContext.getString(R.string.timer_completed_channel_description))
            .build()

      /*  val rewardUnlockedChannel = NotificationChannelCompat.Builder(
            REWARD_UNLOCKED_CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_HIGH
        )
            .setName(applicationContext.getString(R.string.reward_unlocked_channel_name))
            .setDescription(applicationContext.getString(R.string.reward_unlocked_channel_description))
            .build()*/

        notificationManager.createNotificationChannelsCompat(
            listOf(
                timerServiceChannel,
                timerCompletedChannel,
              //  rewardUnlockedChannel,
            )
        )
    }
}

private const val TIMER_SERVICE_CHANNEL_ID = "timer_service_notification_channel"
private const val TIMER_COMPLETED_CHANNEL_ID = "timer_completed_notification_channel"
private const val REWARD_UNLOCKED_CHANNEL_ID = "reward_unlocked_notification_channel"
const val TIMER_SERVICE_NOTIFICATION_ID = -1
const val RESUME_TIMER_NOTIFICATION_ID = -2
private const val TIMER_COMPLETED_NOTIFICATION_ID = -3