package com.example.str3ky.repository


import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.str3ky.core.alarm.AlarmReceiver
import com.example.str3ky.data.DayProgress
import com.example.str3ky.data.Goal
import com.example.str3ky.data.GoalDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.Calendar
import java.util.concurrent.Executors

class GoalRepositoryImpl(private val goalDao: GoalDao, private val context: Context) :
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
    }
    @RequiresApi(Build.VERSION_CODES.M)
    fun scheduleRemindersForGoal(goal: Goal, dayProgressList: List<DayProgress>) {
        if (goal.alarmTime == null) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e("GoalRepository", "Cannot schedule exact alarms")
                //callback.onExactAlarmPermissionNeeded()
                _alarmPermissionNeeded.tryEmit(Unit)
                return
            }
        }



        for (dayProgress in dayProgressList) {

                scheduleReminder(goal, dayProgress)

        }
    }
    @RequiresApi(Build.VERSION_CODES.M)
    private fun scheduleReminder(goal: Goal, dayProgress: DayProgress) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(GOAL_ID_EXTRA, goal.id)
            Log.d("this is the ID", "$goal.id")
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            generatePendingIntentId(goal, dayProgress), // Unique ID
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
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
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            Log.e("GoalRepository", "SecurityException: ${e.message}")
        }
    }

//remember to call this when you delete a goal or well goal is successful
    fun cancelRemindersForGoal(goal: Goal, dayProgressList: List<DayProgress>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)

        for (dayProgress in dayProgressList) {
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                generatePendingIntentId(goal, dayProgress), // Unique ID
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager.cancel(pendingIntent)
        }
    }

    fun cancelReminderForDayProgress(goal: Goal, dayProgress: DayProgress) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            generatePendingIntentId(goal, dayProgress), // Unique ID
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
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
        // Create a unique ID based on the goal ID and the dayProgress date
        return (goal.id.toString() + dayProgress.date.toString()).hashCode()
    }

}
