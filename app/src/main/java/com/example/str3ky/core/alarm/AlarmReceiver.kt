package com.example.str3ky.core.alarm

import dagger.hilt.android.AndroidEntryPoint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.str3ky.MainActivity
import com.example.str3ky.R
import com.example.str3ky.repository.GoalRepositoryImpl

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {
    companion object {
        const val CHANNEL_ID = "reminder_channel"
        const val NOTIFICATION_ID = 100
    }

    override fun onReceive(context: Context, intent: Intent) {
        val goalId = intent.getIntExtra(GoalRepositoryImpl.GOAL_ID_EXTRA, -1)
        val progressDate = intent.getLongExtra(GoalRepositoryImpl.PROGRESS_DATE_EXTRA, 0L)
        if (goalId == -1) return
        createNotificationChannel(context)
        val notification = createNotification(context, goalId, progressDate)
        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) {
            val permissionIntent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("request_post_notifications", true)
            }
            context.startActivity(permissionIntent)
            return
        }
        notificationManager.notify(goalId, notification.build())
    }

    private fun createNotification(context: Context, goalId: Int, progressDate: Long): NotificationCompat.Builder {
        val deepLink = Uri.parse("myapp://progressscreen?goalId=${goalId}&progressDate=$progressDate")
        val openTimerIntent = Intent(
            Intent.ACTION_VIEW,
            deepLink,
            context,
            MainActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            goalId,
            openTimerIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.reminder_title))
            .setContentText(context.getString(R.string.reminder_body))
            .setSmallIcon(R.drawable.baseline_timer_24)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.reminder_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}















