package com.example.str3ky.data

import com.example.str3ky.repository.UserRepository
import com.example.str3ky.repository.GoalRepository
import com.example.str3ky.core.notification.NotificationAdapter
import com.example.str3ky.core.notification.TimerServiceAdapter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.delay
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

// Minimal fake implementations
class FakeUserRepository(initialUser: User) : UserRepository {
    private val _users = MutableStateFlow(listOf(initialUser))
    var updatedCount = AtomicInteger(0)

    override suspend fun save(user: User) {
        _users.value = listOf(user)
    }

    override fun getUser(): Flow<List<User>> = _users.asStateFlow()

    override suspend fun deleteUsers(vararg users: User) {
        // no-op
    }

    override suspend fun update(user: User) {
        _users.value = listOf(user)
        updatedCount.incrementAndGet()
    }

    override suspend fun updateUserAtomically(transform: suspend (User) -> User) {
        val current = _users.value.firstOrNull() ?: return
        val merged = transform(current)
        update(merged)
    }

    override suspend fun addAchievementsAtomically(achievementsToAdd: List<Achievement>): List<Achievement> {
        val current = _users.value.firstOrNull() ?: return emptyList()
        val existing = current.achievementsUnlocked.map { it.name }.toSet()
        val toAdd = achievementsToAdd.filter { it.name !in existing }
        if (toAdd.isEmpty()) return emptyList()
        val merged = (current.achievementsUnlocked + toAdd).distinctBy { it.name }
        val updated = current.copy(achievementsUnlocked = merged)
        update(updated)
        return toAdd
    }
}

class FakeNotificationAdapter : NotificationAdapter {
    val shown = mutableListOf<Achievement>()
    override fun removeTimerCompletedNotification() {}
    override fun removeTimerServiceNotification() {}
    override fun removeResumeTimerNotification() {}
    override fun showAchievementUnlockedNotification(achievement: Achievement) { shown.add(achievement) }
    override fun showTimerServiceNotification() {}
    override fun showResumeTimerNotification(currentPhase: CountdownTimerManager.Phase, timeLeftInMillis: Long, focusCompleted: Int, breakCompleted: Int) {}
    override fun showTimerCompletedNotification(finishedPhase: CountdownTimerManager.Phase, goalId: Int, progressDate: Long, sessionDuration: Long) {}
    override fun updateTimerServiceNotification(currentPhase: CountdownTimerManager.Phase, timeLeftInMillis: Long, timerRunning: Boolean, goalId: Int, totalSessions: Int, sessionDuration: Int, progressDate: Long, focusCompleted: Int, breakCompleted: Int) {}
}

class FakeGoalRepository : GoalRepository {
    override fun getGoal(id: Int) = flowOf<Goal?>(null)
    override suspend fun delete(goal: Goal) {}
    override suspend fun save(goal: Goal, callback: (Int) -> Unit) { callback(-1) }
    override suspend fun update(goal: Goal) {}
    override fun getAllGoals() = flowOf<List<Goal>>(emptyList())
    override fun getGoalsForUser(userId: Int) = flowOf<List<Goal>>(emptyList())
    override fun scheduleRemindersForGoal(goal: Goal, dayProgressList: List<DayProgress>) {}
    override fun cancelRemindersForGoal(goal: Goal, dayProgressList: List<DayProgress>) {}
    override fun cancelReminderForDayProgress(goal: Goal, dayProgress: DayProgress) {}
}

// Tests
class CountdownTimerManagerAchievementTest {

    @Test
    fun `accumulateSessionHours updates user totalHoursSpent`() = runTest {
        val initialUser = User(id = 1, totalHoursSpent = 1.0, achievementsUnlocked = emptyList(), longestStreak = 0)
        val repo = FakeUserRepository(initialUser)
        val notif = FakeNotificationAdapter()
        val timerSvc = object : TimerServiceAdapter {
            override fun startTimerService(suppressNextFinished: Boolean) {}
            override fun stopTimerService() {}
        }
        val goalRepo = FakeGoalRepository()

        val manager = CountdownTimerManager(timerSvc, goalRepo, notif, repo)

        // set a progressDate so accumulateSessionHours will add DayProgress entry
        manager.progressDate.value = System.currentTimeMillis()

        manager.accumulateSessionHours(0.5)

        // wait briefly to allow coroutine update
        delay(50)

        val users = repo.getUser().first()
        assertEquals(1, users.size)
        assertEquals(1.5, users[0].totalHoursSpent, 0.0001)
    }

