@file:Suppress("unused")

package com.example.str3ky.ui.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.str3ky.data.Achievement
import com.example.str3ky.data.CountdownTimerManager
import com.example.str3ky.data.User
import com.example.str3ky.repository.UserRepository
import com.example.str3ky.ui.achievements.getUpdatedAchievements
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AchievementViewModel @Inject constructor(
    private val countdownTimerManager: CountdownTimerManager
    , private val userRepository: UserRepository
) : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    private val _recentUnlocked = MutableStateFlow<List<Achievement>>(emptyList())
    val recentUnlocked: StateFlow<List<Achievement>> = _recentUnlocked.asStateFlow()

    // Unseen / unread count for badge
    private val _unseenCount = MutableStateFlow(0)
    val unseenCount: StateFlow<Int> = _unseenCount.asStateFlow()

    init {
        viewModelScope.launch {
            countdownTimerManager.unlockedAchievementsEvent.collect { list ->
                // Append to recent unlocked for the banner and increase unseen counter
                _recentUnlocked.value = list
                if (list.isNotEmpty()) {
                    _unseenCount.value = _unseenCount.value + list.size
                }
            }
        }

        // Observe user from repository and update achievements list
        viewModelScope.launch {
            userRepository.getUser().collect { users ->
                val u = users.firstOrNull()
                _user.value = u
                if (u != null) {
                    _achievements.value = getUpdatedAchievements(u)
                } else {
                    _achievements.value = emptyList()
                }
            }
        }
    }

    fun clear() {
        _recentUnlocked.value = emptyList()
    }

    // Mark all unlocked achievements as seen (clear unseen counter)
    fun markAllSeen() {
        _unseenCount.value = 0
        _recentUnlocked.value = emptyList()

        // Persist seen flag for all unlocked achievements in the user atomically
        viewModelScope.launch {
            try {
                userRepository.updateUserAtomically { current ->
                    val updated = current.copy(
                        achievementsUnlocked = current.achievementsUnlocked.map { it.copy(isSeen = true) }
                    )
                    updated
                }
            } catch (e: Exception) {
                // Best-effort; keep UI state cleared even if persistence fails
            }
        }
    }

    // Mark a single achievement as seen by name (persisted)
    fun markAchievementSeen(name: String) {
        viewModelScope.launch {
            try {
                userRepository.updateUserAtomically { current ->
                    val updatedList = current.achievementsUnlocked.map {
                        if (it.name == name) it.copy(isSeen = true) else it
                    }
                    current.copy(achievementsUnlocked = updatedList)
                }
                // reduce unseen count if present
                _unseenCount.value = (_unseenCount.value - 1).coerceAtLeast(0)
            } catch (e: Exception) {
                // ignore failures
            }
        }
    }
}
