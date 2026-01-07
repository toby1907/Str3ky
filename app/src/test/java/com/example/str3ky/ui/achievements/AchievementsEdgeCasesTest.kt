package com.example.str3ky.ui.achievements

import com.example.str3ky.data.User
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementsEdgeCasesTest {

    @Test
    fun `checkAchievements does not unlock when below thresholds`() {
        val user = User(
            id = 1,
            totalHoursSpent = 5.0,
            achievementsUnlocked = emptyList(),
            longestStreak = 1
        )

        val result = getNewlyUnlockedAchievements(user)
        val unlocked = result.filter { it.isUnlocked }

        assertTrue("No achievements should be unlocked for low hours/streak", unlocked.isEmpty())
    }

    @Test
    fun `checkAchievements does not reunlock already unlocked achievements`() {
        // Simulate user who already unlocked BEGINNER_STREAK
        val alreadyUnlocked = Achievements.BEGINNER_STREAK.copy(isUnlocked = true)
        val user = User(
            id = 1,
            totalHoursSpent = 20.0,
            achievementsUnlocked = listOf(alreadyUnlocked),
            longestStreak = 30
        )

        val result = getNewlyUnlockedAchievements(user)
        val newlyUnlocked = result.filter { it.isUnlocked && !user.achievementsUnlocked.any { u -> u.name == it.name } }

        // Time-based achievements may unlock, but BEGINNER_STREAK should not appear as newly unlocked
        assertFalse("BEGINNER_STREAK should not be considered newly unlocked", newlyUnlocked.any { it.name == Achievements.BEGINNER_STREAK.name })
    }
}
















