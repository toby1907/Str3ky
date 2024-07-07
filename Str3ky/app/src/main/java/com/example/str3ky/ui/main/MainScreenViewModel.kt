package com.example.str3ky.ui.main

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.str3ky.data.Goal
import com.example.str3ky.data.User
import com.example.str3ky.repository.GoalRepositoryImpl
import com.example.str3ky.repository.SettingsRepository
import com.example.str3ky.repository.UserRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val goalRepository: GoalRepositoryImpl,
    private val userRepositoryImpl: UserRepositoryImpl,
    private val dataStore: SettingsRepository,
) : ViewModel() {

    private val existingUserFlow = MutableStateFlow(emptyList<User>())
    private val goalListFlow = MutableStateFlow(emptyList<Goal>())
    val goalList: StateFlow<List<Goal>> = goalListFlow

    init {
        checkAndCreateUserGetList()
    }

    private fun checkAndCreateUserGetList() {

        viewModelScope.launch {
val status = dataStore.checkFirstEntryStatus()
            if (status == false) {
                dataStore.updateEntryStatus()
                val user = User(
                    totalHoursSpent = 0L,
                    achievementsUnlocked = emptyList(),
                    longestStreak = 0
                )
                userRepositoryImpl.save(user)
            }

           val user =   userRepositoryImpl.getUser().first()

                existingUserFlow.value = user
           if (user[0].id!=null) {
              goalRepository.getGoalsForUser(user[0].id!!).collect {goal ->
                  goalListFlow.value = goal
                }



            }



            Log.d("goalUser","$user")
            Log.d("UserGoals","${goalListFlow.value}")
        }
    }

    private fun deleteUser(vararg users: User)  {
      viewModelScope.launch  { userRepositoryImpl.deleteUsers(*users) }
    }

}