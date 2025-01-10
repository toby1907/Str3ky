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
        daysRemaining = 3 // Initial days remaining
    )
    val NOVICE_STREAK = Achievement(
        name = "Novice Streak",
        chanceInPercent = 100,
        iconKey = IconKey.TV,
        daysRemaining = 7 // Initial days remaining
    )
    val MASTER_STREAK = Achievement(
        name = "Master Streak",
        chanceInPercent = 100,
        iconKey = IconKey.PETS,
        daysRemaining = 30 // Initial days remaining
    )
    val TIME_TRAVELER = Achievement(
        name = "Time Traveler",
        chanceInPercent = 100,
        iconKey = IconKey.GROUP,
        hoursRemaining = 10 // Initial hours remaining
    )
    val TIME_MASTER = Achievement(
        name = "Time Master",
        chanceInPercent = 100,
        iconKey = IconKey.CAKE,
        hoursRemaining = 50 // Initial hours remaining
    )
    val TIME_LORD = Achievement(
        name = "Time Lord",
        chanceInPercent = 100,
        iconKey = IconKey.BEVERAGE,
        hoursRemaining = 100 // Initial hours remaining
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

fun checkAchievements(user: User): List<Achievement> {
    val unlockedAchievements = mutableListOf<Achievement>()
    val currentAchievements = user.achievementsUnlocked.toMutableList()
    val updatedAchievements = mutableListOf<Achievement>()

    // Streak-Based Achievements
    val beginnerStreak = calculateStreakAchievementProgress(user, BEGINNER_STREAK)
    val noviceStreak = calculateStreakAchievementProgress(user, NOVICE_STREAK)
    val masterStreak = calculateStreakAchievementProgress(user, MASTER_STREAK)

    if (user.longestStreak >= 3 && !currentAchievements.contains(BEGINNER_STREAK)) {
        unlockedAchievements.add(beginnerStreak.copy(isUnlocked = true))
    }
    if (user.longestStreak >= 7 && !currentAchievements.contains(NOVICE_STREAK)) {
        unlockedAchievements.add(noviceStreak.copy(isUnlocked = true))
    }
    if (user.longestStreak >= 30 && !currentAchievements.contains(MASTER_STREAK)) {
        unlockedAchievements.add(masterStreak.copy(isUnlocked = true))
    }

    // Hours-Based Achievements
    val timeTraveler = calculateTimeAchievementProgress(user, TIME_TRAVELER)
    val timeMaster = calculateTimeAchievementProgress(user, TIME_MASTER)
    val timeLord = calculateTimeAchievementProgress(user, TIME_LORD)

    if (user.totalHoursSpent >= 10 && !currentAchievements.contains(TIME_TRAVELER)) {
        unlockedAchievements.add(timeTraveler.copy(isUnlocked = true))
    }
    if (user.totalHoursSpent >= 50 && !currentAchievements.contains(TIME_MASTER)) {
        unlockedAchievements.add(timeMaster.copy(isUnlocked = true))
    }
    if (user.totalHoursSpent >= 100 && !currentAchievements.contains(TIME_LORD)) {
        unlockedAchievements.add(timeLord.copy(isUnlocked = true))
    }
    updatedAchievements.add(beginnerStreak)
    updatedAchievements.add(noviceStreak)
    updatedAchievements.add(masterStreak)
    updatedAchievements.add(timeTraveler)
    updatedAchievements.add(timeMaster)
    updatedAchievements.add(timeLord)

    return unlockedAchievements + updatedAchievements
}

fun calculateStreakAchievementProgress(user: User, achievement: Achievement): Achievement {
    val daysNeeded = achievement.daysRemaining ?: 0
    val daysRemaining = (daysNeeded - user.longestStreak).coerceAtLeast(0)
    return achievement.copy(daysRemaining = daysRemaining)
}

fun calculateTimeAchievementProgress(user: User, achievement: Achievement): Achievement {
    val hoursNeeded = achievement.hoursRemaining ?: 0
    val hoursRemaining = (hoursNeeded - user.totalHoursSpent).coerceAtLeast(0)
    return achievement.copy(hoursRemaining = hoursRemaining.toInt())
}
fun getUpdatedAchievements(user: User): List<Achievement> {
    val updatedAchievements = mutableListOf<Achievement>()

    for (achievement in Achievements.allAchievements) {
        val updatedAchievement = when {
            achievement.daysRemaining != null -> {
                val progress = calculateStreakAchievementProgress(user, achievement)
                progress.copy(isUnlocked = user.longestStreak >= (achievement.daysRemaining ?: 0))
            }
            achievement.hoursRemaining != null -> {
                val progress = calculateTimeAchievementProgress(user, achievement)
                progress.copy(isUnlocked = user.totalHoursSpent >= (achievement.hoursRemaining ?: 0))
            }
            else -> achievement // Should not happen, but just in case
        }
        updatedAchievements.add(updatedAchievement)
    }

    return updatedAchievements
}