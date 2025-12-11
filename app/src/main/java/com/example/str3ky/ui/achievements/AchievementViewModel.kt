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

    init {
        viewModelScope.launch {
            countdownTimerManager.unlockedAchievementsEvent.collect { list ->
                _recentUnlocked.value = list
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
}
