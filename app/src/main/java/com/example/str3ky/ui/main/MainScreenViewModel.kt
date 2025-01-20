package com.example.str3ky.ui.main

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.str3ky.data.Goal
import com.example.str3ky.data.User
import com.example.str3ky.repository.GoalRepositoryImpl
import com.example.str3ky.repository.SettingsRepository
import com.example.str3ky.repository.UserRepositoryImpl
import com.example.str3ky.ui.snackbar.SnackbarAction
import com.example.str3ky.ui.snackbar.SnackbarController
import com.example.str3ky.ui.snackbar.SnackbarEvent
import com.example.str3ky.use_case.GoalUseCases
import com.example.str3ky.use_case.GoalsEvent
import com.example.str3ky.use_case.GoalsState
import com.example.str3ky.use_case.util.GoalOrder
import com.example.str3ky.use_case.util.OrderType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    private val goalUseCases: GoalUseCases
) : ViewModel() {


    private val existingUserFlow = MutableStateFlow(emptyList<User>())
    private val goalListFlow = MutableStateFlow(emptyList<Goal>())
    val goalList: StateFlow<List<Goal>> = goalListFlow

    private val _state = mutableStateOf(GoalsState())
    val state: State<GoalsState> = _state

    private var recentlyDeletedNote: Goal? = null

    private var getGoalsJob: Job? = null


    init {
        checkAndCreateUserGetList(GoalOrder.Date(OrderType.Descending))
    }

    private fun checkAndCreateUserGetList(goalOrder: GoalOrder) {

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
               goalUseCases.getGoals(goalOrder,user[0].id!!).collectLatest { goals ->
                _state.value = state.value.copy(
                    goals = goals,
                    goalOrder = goalOrder
                )
               }
             /* goalRepository.getGoalsForUser(user[0].id!!).collect {goal ->
                  goalListFlow.value = goal
                }*/



            }



            Log.d("goalUser","$user")
            Log.d("UserGoals","${goalListFlow.value}")
        }
    }


    fun onEvent(event: GoalsEvent) {
        when (event) {
            is GoalsEvent.Order -> {
                if (state.value.goalOrder::class == event.goalOrder::class &&
                    state.value.goalOrder.orderType == event.goalOrder.orderType
                ) {
                    return
                }
                checkAndCreateUserGetList(event.goalOrder)
            }
            is GoalsEvent.DeleteGoal -> {
                viewModelScope.launch {
                    goalUseCases.deleteGoal(event.goal)
                    recentlyDeletedNote = event.goal
                }
            }
            is GoalsEvent.RestoreNote -> {
                viewModelScope.launch {
                    goalUseCases.addGoal(recentlyDeletedNote ?: return@launch)
                    recentlyDeletedNote = null
                }
            }
            is GoalsEvent.ToggleOrderSection -> {
                _state.value = state.value.copy(
                    isOrderSectionVisible = !state.value.isOrderSectionVisible
                )
            }

        }
    }

    private fun deleteUser(vararg users: User)  {
      viewModelScope.launch  { userRepositoryImpl.deleteUsers(*users) }
    }

  /*  private fun getGoals(goalOrder: GoalOrder) {
        getGoalsJob?.cancel()
        getGoalsJob = goalUseCases.getGoals(goalOrder)
            .onEach { goals ->
                _state.value = state.value.copy(
                    goals = goals,
                    goalOrder = goalOrder
                )
            }
            .launchIn(viewModelScope)
    }*/
  fun showDeleteSnackbar() {
      viewModelScope.launch {
          SnackbarController.sendEvent(
              event = SnackbarEvent(
                  message = "Your Goal is deleted",
                    action = SnackbarAction(
                        name = "Undo",
                        action = {
                            onEvent(GoalsEvent.RestoreNote)
                            SnackbarController.sendEvent(
                                event = SnackbarEvent(
                                    message = "Goal restored"
                                )
                            )
                        }
                    )

              )
          )
      }
  }

}