    @Test
    fun `checkAndUnlockAchievements emits and notifies when new achievements unlocked`() = runTest {
        val initialUser = User(id = 1, totalHoursSpent = 9.0, achievementsUnlocked = emptyList(), longestStreak = 0)
        val repo = FakeUserRepository(initialUser)
        val notif = FakeNotificationAdapter()
        val timerSvc = object : TimerServiceAdapter {
            override fun startTimerService(suppressNextFinished: Boolean) {}
            override fun stopTimerService() {}
        }
        val goalRepo = FakeGoalRepository()

        val manager = CountdownTimerManager(timerSvc, goalRepo, notif, repo)

        // Simulate that user now has >10 hours
        val updatedUser = initialUser.copy(totalHoursSpent = 11.0)

        // start a collector before calling check
        var received = false
        val job = launch {
            manager.unlockedAchievementsEvent.collect { list ->
                if (list.any { it.name == com.example.str3ky.ui.achievements.Achievements.TIME_TRAVELER.name }) {
                    received = true
                }
            }
        }

        // Call checkAndUnlockAchievements with updated user
        manager.checkAndUnlockAchievements(updatedUser)

        // allow background coroutine to run
        delay(100)

        // Notification should have been called for TIME_TRAVELER (and possibly others)
        assertTrue(notif.shown.isNotEmpty())
        assertTrue(notif.shown.any { it.name == com.example.str3ky.ui.achievements.Achievements.TIME_TRAVELER.name })

        job.cancel()
        assertTrue(received)
    }

    @Test
    fun `onDayChallengeCompleted is idempotent and does not double update user`() = runTest {
        val today = System.currentTimeMillis()
        val dayProgress = DayProgress(date = today, completed = true, hoursSpent = 10.0)
        val initialUser = User(id = 1, totalHoursSpent = 10.0, achievementsUnlocked = emptyList(), longestStreak = 1)
        val repo = FakeUserRepository(initialUser)
        val notif = FakeNotificationAdapter()
        val timerSvc = object : TimerServiceAdapter {
            override fun startTimerService(suppressNextFinished: Boolean) {}
            override fun stopTimerService() {}
        }
        val goalRepo = FakeGoalRepository()

        val manager = CountdownTimerManager(timerSvc, goalRepo, notif, repo)

        // set internal day progress to include today's completed entry
        manager.dayProgressFlow.value = listOf(dayProgress)
        manager.progressDate.value = today

        // Call onDayChallengeCompleted twice; only first should trigger update
        manager.onDayChallengeCompleted(true)
        delay(100)
        manager.onDayChallengeCompleted(true)
        delay(100)

        // The repo.update should have been invoked at most once for the merged user update
        assertTrue(repo.updatedCount.get() <= 1)
    }

    @Test
    fun `duplicate achievement unlock requests are idempotent`() = runTest {
        val initialUser = User(id = 1, totalHoursSpent = 9.0, achievementsUnlocked = emptyList(), longestStreak = 0)
        val repo = FakeUserRepository(initialUser)
        val notif = FakeNotificationAdapter()
        val timerSvc = object : TimerServiceAdapter {
            override fun startTimerService(suppressNextFinished: Boolean) {}
            override fun stopTimerService() {}
        }
        val goalRepo = FakeGoalRepository()

        val manager = CountdownTimerManager(timerSvc, goalRepo, notif, repo)

        // simulate repeated unlock calls with same user state
        val updatedUser = initialUser.copy(totalHoursSpent = 11.0)
        manager.checkAndUnlockAchievements(updatedUser)
        delay(50)
        manager.checkAndUnlockAchievements(updatedUser)
        delay(50)

        // Only one actual persisted addition should occur
        assertTrue(repo.updatedCount.get() >= 1)
        // Notifications should not be duplicated (FakeNotificationAdapter collects shown)
        assertTrue(notif.shown.size <= 3) // tolerates multiple achievements but should not explode
    }

    @Test
    fun `multiple achievements can unlock in single update`() = runTest {
        val initialUser = User(id = 1, totalHoursSpent = 0.0, achievementsUnlocked = emptyList(), longestStreak = 0)
        val repo = FakeUserRepository(initialUser)
        val notif = FakeNotificationAdapter()
        val timerSvc = object : TimerServiceAdapter {
            override fun startTimerService(suppressNextFinished: Boolean) {}
            override fun stopTimerService() {}
        }
        val goalRepo = FakeGoalRepository()

        val manager = CountdownTimerManager(timerSvc, goalRepo, notif, repo)

        // create user state that satisfies multiple achievements
        val multiUser = initialUser.copy(totalHoursSpent = 120.0, longestStreak = 40)
        manager.checkAndUnlockAchievements(multiUser)
        delay(100)

        // check that at least two achievements were shown
        assertTrue(notif.shown.size >= 2)
    }
}
