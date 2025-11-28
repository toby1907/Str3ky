package com.example.str3ky.ui.achievements

import com.example.str3ky.data.Achievement
import com.example.str3ky.data.User
import com.example.str3ky.ui.achievements.Achievements.BEGINNER_STREAK
import com.example.str3ky.ui.achievements.Achievements.MASTER_STREAK
import com.example.str3ky.ui.achievements.Achievements.NOVICE_STREAK
import com.example.str3ky.ui.achievements.Achievements.TIME_LORD
import com.example.str3ky.ui.achievements.Achievements.TIME_MASTER
import com.example.str3ky.ui.achievements.Achievements.TIME_TRAVELER
import com.florianwalther.incentivetimer.core.ui.IconKey
import com.florianwalther.incentivetimer.core.ui.defaultRewardIconKey

object Achievements {
    val BEGINNER_STREAK = Achievement(
        name = "Beginner Streak",
        chanceInPercent = 100,
        iconKey = IconKey.STAR,
        daysRemaining = 3 // Initial days remaining - 3 day streak needed
    )
    val NOVICE_STREAK = Achievement(
        name = "Novice Streak",
        chanceInPercent = 100,
        iconKey = IconKey.TV,
        daysRemaining = 7 // Initial days remaining - 7 day streak needed
    )
    val MASTER_STREAK = Achievement(
        name = "Master Streak",
        chanceInPercent = 100,
        iconKey = IconKey.PETS,
        daysRemaining = 30 // Initial days remaining - 30 day streak needed
    )
    val TIME_TRAVELER = Achievement(
        name = "Time Traveler",
        chanceInPercent = 100,
        iconKey = IconKey.GROUP,
        hoursRemaining = 10 // Initial hours remaining - 10 hours needed
    )
    val TIME_MASTER = Achievement(
        name = "Time Master",
        chanceInPercent = 100,
        iconKey = IconKey.CAKE,
        hoursRemaining = 50 // Initial hours remaining - 50 hours needed
    )
    val TIME_LORD = Achievement(
        name = "Time Lord",
        chanceInPercent = 100,
        iconKey = IconKey.BEVERAGE,
        hoursRemaining = 100 // Initial hours remaining - 100 hours needed
    )

    val allAchievements = listOf(
        BEGINNER_STREAK,
        NOVICE_STREAK,
        MASTER_STREAK,
        TIME_TRAVELER,
        TIME_MASTER,
        TIME_LORD
    )
}

/**
 * Checks for newly unlocked achievements based on user progress.
 * Returns a list of newly unlocked achievements.
 */
fun checkAchievements(user: User): List<Achievement> {
    val newlyUnlockedAchievements = mutableListOf<Achievement>()
    val alreadyUnlockedNames = user.achievementsUnlocked.map { it.name }.toSet()

    // Streak-Based Achievements
    if (user.longestStreak >= 3 && !alreadyUnlockedNames.contains(BEGINNER_STREAK.name)) {
        val achievement = calculateStreakAchievementProgress(user, BEGINNER_STREAK)
        newlyUnlockedAchievements.add(achievement.copy(isUnlocked = true, daysRemaining = 0))
    }
    if (user.longestStreak >= 7 && !alreadyUnlockedNames.contains(NOVICE_STREAK.name)) {
        val achievement = calculateStreakAchievementProgress(user, NOVICE_STREAK)
        newlyUnlockedAchievements.add(achievement.copy(isUnlocked = true, daysRemaining = 0))
    }
    if (user.longestStreak >= 30 && !alreadyUnlockedNames.contains(MASTER_STREAK.name)) {
        val achievement = calculateStreakAchievementProgress(user, MASTER_STREAK)
        newlyUnlockedAchievements.add(achievement.copy(isUnlocked = true, daysRemaining = 0))
    }

    // Hours-Based Achievements
    if (user.totalHoursSpent >= 10 && !alreadyUnlockedNames.contains(TIME_TRAVELER.name)) {
        val achievement = calculateTimeAchievementProgress(user, TIME_TRAVELER)
        newlyUnlockedAchievements.add(achievement.copy(isUnlocked = true, hoursRemaining = 0))
    }
    if (user.totalHoursSpent >= 50 && !alreadyUnlockedNames.contains(TIME_MASTER.name)) {
        val achievement = calculateTimeAchievementProgress(user, TIME_MASTER)
        newlyUnlockedAchievements.add(achievement.copy(isUnlocked = true, hoursRemaining = 0))
    }
    if (user.totalHoursSpent >= 100 && !alreadyUnlockedNames.contains(TIME_LORD.name)) {
        val achievement = calculateTimeAchievementProgress(user, TIME_LORD)
        newlyUnlockedAchievements.add(achievement.copy(isUnlocked = true, hoursRemaining = 0))
    }

    return newlyUnlockedAchievements
}

fun calculateStreakAchievementProgress(user: User, achievement: Achievement): Achievement {
    val daysNeeded = achievement.daysRemaining ?: 0
    val daysRemaining = (daysNeeded - user.longestStreak).coerceAtLeast(0)
    return achievement.copy(daysRemaining = daysRemaining)
}

fun calculateTimeAchievementProgress(user: User, achievement: Achievement): Achievement {
    val hoursNeeded = achievement.hoursRemaining ?: 0
    val hoursRemaining = (hoursNeeded - user.totalHoursSpent).coerceAtLeast(0.0)
    return achievement.copy(hoursRemaining = hoursRemaining.toInt())
}

/**
 * Gets the full list of achievements with updated progress for display.
 * Returns all achievements with current progress and unlock status.
 */
fun getUpdatedAchievements(user: User): List<Achievement> {
    val alreadyUnlockedNames = user.achievementsUnlocked.map { it.name }.toSet()
    val updatedAchievements = mutableListOf<Achievement>()

    for (achievement in Achievements.allAchievements) {
        val updatedAchievement = when {
            achievement.daysRemaining != null -> {
                val progress = calculateStreakAchievementProgress(user, achievement)
                val isUnlocked = alreadyUnlockedNames.contains(achievement.name) || 
                                 user.longestStreak >= (achievement.daysRemaining ?: 0)
                progress.copy(isUnlocked = isUnlocked)
            }
            achievement.hoursRemaining != null -> {
                val progress = calculateTimeAchievementProgress(user, achievement)
                val isUnlocked = alreadyUnlockedNames.contains(achievement.name) || 
                                 user.totalHoursSpent >= (achievement.hoursRemaining ?: 0)
                progress.copy(isUnlocked = isUnlocked)
            }
            else -> achievement
        }
        updatedAchievements.add(updatedAchievement)
    }

    return updatedAchievements
}