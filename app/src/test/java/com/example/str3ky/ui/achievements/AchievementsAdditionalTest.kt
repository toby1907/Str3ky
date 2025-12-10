package com.example.str3ky.ui.achievements

import com.example.str3ky.data.User
import org.junit.Assert.*
import org.junit.Test

class AchievementsAdditionalTest {

    @Test
    fun `getNewlyUnlockedAchievements unlocks multiple achievements when thresholds met`() {
        // Arrange: user with high hours and long streak
        val user = User(
            id = 1,
            totalHoursSpent = 60.0,
            achievementsUnlocked = emptyList(),
            longestStreak = 30,
            currentStreak = 30,
            lastCompletedDate = 0L
        )

        // Act
        val newly = getNewlyUnlockedAchievements(user)

        // Assert: should include MASTER_STREAK and TIME_MASTER (50h) and TIME_TRAVELER (10h)
        val names = newly.map { it.name }.toSet()
        assertTrue(names.contains(Achievements.MASTER_STREAK.name))
        assertTrue(names.contains(Achievements.TIME_MASTER.name))
        assertTrue(names.contains(Achievements.TIME_TRAVELER.name))
    }

    @Test
    fun `calculateTimeAchievementProgress returns zero or negative remaining when threshold exceeded`() {
        val user = User(
            id = 2,
            totalHoursSpent = 12.0,
            achievementsUnlocked = emptyList(),
            longestStreak = 0,
            currentStreak = 0,
            lastCompletedDate = 0L
        )

        val timeTraveler = calculateTimeAchievementProgress(user, Achievements.TIME_TRAVELER)
        assertTrue("TIME_TRAVELER hoursRemaining should be <= 0 when user has >=10 hours", (timeTraveler.hoursRemaining ?: 0) <= 0)
    }

    @Test
    fun `calculateStreakAchievementProgress reports remaining days correctly`() {
        val user = User(
            id = 3,
            totalHoursSpent = 0.0,
            achievementsUnlocked = emptyList(),
            longestStreak = 2,
            currentStreak = 2,
            lastCompletedDate = 0L
        )

        val beginner = calculateStreakAchievementProgress(user, Achievements.BEGINNER_STREAK)
        assertEquals(1, beginner.daysRemaining)

        val novice = calculateStreakAchievementProgress(user, Achievements.NOVICE_STREAK)
        assertEquals(5, novice.daysRemaining)
    }
}
