package com.example.str3ky

import com.example.str3ky.core.notification.TimerServiceManager
import com.example.str3ky.data.CountdownTimerManager
import com.example.str3ky.data.DayProgress
import com.example.str3ky.data.User
import com.example.str3ky.repository.GoalRepositoryImpl
import com.example.str3ky.repository.UserRepositoryImpl
import com.florianwalther.incentivetimer.core.notification.DefaultNotificationHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.launch
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class CountdownTimerManagerAccumulationAndUnlockTest {

    private val timerServiceManager: TimerServiceManager = mock()
    private val goalRepository: GoalRepositoryImpl = mock()
    private val notificationHelper: DefaultNotificationHelper = mock()
    private val userRepository: UserRepositoryImpl = mock()

    @Test
    fun `accumulateSessionHours updates dayProgress and persists user hours`() = runTest {
        val manager = CountdownTimerManager(
            timerServiceManager,
            goalRepository,
            notificationHelper,
            userRepository,
            externalScope = this
        )

        val user = User(
            id = 1,
            totalHoursSpent = 1.0,
            achievementsUnlocked = emptyList(),
            longestStreak = 0,
            currentStreak = 0,
            lastCompletedDate = 0L
        )
        whenever(userRepository.getUser()).thenReturn(flowOf(listOf(user)))

        val now = System.currentTimeMillis()
        manager.progressDate.value = now
        manager.dayProgressFlow.value = listOf(DayProgress(date = now, completed = false, hoursSpent = 0.0))

        // accumulate 0.5 hours
        manager.accumulateSessionHours(0.5)

        // advance test dispatcher so async persistence runs
        advanceUntilIdle()

        // dayProgressFlow should be updated synchronously
        val updated = manager.dayProgressFlow.value.find { it.date.toStartOfDayMillis() == now.toStartOfDayMillis() }
        assert(updated != null)
        assert(updated!!.hoursSpent == 0.5)

        // persisted user update happens asynchronously; now verify
        org.mockito.Mockito.verify(userRepository).update(any())
    }

    @Test
    fun `onDayChallengeCompleted triggers achievement unlocks and notifications`() = runTest {
        val manager = CountdownTimerManager(
            timerServiceManager,
            goalRepository,
            notificationHelper,
            userRepository,
            externalScope = this
        )

        val user = User(
            id = 2,
            totalHoursSpent = 10.0, // meets TIME_TRAVELER threshold
            achievementsUnlocked = emptyList(),
            longestStreak = 0,
            currentStreak = 0,
            lastCompletedDate = 0L
        )
        whenever(userRepository.getUser()).thenReturn(flowOf(listOf(user)))

        val now = System.currentTimeMillis()
        manager.progressDate.value = now
        // today's progress with some hours so it can be considered completed
        manager.dayProgressFlow.value = listOf(DayProgress(date = now, completed = false, hoursSpent = 1.0))

        // Trigger day completion which should call checkAndUnlockAchievements
        manager.onDayChallengeCompleted(true)

        // advance until all launched coroutines finish
        advanceUntilIdle()

        // Verify notification was posted
        org.mockito.Mockito.verify(notificationHelper).showAchievementUnlockedNotification(any())

        // Verify user save was called to persist unlocked achievements
        org.mockito.Mockito.verify(userRepository).save(any())

        // Optionally, assert unlockedAchievementsEvent emitted at least once
        // collect first emission (non-blocking because we've advanced the dispatcher)
        // Use a small local collector to check emission
        var emitted: List<com.example.str3ky.data.Achievement>? = null
        val job = launch {
            try {
                emitted = manager.unlockedAchievementsEvent.first()
            } catch (_: Exception) {
            }
        }
        advanceUntilIdle()
        job.cancel()
        if (emitted != null) {
            assert(emitted!!.isNotEmpty())
        }
    }
}
