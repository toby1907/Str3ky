package com.example.str3ky

import com.example.str3ky.core.notification.TimerServiceManager
import com.example.str3ky.data.CountdownTimerManager
import com.example.str3ky.data.DayProgress
import com.example.str3ky.data.User
import com.example.str3ky.repository.GoalRepositoryImpl
import com.example.str3ky.repository.UserRepositoryImpl
import com.florianwalther.incentivetimer.core.notification.DefaultNotificationHelper
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class CountdownTimerManagerAchievementsTest {

    private lateinit var manager: CountdownTimerManager
    private val timerServiceManager: TimerServiceManager = mock()
    private val goalRepository: GoalRepositoryImpl = mock()
    private val notificationHelper: DefaultNotificationHelper = mock()
    private val userRepository: UserRepositoryImpl = mock()

    @Before
    fun setup() {
        manager = CountdownTimerManager(
            timerServiceManager,
            goalRepository,
            notificationHelper,
            userRepository
        )
    }

    @Test
    fun `onDayChallengeCompleted should update user only once when day transitions to completed`() = runBlocking {
        val user = User(
            id = 1,
            totalHoursSpent = 0.0,
            achievementsUnlocked = emptyList(),
            longestStreak = 0,
            currentStreak = 0,
            lastCompletedDate = 0L
        )
        whenever(userRepository.getUser()).thenReturn(flowOf(listOf(user)))

        // Set today's progress entry as not completed but with some hours
        val now = System.currentTimeMillis()
        manager.progressDate.value = now
        manager.dayProgressFlow.value = listOf(DayProgress(date = now, completed = false, hoursSpent = 1.0))

        // First call - should result in userRepository.update(...) once (as new completion)
        manager.onDayChallengeCompleted(true)
        // Verify asynchronously with Mockito timeout to wait for background coroutine to perform update
        org.mockito.Mockito.verify(userRepository, org.mockito.Mockito.timeout(1000).times(1)).update(any())

        // Second call - idempotent, should not call update again
        manager.onDayChallengeCompleted(true)
        // Still should only have been called once total
        org.mockito.Mockito.verify(userRepository, org.mockito.Mockito.timeout(500).times(1)).update(any())
    }

}
