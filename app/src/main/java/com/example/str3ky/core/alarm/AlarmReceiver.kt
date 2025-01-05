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
        val goalId = intent.getIntExtra(GoalRepositoryImpl.GOAL_ID_EXTRA, -1) // Retrieve goalId
        Log.d("Alarm Recieved faild","${intent.getIntExtra(GoalRepositoryImpl.GOAL_ID_EXTRA, 0)}")

        if (goalId != -1) {
            Log.d("Alarm Recieved","$goalId")
            createNotificationChannel(context)
            val notification = createNotification(context, goalId) // Pass goalId
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification.build())
        }
    }

    private fun createNotification(context: Context, goalId: Int): NotificationCompat.Builder {
        val deepLink = Uri.parse("myapp://progressscreen?goalId=${goalId}")
        val openTimerIntent = Intent(
            Intent.ACTION_VIEW,
            deepLink,
            context,
            MainActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
       /* val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }*/
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            openTimerIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Reminder")
            .setContentText("Don't forget to work on your goal today!")
            .setSmallIcon(R.drawable.baseline_timer_24)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Reminder Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}