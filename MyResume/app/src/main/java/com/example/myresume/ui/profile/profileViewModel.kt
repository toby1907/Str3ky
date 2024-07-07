package com.example.myresume.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ProfileViewModel:ViewModel() {
    private val _stateFlow = MutableSharedFlow<UiState>()
    val stateFlow = _stateFlow.asSharedFlow()
  init {
      viewModelScope.launch {
              onEvent(UiState.LightMode)

      }
  }


    fun onEvent (state: UiState){
        when(state){
            is UiState.DarkMode -> {
                viewModelScope.launch {
                    _stateFlow.emit(
                        UiState.DarkMode
                    )
                }
            }
            is UiState.LightMode -> {
                viewModelScope.launch {
                    _stateFlow.emit(
                        UiState.LightMode
                    )
                }
            }
        }
    }



    sealed class UiState{
        object DarkMode: UiState()
        object LightMode: UiState()
    }
}