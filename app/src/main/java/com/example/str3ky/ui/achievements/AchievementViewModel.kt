package com.example.str3ky.ui.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.str3ky.data.Achievement
import com.example.str3ky.data.User
import com.example.str3ky.repository.UserRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AchievementViewModel @Inject constructor(
    private val userRepository: UserRepositoryImpl
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    fun updateShowDialog(value: Boolean) {
        _showDialog.update { value }

    }

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
             userRepository.getUser().collect{ user ->
                _user.value = user.first()
                _achievements.value = getUpdatedAchievements(user[0])
            } // Assuming userId is 1 for now

        }
    }


}