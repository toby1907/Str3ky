package com.example.str3ky.core.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.str3ky.repository.GoalRepositoryImpl
import com.example.str3ky.data.Goal
import com.example.str3ky.data.DayProgress
import com.example.str3ky.repository.UserRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var goalRepository: GoalRepositoryImpl

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        CoroutineScope(Dispatchers.IO).launch {
            goalRepository.rescheduleAllReminders()
        }
    }
}

