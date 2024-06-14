package com.example.str3ky.ui.session

import android.os.CountDownTimer
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class SessionScreenViewModel
@Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    enum class Phase {
        FOCUS_SESSION, BREAK
    }

    // Properties
    private val _currentphase = MutableStateFlow(
       Phase.FOCUS_SESSION
    )

    private val _sessionTotalDurationMillis = MutableStateFlow(0L)
    private val _countdownTimeMillis = mutableStateOf(SessionScreenState())
    private val _breakDurationMillis = mutableStateOf(SessionScreenState())

    // total no of pomo
    private val _totalNoOfSessions = mutableStateOf(
        SessionScreenState()
    )
    private val _totalNoOfBreaks = mutableStateOf(
        SessionScreenState()
    )
    //no of pomocompleted
    private val _currentSession = mutableStateOf(
        SessionScreenState()
    )
    private val _currentBreak = mutableStateOf(
        SessionScreenState()
    )

    private var _isSessionInProgress = mutableStateOf(
        false
    )
    private var _isBreakInProgress = mutableStateOf(
        false
    )
   private val  currentTimeTargetInMillisFlow = MutableStateFlow(_sessionTotalDurationMillis.value)
    val currentTimeTargetInMillis: Flow<Long> = currentTimeTargetInMillisFlow
    private val timeLeftInMillisFlow = MutableStateFlow(_sessionTotalDurationMillis.value)
    val timeLeftInMillis: Flow<Long> = timeLeftInMillisFlow

    val sessionTotalDurationMillis: Flow<Long> = _sessionTotalDurationMillis
    val breakDurationMillis: State<SessionScreenState> = _breakDurationMillis
    val totalNoOfSessions: State<SessionScreenState> = _totalNoOfSessions
    val totalNoOfBreaks: State<SessionScreenState> = _totalNoOfBreaks
    val countdownTimeMillis: State<SessionScreenState> = _countdownTimeMillis
    val currentSession: State<SessionScreenState> = _currentSession
    val currentBreak: State<SessionScreenState> = _currentBreak

    val isSessionInProgress: State<Boolean> = _isSessionInProgress

    val isBreakInProgress: State<Boolean> = _isBreakInProgress
    val currentPhase: StateFlow<Phase> = _currentphase








    init {
        savedStateHandle.get<Int>("goalId")?.let { goalId ->
            if (goalId != -1) {

            }
        }
        savedStateHandle.get<Int>("totalSessions").let {
            if (it != -1) {
                if (it != null) {
                    _totalNoOfSessions.value = _totalNoOfSessions.value.copy(
                        totalSessions = it
                    )
                    _totalNoOfBreaks.value = _totalNoOfBreaks.value.copy(
                        totalBreaks = it - 1
                    )
                    Log.d("totalSessions", "${totalNoOfSessions.value} , ${totalNoOfBreaks.value}")
                }
            }
        }
        savedStateHandle.get<Int>("sessionDuration").let {
            if (it != -1) {
                if (it != null) {

                   _sessionTotalDurationMillis.value = 10000L
                /*    _countdownTimeMillis.value = _countdownTimeMillis.value.copy(
                        countdownTimeMillis = (it * 60000).toLong()
                    )*/
                    _breakDurationMillis.value = _breakDurationMillis.value.copy(
                        breakDurationMillis = 5000L
                    )
                    currentTimeTargetInMillisFlow.value = _sessionTotalDurationMillis.value
                    timeLeftInMillisFlow.value = _sessionTotalDurationMillis.value
                    Log.d("sessionDuration", "$it")

                }
            }

        }


       // startSession()
    }


    private var countDownTimer: CountDownTimer? = null

    // Initialize the timer with 30 seconds (30000 milliseconds)
    fun startCountDown(countDownTimeMillis: Long) {
        if (_sessionTotalDurationMillis.value==countDownTimeMillis){
            _currentSession.value= currentSession.value.copy(
                currentSession = +1
            )
        }
        if (breakDurationMillis.value.breakDurationMillis==countDownTimeMillis){
            _currentBreak.value = currentBreak.value.copy(
                currentBreak = +1
            )
        }
        countDownTimer = object : CountDownTimer(countDownTimeMillis, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillisFlow.value = millisUntilFinished
            }

            override fun onFinish() {


                cancelCountdown()
                startNextPhase()


                // Timer finished (e.g., perform some action)
                // You can emit this event using LiveData if needed
            }
        }.start()
       _isSessionInProgress.value = true
    }

    // Cancel the timer when it's no longer needed (e.g., when ViewModel is cleared)
    fun cancelCountdown() {
        countDownTimer?.cancel()
        _isSessionInProgress.value = false
    }


    // Start a new session or break
   fun startSession() {

Log.d("TimeTarget","${currentTimeTargetInMillisFlow.value}")
            startCountDown(currentTimeTargetInMillisFlow.value)







       /* if (totalNoOfSessions.value.totalSessions == 1) {
            _isSessionInProgress.value = true
            startCountDown(sessionTotalDurationMillis.value.sessionTotalDurationMillis)

        }

        if (totalNoOfSessions.value.totalSessions >= 2 && totalNoOfBreaks.value.totalBreaks == 0) {
            _isSessionInProgress.value = true
            startCountDown(sessionTotalDurationMillis.value.sessionTotalDurationMillis)
        }
        if (totalNoOfSessions.value.totalSessions >= 2 && totalNoOfBreaks.value.totalBreaks >= 1) {

            *//*for (session in 1..totalNoOfSessions.value.totalSessions) {
                if (!isBreakInProgress.value) {
                    _isSessionInProgress.value = true
                    _currentSession.value = _currentSession.value.copy(
                        currentSession = session
                    )
                    startCountDown(sessionTotalDurationMillis.value.sessionTotalDurationMillis)

                //    onEvent(SessionEvent.BreakSession)
                    if (session < totalNoOfSessions.value.totalSessions) {
                        _isBreakInProgress.value = true
                    }
                }
                if (isBreakInProgress.value) {
                      _breakDurationMillis.value = _breakDurationMillis.value.copy(
                     breakDurationMillis = 5 * 60000
                 )
                    _currentBreak.value = _currentBreak.value.copy(
                        currentBreak = session
                    )
                    startCountDown(breakDurationMillis.value.breakDurationMillis)
                    _isBreakInProgress.value = false
                }


            }*//*

            val sessions = (1..totalNoOfSessions.value.totalSessions).toList()
            val sessionMaptoLong = sessions.map {
                _sessionTotalDurationMillis.value.sessionTotalDurationMillis
            }
            val breaks = (1..totalNoOfSessions.value.totalSessions).toList()
            val breakMaptoLong = breaks.map {
               ( 5*60000).toLong()
            }
            val sessionsAndBreaksList = sessionMaptoLong.zip(breakMaptoLong) { sessionTime, breakTime ->
                Session(sessionTime, breakTime)
            }
            startSessionTimers(sessionsAndBreaksList)

        }*/

    }

    fun onEvent(event: SessionEvent) {

        when (event) {
            is SessionEvent.Break -> {

            }

            SessionEvent.BreakSession -> {

            //    startCountDown(_breakDurationMillis.value.breakDurationMillis)
            }

            is SessionEvent.Session -> {

            }

            SessionEvent.SessionTime -> {

            }
        }

    }

    //function from codeinflow
    private fun startNextPhase(){
        val  currentPhase = currentPhase.value
        val  sessionsCompleted = _currentSession.value.currentSession
        val  totalNoOfSessions = _totalNoOfSessions.value.totalSessions

        val nextPhase: Phase = when(currentPhase)
        {
            Phase.FOCUS_SESSION -> {
                Phase.BREAK /*if(sessionsCompleted < totalNoOfSessions) {
                    Phase.BREAK
                } */
            }
            Phase.BREAK -> {
                Phase.FOCUS_SESSION
            }

        }
        _currentphase.value = nextPhase
        val nextTimeTarget = when(nextPhase){
            Phase.FOCUS_SESSION -> _sessionTotalDurationMillis.value
            Phase.BREAK -> _breakDurationMillis.value.breakDurationMillis
        }
        currentTimeTargetInMillisFlow.value = nextTimeTarget
        timeLeftInMillisFlow.value = nextTimeTarget
     startSession()
    }

}
