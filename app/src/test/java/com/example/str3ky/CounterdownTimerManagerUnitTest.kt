package com.example.str3ky


import com.example.str3ky.core.notification.TimerServiceManager
import com.example.str3ky.data.CountdownTimerManager
import com.example.str3ky.data.CountdownTimerManager.Phase
import com.example.str3ky.data.TimerState.Running
import com.example.str3ky.repository.GoalRepositoryImpl
import com.example.str3ky.repository.UserRepositoryImpl
import com.florianwalther.incentivetimer.core.notification.DefaultNotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class CountdownTimerManagerTest {

    private lateinit var countdownTimerManager: CountdownTimerManager
    private val timerServiceManager: TimerServiceManager = mock()
    private val goalRepository: GoalRepositoryImpl = mock()
    private val notificationHelper: DefaultNotificationHelper = mock()
    private val userRepository: UserRepositoryImpl = mock()
    private val testDispatcher = StandardTestDispatcher()
    private val testScheduler = TestCoroutineScheduler()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        countdownTimerManager = CountdownTimerManager(
            timerServiceManager,
            goalRepository,
            notificationHelper,
            userRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startSession should start timer service and update timer state to running`() = runTest {
        // Given
        val openAndPopUp: (String, String) -> Unit = { _, _ -> }
        val countDownTimeMillis = 5000L
        countdownTimerManager.currentTimeTargetInMillisFlow.value = countDownTimeMillis
        // When
        countdownTimerManager.startSession(openAndPopUp)
        // Then
        verify(timerServiceManager).startTimerService()
        assertEquals(Running, countdownTimerManager.timerState.value)
    }

    @Test
    fun `startSession should start countdown with correct time`() = runTest {
        // Given
        val openAndPopUp: (String, String) -> Unit = { _, _ -> }
        val countDownTimeMillis = 5000L
        countdownTimerManager.currentTimeTargetInMillisFlow.value = countDownTimeMillis
        // When
        countdownTimerManager.startSession(openAndPopUp)
        // Then
        assertEquals(countDownTimeMillis, countdownTimerManager.timeLeftInMillisFlow.value)
    }

    @Test
    fun `startSession should increment focus set when session starts`() = runTest {
        // Given
        val openAndPopUp: (String, String) -> Unit = { _, _ -> }
        val countDownTimeMillis = 5000L
        countdownTimerManager.currentTimeTargetInMillisFlow.value = countDownTimeMillis
        countdownTimerManager._sessionTotalDurationMillis.value = countDownTimeMillis
        // When
        countdownTimerManager.startSession(openAndPopUp)
        // Then
        assertEquals(1, countdownTimerManager.focusSet.first())
    }

    @Test
    fun `startNextPhase should switch to break phase after focus session`() = runTest {
        // Given
        val openAndPopUp: (String, String) -> Unit = { _, _ -> }
        val countDownTimeMillis = 5000L
        countdownTimerManager.currentTimeTargetInMillisFlow.value = countDownTimeMillis
        countdownTimerManager._sessionTotalDurationMillis.value = countDownTimeMillis
        // When
        countdownTimerManager.startSession(openAndPopUp)
        countdownTimerManager.startNextPhase(openAndPopUp)
        // Then
        assertEquals(Phase.BREAK, countdownTimerManager.currentPhase.value)
    }

    @Test
    fun `startNextPhase should switch to focus session phase after break`() = runTest {
        // Given
        val openAndPopUp: (String, String) -> Unit = { _, _ -> }
        val countDownTimeMillis = 5000L
        countdownTimerManager.currentTimeTargetInMillisFlow.value = countDownTimeMillis
        countdownTimerManager._sessionTotalDurationMillis.value = countDownTimeMillis
        // When
        countdownTimerManager.startSession(openAndPopUp)
        countdownTimerManager.startNextPhase(openAndPopUp)
        countdownTimerManager.startNextPhase(openAndPopUp)
        // Then
        assertEquals(Phase.FOCUS_SESSION, countdownTimerManager.currentPhase.value)
    }

    @Test
    fun `startNextPhase should switch to completed phase when all sessions are done`() = runTest {
        // Given
        val openAndPopUp: (String, String) -> Unit = { _, _ -> }
        val countDownTimeMillis = 5000L
        countdownTimerManager.currentTimeTargetInMillisFlow.value = countDownTimeMillis
        countdownTimerManager._sessionTotalDurationMillis.value = countDownTimeMillis
        countdownTimerManager.totalFocusSetFlow.value = 1
        // When
        countdownTimerManager.startSession(openAndPopUp)
        countdownTimerManager.startNextPhase(openAndPopUp)
        // Then
        assertEquals(Phase.COMPLETED, countdownTimerManager.currentPhase.value)
    }

    @Test
    fun `startNextPhase should reset timer and not start next session when completed`() = runTest {
        // Given
        val openAndPopUp: (String, String) -> Unit = { _, _ -> }
        val countDownTimeMillis = 5000L
        countdownTimerManager.currentTimeTargetInMillisFlow.value = countDownTimeMillis
        countdownTimerManager._sessionTotalDurationMillis.value = countDownTimeMillis
        countdownTimerManager.totalFocusSetFlow.value = 1
        // When
        countdownTimerManager.startSession(openAndPopUp)
        countdownTimerManager.startNextPhase(openAndPopUp)
        // Then
        assertEquals(countDownTimeMillis, countdownTimerManager.timeLeftInMillisFlow.value)
        assertEquals(0, countdownTimerManager.focusSet.first())
        assertEquals(0, countdownTimerManager.breakSet.first())
        assertEquals(Phase.FOCUS_SESSION, countdownTimerManager.currentPhase.value)
        assertTrue(countdownTimerManager.isCompleted.value)
        verify(notificationHelper).removeTimerServiceNotification()
        verify(notificationHelper).showTimerCompletedNotification(any(), any(), any(), any())
    }

    @Test
    fun `cancelCountdown should stop timer service and set session in progress to false`() = runTest {
        // Given
        val openAndPopUp: (String, String) -> Unit = { _, _ -> }
        countdownTimerManager.startSession(openAndPopUp)
        // When
        countdownTimerManager.cancelCountdown()
        // Then
        verify(timerServiceManager).stopTimerService()
        assertFalse(countdownTimerManager.isSessionInProgress.value)
    }

    @Test
    fun `resetCountdown should stop timer service, reset time, focus set, break set, and phase`() = runTest {
        // Given
        val openAndPopUp: (String, String) -> Unit = { _, _ -> }
        val countDownTimeMillis = 5000L
        countdownTimerManager.currentTimeTargetInMillisFlow.value = countDownTimeMillis
        countdownTimerManager._sessionTotalDurationMillis.value = countDownTimeMillis
        // When
        countdownTimerManager.startSession(openAndPopUp)
        countdownTimerManager.startNextPhase(openAndPopUp)
        countdownTimerManager.startNextPhase(openAndPopUp)
        // Then
        assertEquals(Phase.FOCUS_SESSION, countdownTimerManager.currentPhase.value)
    }




}