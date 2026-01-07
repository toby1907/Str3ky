package com.example.str3ky.data

import com.example.str3ky.core.notification.NotificationAdapter
import com.example.str3ky.data.Achievement
import com.example.str3ky.data.User
import com.example.str3ky.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CountdownTimerManagerAchievementTest {

    private class FakeUserRepository(initial: User) : UserRepository {
        private val _userFlow = MutableStateFlow(listOf(initial))

        override suspend fun save(user: User) {
            _userFlow.value = listOf(user)
        }

        override fun getUser(): Flow<List<User>> = _userFlow

        override suspend fun deleteUsers(vararg users: User) {
            // no-op
        }

        override suspend fun update(user: User) {
            _userFlow.value = listOf(user)
        }

        override suspend fun updateUserAtomically(transform: suspend (User) -> User) {
            val current = _userFlow.value.first()[0]
            val merged = runCatching { transform(current) }.getOrNull() ?: current
            _userFlow.value = listOf(merged)
        }

        override suspend fun addAchievementsAtomically(achievementsToAdd: List<Achievement>): List<Achievement> {
            val current = _userFlow.value.first()[0]
            val existingNames = current.achievementsUnlocked.map { it.name }.toSet()
            val actuallyToAdd = achievementsToAdd.filter { it.name !in existingNames }
            if (actuallyToAdd.isEmpty()) return emptyList()
            val merged = (current.achievementsUnlocked + actuallyToAdd).distinctBy { it.name }
            val updated = current.copy(achievementsUnlocked = merged)
            _userFlow.value = listOf(updated)
            return actuallyToAdd
        }
    }

    private class FakeNotificationAdapter : NotificationAdapter {
        val shown = mutableListOf<Achievement>()
        override fun showAchievementUnlockedNotification(achievement: Achievement) {
            shown.add(achievement)
        }

        override fun removeTimerServiceNotification() {}
        override fun removeTimerCompletedNotification() {}
    }

    @Test
    fun `accumulateSessionHours increases user hours and unlocks hour achievement`() = runTest {
        val testScope = TestScope(UnconfinedTestDispatcher(testScheduler))
        val initialUser = User(id = 1, totalHoursSpent = 9.0, achievementsUnlocked = emptyList(), longestStreak = 0, currentStreak = 0, lastCompletedDate = 0L)
        val repo = FakeUserRepository(initialUser)
        val notif = FakeNotificationAdapter()
        val manager = CountdownTimerManager(
            timerServiceAdapter = object: TimerServiceAdapter { override fun startTimerService(suppressNextFinished: Boolean) {} override fun startTimerService() {} override fun stopTimerService() {} },
            goalRepository = object: com.example.str3ky.repository.GoalRepository { /* minimal stub */
                override fun save(goal: com.example.str3ky.data.Goal, onSaved: (Long) -> Unit) {}
                override fun getGoals(): Flow<List<com.example.str3ky.data.Goal>> = flow { emit(emptyList()) }
                override fun cancelReminderForDayProgress(goal: com.example.str3ky.data.Goal, dayProgress: DayProgress) {}
                override suspend fun scheduleRemindersForGoal(goal: com.example.str3ky.data.Goal) {}
            },
            notificationAdapter = notif,
            userRepository = repo,
            externalScope = testScope
        )

        // accumulate 1 hour -> should reach 10 and unlock TIME_TRAVELER
        manager.progressDate.value = System.currentTimeMillis()
        manager.accumulateSessionHours(1.0)

        // give coroutines a moment
        testScheduler.advanceUntilIdle()

        val users = repo.getUser().first()
        assertEquals(10.0, users[0].totalHoursSpent, 0.001)
        // notification adapter should have been called for unlocked achievement
        assertEquals(1, notif.shown.size)
    }

    @Test
    fun `checkAndUnlockAchievements is idempotent`() = runTest {
        val testScope = TestScope(UnconfinedTestDispatcher(testScheduler))
        val initialUser = User(id = 1, totalHoursSpent = 50.0, achievementsUnlocked = emptyList(), longestStreak = 0, currentStreak = 0, lastCompletedDate = 0L)
        val repo = FakeUserRepository(initialUser)
        val notif = FakeNotificationAdapter()
        val manager = CountdownTimerManager(
            timerServiceAdapter = object: TimerServiceAdapter { override fun startTimerService(suppressNextFinished: Boolean) {} override fun startTimerService() {} override fun stopTimerService() {} },
            goalRepository = object: com.example.str3ky.repository.GoalRepository { override fun save(goal: com.example.str3ky.data.Goal, onSaved: (Long) -> Unit) {} override fun getGoals(): Flow<List<com.example.str3ky.data.Goal>> = flow { emit(emptyList()) } override fun cancelReminderForDayProgress(goal: com.example.str3ky.data.Goal, dayProgress: DayProgress) {} override suspend fun scheduleRemindersForGoal(goal: com.example.str3ky.data.Goal) {} },
            notificationAdapter = notif,
            userRepository = repo,
            externalScope = testScope
        )

        // call check twice
        manager.checkAndUnlockAchievements(repo.getUser().first()[0])
        testScheduler.advanceUntilIdle()
        manager.checkAndUnlockAchievements(repo.getUser().first()[0])
        testScheduler.advanceUntilIdle()

        // Only TIME_MASTER and TIME_TRAVELER (and maybe others) should have been added once each
        val users = repo.getUser().first()
        assertEquals(true, users[0].achievementsUnlocked.size >= 1)
        // Notification shows should not contain duplicates for same achievement
        val names = notif.shown.map { it.name }
        assertEquals(names.toSet().size, names.size)
    }
}

















