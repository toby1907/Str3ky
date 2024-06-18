package com.example.str3ky.ui.session

data class SessionScreenState(
    var sessionTotalDurationMillis:Long = 0,
    var countdownTimeMillis:Long = 0,
    var breakDurationMillis: Long = 0,
    var totalSessions:Int = 0,
    var totalBreaks: Int = 0,
    var currentSession:Int = 0,
    var currentBreak:Int = 0,
  //  var sessions: List<SessionScreenViewModel.Session> = emptyList()
)