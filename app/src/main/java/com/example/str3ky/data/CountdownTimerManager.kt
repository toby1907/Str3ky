package com.example.str3ky.data

import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.str3ky.core.notification.TimerServiceManager
import com.example.str3ky.repository.GoalRepositoryImpl
import com.example.str3ky.ui.add_challenge_screen.GoalState
import com.example.str3ky.ui.nav.DONE_SCREEN
import com.example.str3ky.ui.nav.SESSION_SCREEN
import com.example.str3ky.ui.session.SessionScreenState
import com.florianwalther.incentivetimer.core.notification.DefaultNotificationHelper
import com.florianwalther.incentivetimer.core.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CountdownTimerManager @Inject constructor(
    val timerServiceManager: TimerServiceManager,
    val goalRepository: GoalRepositoryImpl,
    val notificationHelper: DefaultNotificationHelper,

    ) {
    private val scope = CoroutineScope(SupervisorJob())

    enum class Phase {
        FOCUS_SESSION, BREAK, COMPLETED
    }

    private val _timerState = MutableStateFlow<TimerState>(TimerState.Initial)
    val timerState: StateFlow<TimerState> = _timerState


    // Event to signal timer completion
    private val _timerFinishedEvent = MutableSharedFlow<Boolean>()
    val timerFinishedEvent: SharedFlow<Boolean> = _timerFinishedEvent.asSharedFlow()

    var popUpLambda: ((String, String) -> Unit)? = null
    var work: ((Boolean) -> Unit)? = null
    val goalId = MutableStateFlow(-1)


  /*  private val _progressDate = mutableStateOf(0L)
    val progressDate: State<Long> = _progressDate

    fun setProgressDate(date: Long) {
        _progressDate.value = date
    }*/



    private var countDownTimer: CountDownTimer? = null

    // Properties
    val _sessionTotalDurationMillis = MutableStateFlow(0L)
    val timeLeftInMillisFlow = MutableStateFlow(_sessionTotalDurationMillis.value)
    private val timeLeftInMillisForCombine: Flow<Long> = timeLeftInMillisFlow.asStateFlow()
    val timeLeftInMillis: Flow<Long> = timeLeftInMillisFlow
    //propeties for completion function
    var dayHourSpent = MutableStateFlow(0L)
    var sessionDuration = MutableStateFlow(-1)
    var progressDate =  MutableStateFlow(0L)
    var progressFlowCombine: Flow<Long> = progressDate.asStateFlow()
    val dayProgressFlow = MutableStateFlow(emptyList<DayProgress>())
    val dayProgress: StateFlow<List<DayProgress>> = dayProgressFlow
    val _goalState = MutableStateFlow(
        GoalState()
    )

    private val
            currentphase = MutableStateFlow(
        Phase.FOCUS_SESSION
    )
    val currentPhase: StateFlow<Phase> = currentphase
    val currentPhaseFlow: Flow<Phase> = currentPhase


    val _countdownTimeMillis = mutableStateOf(SessionScreenState())
    val _breakDurationMillis = mutableStateOf(SessionScreenState())


    // total no of pomo
    val _totalNoOfSessions = MutableStateFlow(0)
    val _totalNoOfBreaks = MutableStateFlow(0)

    //no of pomocompleted
    private val focusSetFlow = MutableStateFlow(0)
    val focusSet: Flow<Int> = focusSetFlow

    val totalFocusSetFlow = MutableStateFlow(_totalNoOfSessions.value)
    val totalFocusSet: Flow<Int> = totalFocusSetFlow

    private val breakSetFlow = MutableStateFlow(0)
    val breakSet: Flow<Int> = breakSetFlow

    val totalBreakSetFlow = MutableStateFlow(_totalNoOfBreaks.value)
    val totalBreakSet: Flow<Int> = totalBreakSetFlow

    val combinedFlow: Flow<CombinedData> = combine(
        //focusSetFlow,
        totalFocusSetFlow,
        goalId,
      //  totalBreakSetFlow,
        currentPhaseFlow,
        timeLeftInMillisForCombine,
        progressFlowCombine


        // Include currentphase as a flow
    ) { totalFocusSet,goalIdFlow, currentPhaseFlow, timeLeftInMillis,progressDateFlow ->
        CombinedData(
            totalFocusSet = totalFocusSet,
            currentPhase = currentPhaseFlow,
            timeLeftInMillis = timeLeftInMillis,
           goalId = goalIdFlow,
            progressDate = progressDateFlow
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(300),
        initialValue = CombinedData( 0,   Phase.COMPLETED, 0,-1,0)
    )
    private var _isSessionInProgress = mutableStateOf(
        false
    )
    private var _isBreakInProgress = mutableStateOf(
        false
    )
    private var isCompleted = mutableStateOf(
        false
    )
    val currentTimeTargetInMillisFlow = MutableStateFlow(_sessionTotalDurationMillis.value)
    val currentTimeTargetInMillis: Flow<Long> = currentTimeTargetInMillisFlow


    val sessionTotalDurationMillis: Flow<Long> = _sessionTotalDurationMillis
    val breakDurationMillis: State<SessionScreenState> = _breakDurationMillis
    val countdownTimeMillis: State<SessionScreenState> = _countdownTimeMillis

    val isSessionInProgress: State<Boolean> = _isSessionInProgress

    val isBreakInProgress: State<Boolean> = _isBreakInProgress

    val onSkipClickedFlow = MutableStateFlow(false)
    val onSkipClicked: StateFlow<Boolean> = onSkipClickedFlow

    init {

        _breakDurationMillis.value = _breakDurationMillis.value.copy(
            breakDurationMillis = 5000L
        )
    }

    private fun startCountDown(countDownTimeMillis: Long) {

        if (_sessionTotalDurationMillis.value == countDownTimeMillis) {
            focusSetFlow.value = focusSetFlow.value + 1
        }
        if (breakDurationMillis.value.breakDurationMillis == countDownTimeMillis) {
            breakSetFlow.value = breakSetFlow.value + 1
        }
        countDownTimer = object : CountDownTimer(countDownTimeMillis, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillisFlow.value = millisUntilFinished

            }

            override fun onFinish() {


                cancelCountdown()

                popUpLambda?.let { startNextPhase(it) }


                // Timer finished (e.g., perform some action)
                // You can emit this event using LiveData if needed
            }
        }.start()
        _isSessionInProgress.value = true

    }

    fun startSession(openAndPopUp: (String, String) -> Unit) {
        _timerState.value = TimerState.Running

        notificationHelper.removeTimerCompletedNotification()
        timerServiceManager.startTimerService()
        popUpLambda = openAndPopUp
        /*if (isCompleted.value) {
            currentphase.value = Phase.FOCUS_SESSION
            isCompleted.value = false
        }*/

        Log.d("TimeTarget", "${currentTimeTargetInMillisFlow.value}")
        startCountDown(currentTimeTargetInMillisFlow.value)

    }

    private fun startNextPhase(openAndPopUp: (String, String) -> Unit) {
        val currentPhase = currentPhase.value
        val sessionsCompleted = focusSetFlow.value
        val totalNoOfSessions = totalFocusSetFlow.value

        val nextPhase: Phase = when (currentPhase) {
            Phase.FOCUS_SESSION -> {
                if (sessionsCompleted >= totalNoOfSessions) {
                    Phase.COMPLETED
                } else if (onSkipClicked.value) {
                    Phase.FOCUS_SESSION
                } else {
                    Phase.BREAK
                }
            }

            Phase.BREAK -> {
                Phase.FOCUS_SESSION

            }

            Phase.COMPLETED -> {
                Phase.COMPLETED
            }
        }
        currentphase.value = nextPhase
        val nextTimeTarget = when (nextPhase) {
            Phase.FOCUS_SESSION -> {

                _sessionTotalDurationMillis.value
            }

            Phase.BREAK -> {

                _breakDurationMillis.value.breakDurationMillis
            }

            Phase.COMPLETED -> {
                _sessionTotalDurationMillis.value

            }
        }
        currentTimeTargetInMillisFlow.value = nextTimeTarget
        timeLeftInMillisFlow.value = nextTimeTarget
        if (sessionsCompleted >= totalNoOfSessions) {
            // Reset the timer and do not start the next session
            timeLeftInMillisFlow.value = _sessionTotalDurationMillis.value
            focusSetFlow.value = 0
            breakSetFlow.value = 0
            currentphase.value = Phase.FOCUS_SESSION
            isCompleted.value = true
            work?.invoke(true)

            timerServiceManager.stopTimerService()
            notificationHelper.showTimerCompletedNotification(currentPhase,goalId.value,progressDate.value,sessionDuration.value.toLong())


            /*popUpLambda?.invoke(
                DONE_SCREEN + "?goalId=${goalId}&sessionDuration=${_sessionTotalDurationMillis.value}&progressDate=${_progressDate.value}",
                SESSION_SCREEN
            )*/
            scope.launch {
                _timerFinishedEvent.emit(true)
            }

            return // Exit the function
        }
        startSession(openAndPopUp)
    }

    fun cancelCountdown() {
        timerServiceManager.stopTimerService()
        countDownTimer?.cancel()
        _isSessionInProgress.value = false


    }

    fun resetCountdown() {
        timerServiceManager.stopTimerService()
        countDownTimer?.cancel()
        timeLeftInMillisFlow.value = _sessionTotalDurationMillis.value
        focusSetFlow.value = 0
        breakSetFlow.value = 0
        isCompleted.value = true
        currentphase.value = Phase.FOCUS_SESSION
    }

    // Add a property to save the remaining time when paused
    private var remainingTimeMillis = 0L

    fun pauseCountdown() {
        timerServiceManager.stopTimerService()
        _timerState.value = TimerState.Paused

        // Save the remaining time and cancel the current timer
        remainingTimeMillis = timeLeftInMillisFlow.value
        cancelCountdown()
    }

    fun resumeCountdown(openAndPopUp: (String, String) -> Unit) {

        _timerState.value = TimerState.Running
        timerServiceManager.startTimerService()
        // Start a new countdown with the remaining time
        startCountDown(remainingTimeMillis)
    }


    fun onDayChallengeCompleted(work: (Boolean) -> Unit) {

    }

    //for notification
    fun startSession() {

        Log.d("TimeTarget", "${currentTimeTargetInMillisFlow.value}")
        startCountDown(currentTimeTargetInMillisFlow.value)

    }

    fun resumeCountdown() {
        startCountDown(remainingTimeMillis)
    }

    fun onDayChallengeCompleted(change: Boolean) {
        _timerState.value = TimerState.Initial
        dayHourSpent.value = sessionDuration.value.toLong()
        scope.launch {
            val progressList = dayProgressFlow.value.map { dayProgress ->
                if (dayProgress.date == progressDate.value) {
                    val updatedDayProgress = dayProgress.copy(
                        date = progressDate.value,
                        completed = if (dayProgress.hoursSpent >= sessionDuration.value.toLong()) change else false,
                        hoursSpent = dayProgress.hoursSpent + dayHourSpent.value
                    )
                    // Cancel the reminder if the day's goal is completed
                    if (updatedDayProgress.completed) {
                        _goalState.value.goal?.let { goal ->
                            goalRepository.cancelReminderForDayProgress(goal, updatedDayProgress)
                        }
                    }
                    updatedDayProgress
                } else dayProgress
            }
            _goalState.value.goal?.let { goal ->
                goalRepository.save(
                    goal.copy(
                        progress = progressList,
                        durationInfo = Duration(
                            countdownTime = goal.durationInfo.countdownTime + sessionDuration.value.toLong(),
                            isCompleted = sessionDuration.value.toLong() == goal.focusSet
                        )
                    )
                ){goalId ->
                    val goalWithId = goal.copy(id = goalId)

                }
                Log.d("DayHourSpentFromSession", "${dayHourSpent.value}")
            }
        }
    }

}

sealed class TimerState {
    object Initial : TimerState()
    object Running: TimerState()
    object Paused : TimerState()
    object Finished : TimerState()
}