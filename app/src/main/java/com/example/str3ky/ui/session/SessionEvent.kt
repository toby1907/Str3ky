package com.example.str3ky.ui.session

sealed class SessionEvent{
    data class Break(val value:Long): SessionEvent()
    data class Session(val value:Long): SessionEvent()
    object BreakSession: SessionEvent()
    object SessionTime: SessionEvent()


}