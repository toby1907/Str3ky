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
import com.example.str3ky.R

object Achievements {
    val BEGINNER_STREAK = Achievement(
        name = "Beginner Streak",
        chanceInPercent = 100,
        iconKey = IconKey.STAR,
        daysRemaining = 3, // Initial days remaining
        description = "Completed a 3-day streak — nice start! Keep the momentum going to earn bigger streak badges.",
        // use bronze medal for beginner
        badgeDrawableRes = R.drawable.medal_bronze
    )
    val NOVICE_STREAK = Achievement(
        name = "Novice Streak",
        chanceInPercent = 100,
        iconKey = IconKey.TV,
        daysRemaining = 7, // Initial days remaining
        description = "A full week of focused sessions — great dedication. Aim for longer streaks to unlock the Master badge.",
        // use silver medal for novice
        badgeDrawableRes = R.drawable.medal_silver
    )
    val MASTER_STREAK = Achievement(
        name = "Master Streak",
        chanceInPercent = 100,
        iconKey = IconKey.PETS,
        daysRemaining = 30, // Initial days remaining
        description = "30-day streak achieved! You're building a strong habit — outstanding work.",
        // use gold medal for master
        badgeDrawableRes = R.drawable.medal_gold
    )
    val TIME_TRAVELER = Achievement(
        name = "Time Traveler",
        chanceInPercent = 100,
        iconKey = IconKey.GROUP,
        hoursRemaining = 10, // Initial hours remaining
        description = "Accumulated 10 hours of focus time. Small wins add up — keep going.",
        // small trophy/star for time traveler
        badgeDrawableRes = R.drawable.trophy_star
    )
    val TIME_MASTER = Achievement(
        name = "Time Master",
        chanceInPercent = 100,
        iconKey = IconKey.CAKE,
        hoursRemaining = 50, // Initial hours remaining
        description = "50 hours of focused work — you're mastering your time. Great achievement.",
        // cup trophy for time master
        badgeDrawableRes = R.drawable.trophy_cup
    )
    val TIME_LORD = Achievement(
        name = "Time Lord",
        chanceInPercent = 100,
        iconKey = IconKey.BEVERAGE,
        hoursRemaining = 100, // Initial hours remaining
        description = "100 hours of focus completed — impressive dedication and consistency.",
        // special award graphic for the top-tier time lord
        badgeDrawableRes = R.drawable.award_6
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
    val hoursRemaining = (hoursNeeded - user.totalHoursSpent).coerceAtLeast(0.0)
    return achievement.copy(hoursRemaining = hoursRemaining.toInt())
}
fun getUpdatedAchievements(user: User): List<Achievement> {
    val updatedAchievements = mutableListOf<Achievement>()

    // Map existing unlocked achievements by name to preserve isSeen flag
    val seenMap = user.achievementsUnlocked.associateBy({ it.name }, { it.isSeen })

    for (achievement in Achievements.allAchievements) {
        val updatedAchievement = when {
            achievement.daysRemaining != null -> {
                val progress = calculateStreakAchievementProgress(user, achievement)
                val isUnlocked = user.longestStreak >= achievement.daysRemaining
                progress.copy(isUnlocked = isUnlocked, isSeen = seenMap[achievement.name] ?: false)
            }
            achievement.hoursRemaining != null -> {
                val progress = calculateTimeAchievementProgress(user, achievement)
                val isUnlocked = user.totalHoursSpent >= achievement.hoursRemaining
                progress.copy(isUnlocked = isUnlocked, isSeen = seenMap[achievement.name] ?: false)
            }
            else -> achievement.copy(isSeen = seenMap[achievement.name] ?: false) // Should not happen
        }
        updatedAchievements.add(updatedAchievement)
    }

    return updatedAchievements
}

fun getNewlyUnlockedAchievements(user: User): List<Achievement> {
    val currentNames = user.achievementsUnlocked.map { it.name }.toSet()
    val newlyUnlocked = mutableListOf<Achievement>()

    // Streak based
    if (user.longestStreak >= (BEGINNER_STREAK.daysRemaining ?: Int.MAX_VALUE) && !currentNames.contains(BEGINNER_STREAK.name)) {
        newlyUnlocked.add(BEGINNER_STREAK.copy(isUnlocked = true))
    }
    if (user.longestStreak >= (NOVICE_STREAK.daysRemaining ?: Int.MAX_VALUE) && !currentNames.contains(NOVICE_STREAK.name)) {
        newlyUnlocked.add(NOVICE_STREAK.copy(isUnlocked = true))
    }
    if (user.longestStreak >= (MASTER_STREAK.daysRemaining ?: Int.MAX_VALUE) && !currentNames.contains(MASTER_STREAK.name)) {
        newlyUnlocked.add(MASTER_STREAK.copy(isUnlocked = true))
    }

    // Hours based
    if (user.totalHoursSpent >= (TIME_TRAVELER.hoursRemaining?.toDouble() ?: Double.MAX_VALUE) && !currentNames.contains(TIME_TRAVELER.name)) {
        newlyUnlocked.add(TIME_TRAVELER.copy(isUnlocked = true))
    }
    if (user.totalHoursSpent >= (TIME_MASTER.hoursRemaining?.toDouble() ?: Double.MAX_VALUE) && !currentNames.contains(TIME_MASTER.name)) {
        newlyUnlocked.add(TIME_MASTER.copy(isUnlocked = true))
    }
    if (user.totalHoursSpent >= (TIME_LORD.hoursRemaining?.toDouble() ?: Double.MAX_VALUE) && !currentNames.contains(TIME_LORD.name)) {
        newlyUnlocked.add(TIME_LORD.copy(isUnlocked = true))
    }

    return newlyUnlocked
}
