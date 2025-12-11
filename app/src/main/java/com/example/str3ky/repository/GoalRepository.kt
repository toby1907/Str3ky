package com.example.str3ky.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.str3ky.core.alarm.AlarmReceiver
import com.example.str3ky.data.DayProgress
import com.example.str3ky.data.Goal
import com.example.str3ky.data.GoalDao
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.Calendar
import java.util.concurrent.Executors

class GoalRepositoryImpl @Inject constructor(private val goalDao: GoalDao, @ApplicationContext private val context: Context) :
    GoalRepository {

    private val SINGLE_EXECUTOR = Executors.newSingleThreadExecutor()
    fun executeThread(f: () -> Unit) {
        SINGLE_EXECUTOR.execute(f)
    }

    override fun getGoal(id: Int): Flow<Goal?> {
        return goalDao.getLetter(id)
    }

    override suspend  fun delete(goal: Goal) = executeThread {
        goalDao.delete(goal)
    }

    override suspend fun save(goal: Goal,callback: (Int) -> Unit) {
        SINGLE_EXECUTOR.execute {
            val id = goalDao.insert(goal)
            callback(id.toInt())
        }
    }

    override suspend fun update(goal: Goal) = executeThread {
        goalDao.update(goal)
    }

    override fun getAllGoals(): Flow<List<Goal>> {
        return goalDao.getAllLetters()
    }

    override fun getGoalsForUser(userId: Int): Flow<List<Goal>> {
        return goalDao.getGoalsForUser(userId)
    }


    private val _alarmPermissionNeeded = MutableSharedFlow<Unit>()
    val alarmPermissionNeeded: SharedFlow<Unit> = _alarmPermissionNeeded.asSharedFlow()
    companion object {
        const val GOAL_ID_EXTRA = "goalId"
        const val PROGRESS_DATE_EXTRA = "progressDate"
        const val EXTRA_REQUEST_POST_NOTIFICATIONS = "request_post_notifications"
    }
    override fun scheduleRemindersForGoal(goal: Goal, dayProgressList: List<DayProgress>) {
        if (goal.alarmTime == null) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                _alarmPermissionNeeded.tryEmit(Unit)
                return
            }
        }

        for (dayProgress in dayProgressList) {
            scheduleReminder(goal, dayProgress)
        }
    }

    private fun scheduleReminder(goal: Goal, dayProgress: DayProgress) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(GOAL_ID_EXTRA, goal.id ?: return)
            putExtra(PROGRESS_DATE_EXTRA, dayProgress.date)
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            generatePendingIntentId(goal, dayProgress),
            intent,
            flags
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = dayProgress.date
            val alarmCalendar = Calendar.getInstance().apply {
                timeInMillis = goal.alarmTime!!
            }
            set(Calendar.HOUR_OF_DAY, alarmCalendar.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, alarmCalendar.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis < System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        Log.d("GoalRepository", "Alarm scheduled for: ${calendar.time}")
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            Log.e("GoalRepository", "SecurityException: ${e.message}")
        }
    }

    //remember to call this when you delete a goal or well goal is successful
    override fun cancelRemindersForGoal(goal: Goal, dayProgressList: List<DayProgress>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)

        for (dayProgress in dayProgressList) {
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                generatePendingIntentId(goal, dayProgress),
                intent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager.cancel(pendingIntent)
        }
    }

    override fun cancelReminderForDayProgress(goal: Goal, dayProgress: DayProgress) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            generatePendingIntentId(goal, dayProgress),
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun isActive(dayProgress: DayProgress): Boolean {
        val currentCalendar = Calendar.getInstance()
        val yourDateCalendar = Calendar.getInstance().apply { timeInMillis = dayProgress.date }

        val isActive = (currentCalendar.get(Calendar.YEAR) == yourDateCalendar.get(Calendar.YEAR)
                && currentCalendar.get(Calendar.MONTH) == yourDateCalendar.get(Calendar.MONTH)
                && currentCalendar.get(Calendar.DAY_OF_MONTH) == yourDateCalendar.get(Calendar.DAY_OF_MONTH))
        val showCheckMark = dayProgress.completed
        return isActive && !showCheckMark
    }

    private fun generatePendingIntentId(goal: Goal, dayProgress: DayProgress): Int {
        return (goal.id.toString() + dayProgress.date.toString()).hashCode()
    }

    fun rescheduleAllReminders() {
        val goals = goalDao.getAllGoalsBlocking()
        goals.forEach { goal ->
            goal.progress.filter { !it.completed && goal.alarmTime != null }.forEach { dayProgress ->
                scheduleReminder(goal, dayProgress)
            }
        }
    }

}
