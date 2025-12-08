package com.example.str3ky.ui.achievements

import com.example.str3ky.data.Achievement
import com.example.str3ky.data.User
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementsTest {

    @Test
    fun `checkAchievements unlocks time achievement when hours threshold met`() {
        // Arrange: user with >=10 totalHoursSpent and no previously unlocked achievements
        val user = User(
            id = 1,
            totalHoursSpent = 10.0,
            achievementsUnlocked = emptyList(),
            longestStreak = 0
        )

        // Act
        val unlocked = getNewlyUnlockedAchievements(user)

        // Assert: TIME_TRAVELER (10 hours) should be among unlocked
        assertTrue(
            "Expected TIME_TRAVELER to be unlocked when totalHoursSpent >= 10",
            unlocked.any { it.name == Achievements.TIME_TRAVELER.name }
        )
    }

    @Test
    fun `checkAchievements unlocks streak achievement when streak threshold met`() {
        // Arrange: user with longestStreak >= 3 and no previously unlocked achievements
        val user = User(
            id = 1,
            totalHoursSpent = 0.0,
            achievementsUnlocked = emptyList(),
            longestStreak = 3
        )

        // Act
        val unlocked = getNewlyUnlockedAchievements(user)

        // Assert: BEGINNER_STREAK should be unlocked
        assertTrue(
            "Expected BEGINNER_STREAK to be unlocked when longestStreak >= 3",
            unlocked.any { it.name == Achievements.BEGINNER_STREAK.name }
        )
    }
}
