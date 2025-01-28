package com.example.str3ky.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.str3ky.repository.GoalRepositoryImpl
import com.example.str3ky.repository.UserRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val goalRepository: GoalRepositoryImpl,
    private val userRepository: UserRepositoryImpl
) : ViewModel() {

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    private val _launchAppSettings = MutableStateFlow(false)
    val launchAppSettings: StateFlow<Boolean> = _launchAppSettings.asStateFlow()

    fun updateShowDialog(value: Boolean) {
        _showDialog.update { value }

    }
    fun updateLaunchAppSettings(value: Boolean) {
        _launchAppSettings.value = value
    }

}