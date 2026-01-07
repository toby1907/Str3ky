package com.example.str3ky.core.notification

import javax.inject.Inject

class DefaultTimerServiceAdapter @Inject constructor(
    private val manager: TimerServiceManager
) : TimerServiceAdapter {
    override fun startTimerService(suppressNextFinished: Boolean) {
        manager.startTimerService(suppressNextFinished)
    }

    override fun stopTimerService() {
        manager.stopTimerService()
    }
}

















