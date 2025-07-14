package com.example.str3ky.data

import android.os.CountDownTimer
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.example.str3ky.core.notification.TimerServiceManager
import com.example.str3ky.millisecondsToMinutes
import com.example.str3ky.minutesToHours
import com.example.str3ky.repository.GoalRepositoryImpl
import com.example.str3ky.repository.UserRepositoryImpl
import com.example.str3ky.ui.achievements.checkAchievements
import com.example.str3ky.ui.add_challenge_screen.GoalState
import com.example.str3ky.ui.progress.toMinutes
import com.example.str3ky.ui.session.SessionScreenState
import com.florianwalther.incentivetimer.core.notification.DefaultNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CountdownTimerManager @Inject constructor(
    val timerServiceManager: TimerServiceManager,
    val goalRepository: GoalRepositoryImpl,
    val notificationHelper: DefaultNotificationHelper,
    private val userRepository: UserRepositoryImpl
    ) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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
    private var isSessionInProgressFlow =MutableStateFlow(false)
    private var _isBreakInProgress = mutableStateOf(
        false
    )
    var isCompleted = mutableStateOf(
        false
    )
    val currentTimeTargetInMillisFlow = MutableStateFlow(_sessionTotalDurationMillis.value)
    val currentTimeTargetInMillis: Flow<Long> = currentTimeTargetInMillisFlow


    val sessionTotalDurationMillis: Flow<Long> = _sessionTotalDurationMillis
    val breakDurationMillis: State<SessionScreenState> = _breakDurationMillis
    val countdownTimeMillis: State<SessionScreenState> = _countdownTimeMillis

    val isSessionInProgress: StateFlow<Boolean> = isSessionInProgressFlow

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
        isSessionInProgressFlow.value = true

    }

    fun startSession(openAndPopUp: (String, String) -> Unit) {
        _timerState.value = TimerState.Running

        notificationHelper.removeTimerCompletedNotification()
    scope.launch    {
        timerServiceManager.startTimerService()
    }
        popUpLambda = openAndPopUp
        /*if (isCompleted.value) {
            currentphase.value = Phase.FOCUS_SESSION
            isCompleted.value = false
        }*/

        Log.d("TimeTarget", "${currentTimeTargetInMillisFlow.value}")
        startCountDown(currentTimeTargetInMillisFlow.value)

    }

    fun startNextPhase(openAndPopUp: (String, String) -> Unit) {
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
            notificationHelper.removeTimerServiceNotification()
            notificationHelper.showTimerCompletedNotification(currentPhase,goalId.value,progressDate.value,sessionDuration.value.toLong())

            work?.invoke(true)
          //  onDayChallengeCompleted(true)



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
        isSessionInProgressFlow.value = false


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
        val sessionDurationMinutes = sessionDuration.value * _totalNoOfSessions.value.toLong()
Log.d("sessionDurationMinutes","${minutesToHours(sessionDuration.value.toLong())}")
        scope.launch {
            val totalBreakMins =  if (_totalNoOfBreaks.value!=0) _totalNoOfBreaks.value * 5 else 0
            val progressList = dayProgressFlow.value.map { dayProgress ->
                val hoursSpent = dayProgress.hoursSpent + minutesToHours(sessionDurationMinutes + totalBreakMins +1 )
                if (dayProgress.date == progressDate.value) {
                    val sessionsCompleted = focusSetFlow.value
                    val totalNoOfSessions = totalFocusSetFlow.value
                    dayProgress.copy(
                        date = progressDate.value,
                        completed = if ( hoursSpent >= minutesToHours( _goalState.value.goal?.focusSet?.toMinutes()!!)) change else false,
                        hoursSpent = hoursSpent
                    )

                } else dayProgress
            }
            Log.d("totalBreakMins","${_totalNoOfBreaks.value} ")
            // Check if the last DayProgress is completed
            val lastDayProgress = progressList.lastOrNull()
            if (lastDayProgress?.completed == true) {
                _goalState.value.goal?.let { goal ->
                    goalRepository.cancelReminderForDayProgress(goal, lastDayProgress)
                }
            }
            _goalState.value.goal?.let { goal ->
                goalRepository.save(
                    goal.copy(
                        progress = progressList,
                        durationInfo = Duration(
                            countdownTime = minutesToHours( sessionDurationMinutes + totalBreakMins) ,
                            // Todo() you need to  work on this
                            isCompleted = sessionDurationMinutes * _totalNoOfSessions.value == millisecondsToMinutes(goal.focusSet).toLong()
                        )
                    )
                ){goalId ->
                    val goalWithId = goal.copy(id = goalId)

                }
                Log.d("DayHourSpentFromSession", "$sessionDurationMinutes")
            }


            val user = userRepository.getUser().first()
            val previousTotalHours = user[0].totalHoursSpent
            val sessionDurationInHours = minutesToHours(sessionDurationMinutes+ totalBreakMins )
            val currentTotalHours = previousTotalHours + sessionDurationInHours
          //  val updatedTotalHours = user[0].totalHoursSpent.plus(sessionDurationMinutes/60) // Add to total hours
            val updatedUser = user[0].copy(totalHoursSpent = currentTotalHours) // Create a new user with updated data
            userRepository.update(updatedUser) // Update the user in the database
            checkAndUnlockAchievements(updatedUser) // Check for new achievements
            timerServiceManager.stopTimerService()
            Log.d("UserInCountdown", "$sessionDurationInHours")
            Log.d("updatedUser","${updatedUser.totalHoursSpent}")

        }

    }

    private fun checkAndUnlockAchievements(user: User) {

        val newAchievements = checkAchievements(user)
        if (newAchievements.isNotEmpty()) {
            // Update the user's achievements list
            val updatedUser = user.copy(achievementsUnlocked = user.achievementsUnlocked.plus(newAchievements).filter{
                it.isUnlocked
            } )
            scope.launch {
                userRepository.save(updatedUser)
            }
            // Optionally, notify the user about the new achievements
            Log.d("Achievements", "New achievements unlocked: $newAchievements")
        }
    }

}

sealed class TimerState {
    object Initial : TimerState()
    object Running: TimerState()
    object Paused : TimerState()
    object Finished : TimerState()
}