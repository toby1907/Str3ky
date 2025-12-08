package com.florianwalther.incentivetimer.core.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
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
import com.example.str3ky.core.notification.EXTRA_FOCUS_COMPLETED
import com.example.str3ky.core.notification.EXTRA_BREAK_COMPLETED
import com.example.str3ky.core.notification.TimerNotificationBroadcastReceiver
import com.example.str3ky.data.CountdownTimerManager
import com.example.str3ky.data.Achievement
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


    init {
        createNotificationChannels()
    }

    override fun getBaseTimerServiceNotification() =
        NotificationCompat.Builder(applicationContext, TIMER_SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_timer_24)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setOngoing(true)

    override fun updateTimerServiceNotification(
        currentPhase: CountdownTimerManager.Phase,
        timeLeftInMillis: Long,
        timerRunning: Boolean,
        goalId: Int,
        totalSessions: Int,
        sessionDuration:Int,
        progressDate: Long,
        focusCompleted: Int,
        breakCompleted: Int
    ) {
        Log.d(TAG, "updateTimerServiceNotification: phase=$currentPhase timeLeft=$timeLeftInMillis goalId=$goalId totalSessions=$totalSessions focusCompleted=$focusCompleted breakCompleted=$breakCompleted")
        val deepLink = ("myapp://sessionscreen?goalId=${goalId}&totalSessions=${totalSessions}&sessionDuration=${sessionDuration}&progressDate=${progressDate}").toUri()
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
            currentPhase,
            timeLeftInMillis,
            timerRunning,
            focusCompleted,
            breakCompleted
        )

        // Short label for title: e.g., "Focus 1/2" or "Break 1/1"
        val shortTitle = when (currentPhase) {
            CountdownTimerManager.Phase.FOCUS_SESSION -> "Focus ${focusCompleted + if (timerRunning) 1 else 0}/${totalSessions}"
            CountdownTimerManager.Phase.BREAK -> "Break ${breakCompleted + if (timerRunning) 1 else 0}/${Math.max(1, totalSessions - 1)}"
            else -> currentPhase.name
        }

        val notificationUpdate = getBaseTimerServiceNotification()
            .setContentIntent(openTimerPendingIntent)
            .setContentTitle(shortTitle)
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
            return
        }
        Log.d(TAG, "posting service notification id=$TIMER_SERVICE_NOTIFICATION_ID")
        notificationManager.notify(TIMER_SERVICE_NOTIFICATION_ID, notificationUpdate)
    }

    override fun showResumeTimerNotification(
        currentPhase: CountdownTimerManager.Phase,
        timeLeftInMillis: Long,
        focusCompleted: Int,
        breakCompleted: Int,
    ) {
        // Do not show resume when completed
        if (currentPhase == CountdownTimerManager.Phase.COMPLETED) return

        val actionIntent = getTimerNotificationActionIntent(
            currentPhase,
            timeLeftInMillis,
            timerRunning = false,
            focusCompleted = focusCompleted,
            breakCompleted = breakCompleted
        )

        // Short paused title similar to running title
        val pausedTitle = when (currentPhase) {
            CountdownTimerManager.Phase.FOCUS_SESSION -> "Focus ${focusCompleted + 1}/${totalSessionsPlaceholder()}"
            CountdownTimerManager.Phase.BREAK -> "Break ${breakCompleted + 1}/${Math.max(1, totalSessionsPlaceholder()-1)}"
            else -> currentPhase.name + " (" + applicationContext.getString(R.string.paused) + ")"
        }

        val notificationUpdate = getBaseTimerServiceNotification()
            .setOngoing(false)
            .setAutoCancel(false)
            .setContentTitle(pausedTitle)
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
            return
        }
        Log.d(TAG, "posting resume notification id=$RESUME_TIMER_NOTIFICATION_ID")
        notificationManager.notify(RESUME_TIMER_NOTIFICATION_ID, notificationUpdate)
    }

    private fun getTimerNotificationActionIntent(
        currentPhase: CountdownTimerManager.Phase,
        timeLeftInMillis: Long,
        timerRunning: Boolean,
        focusCompleted: Int,
        breakCompleted: Int,
    ): PendingIntent {
        val broadcastIntent =
            Intent(applicationContext, TimerNotificationBroadcastReceiver::class.java).apply {
                putExtra(EXTRA_POMODORO_PHASE, currentPhase)
                putExtra(EXTRA_TIME_LEFT_IN_MILLIS, timeLeftInMillis)
                putExtra(EXTRA_TIMER_RUNNING, timerRunning)
                putExtra(EXTRA_FOCUS_COMPLETED, focusCompleted)
                putExtra(EXTRA_BREAK_COMPLETED, breakCompleted)
            }
        // Use distinct request codes: 1 for pause (running=true), 2 for resume (running=false)
        val requestCode = if (timerRunning) 1 else 2
        // Debug: log action details to help trace user interactions via notifications
        Log.d(TAG, "getTimerNotificationActionIntent: phase=$currentPhase timeLeft=$timeLeftInMillis running=$timerRunning focusCompleted=$focusCompleted breakCompleted=$breakCompleted requestCode=$requestCode")
        return PendingIntent.getBroadcast(
            applicationContext,
            requestCode,
            broadcastIntent,
            pendingIntentFlags
        )
    }

    override fun showTimerCompletedNotification(finishedPhase: CountdownTimerManager.Phase,goalId: Int, progressDate: Long,sessionDuration: Long) {
        Log.d(TAG, "showTimerCompletedNotification: finishedPhase=$finishedPhase goalId=$goalId progressDate=$progressDate sessionDuration=$sessionDuration")
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

        val deepLink = ("myapp://donescreen?goalId=$goalId&sessionDuration=$sessionDuration&progressDate=$progressDate").toUri()

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
            return
        }
        Log.d(TAG, "posting completed notification id=$TIMER_COMPLETED_NOTIFICATION_ID")
        notificationManager.notify(TIMER_COMPLETED_NOTIFICATION_ID, timerCompletedNotification)
    }

    override fun showAchievementUnlockedNotification(achievement: Achievement) {
        Log.d(TAG, "showAchievementUnlockedNotification: ${achievement.name}")

        val title = applicationContext.getString(R.string.reward_unlocked)
        val text = achievement.name

        val deepLink = ("myapp://achievements_screen").toUri()
        val openAchievementIntent = Intent(
            Intent.ACTION_VIEW,
            deepLink,
            applicationContext,
            MainActivity::class.java
        )
        val openPendingIntent = PendingIntent.getActivity(
            applicationContext, 0, openAchievementIntent, pendingIntentFlags
        )

        val notification = NotificationCompat.Builder(applicationContext, REWARD_UNLOCKED_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.baseline_timer_24)
            .setContentIntent(openPendingIntent)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val id = (achievement.name.hashCode() and Int.MAX_VALUE) % 100000
        Log.d(TAG, "posting achievement notification id=$id name=${achievement.name}")
        notificationManager.notify(id, notification)
    }

    override fun removeTimerServiceNotification() {
        Log.d(TAG, "removeTimerServiceNotification: id=$TIMER_SERVICE_NOTIFICATION_ID")
        notificationManager.cancel(TIMER_SERVICE_NOTIFICATION_ID)
    }

    override fun removeResumeTimerNotification() {
        Log.d(TAG, "removeResumeTimerNotification: id=$RESUME_TIMER_NOTIFICATION_ID")
        notificationManager.cancel(RESUME_TIMER_NOTIFICATION_ID)
    }

    override fun removeTimerCompletedNotification() {
        Log.d(TAG, "removeTimerCompletedNotification: id=$TIMER_COMPLETED_NOTIFICATION_ID")
        notificationManager.cancel(TIMER_COMPLETED_NOTIFICATION_ID)
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

        val rewardUnlockedChannel = NotificationChannelCompat.Builder(
            REWARD_UNLOCKED_CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_HIGH
        )
            .setName(applicationContext.getString(R.string.reward_unlocked))
            .setDescription(applicationContext.getString(R.string.timer_completed_channel_description))
            .build()

        notificationManager.createNotificationChannelsCompat(
            listOf(
                timerServiceChannel,
                timerCompletedChannel,
                rewardUnlockedChannel,
            )
        )
    }

    // Temporary placeholder helper to avoid changing signatures for callers that don't provide totals
    private fun totalSessionsPlaceholder(): Int {
        // We're unable to access totalSessions in this method signature; default to 1 to avoid division by zero
        return 1
    }
}

private const val TIMER_SERVICE_CHANNEL_ID = "timer_service_notification_channel"
private const val TIMER_COMPLETED_CHANNEL_ID = "timer_completed_notification_channel"
private const val REWARD_UNLOCKED_CHANNEL_ID = "reward_unlocked_notification_channel"
// Use the same ID as TimerService (123) to keep notifications consistent
const val TIMER_SERVICE_NOTIFICATION_ID = 123
const val RESUME_TIMER_NOTIFICATION_ID = -2
private const val TIMER_COMPLETED_NOTIFICATION_ID = -3
// Short logging tag to stay within Android Log limit (23 chars)
private const val TAG = "NotifHelper"
