package com.example.str3ky.ui.session

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.str3ky.data.CountdownTimerManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class SessionScreenViewModel
@Inject constructor(
    private val savedStateHandle: SavedStateHandle,

) : ViewModel() {

    val  countdownTimerManager = CountdownTimerManager()
    private val  currentTimeTargetInMillisFlow = MutableStateFlow(0L)
    val currentTimeTargetInMillis: Flow<Long> = currentTimeTargetInMillisFlow
    private val timeLeftInMillisFlow = MutableStateFlow(10000L)
    val timeLeftInMillis: Flow<Long> = timeLeftInMillisFlow
    private val
            currentphase = MutableStateFlow(
        CountdownTimerManager.Phase.FOCUS_SESSION
    )
    val currentPhase: StateFlow<CountdownTimerManager.Phase> = currentphase
private val pauseResumeStateFlow = MutableStateFlow(false)
    val pauseResumeState:StateFlow<Boolean> = pauseResumeStateFlow
    init {
        savedStateHandle.get<Int>("goalId")?.let { goalId ->
            if (goalId != -1) {

            }
        }
        savedStateHandle.get<Int>("totalSessions").let {
            if (it != -1) {
                if (it != null) {
                    viewModelScope.launch {
                        countdownTimerManager._totalNoOfSessions.value = it
                        countdownTimerManager.totalFocusSetFlow.value = it
                        countdownTimerManager.totalBreakSetFlow.value = if (it>1) it-1 else 0
                        countdownTimerManager._totalNoOfBreaks.value = if (it>1) it-1 else 0
                    }
                    /*_totalNoOfSessions.value = _totalNoOfSessions.value.copy(
                        totalSessions = it
                    )
                    _totalNoOfBreaks.value = _totalNoOfBreaks.value.copy(
                        totalBreaks = it - 1
                    )
                    Log.d("totalSessions", "${totalNoOfSessions.value} , ${totalNoOfBreaks.value}")*/
                }
            }
        }
        savedStateHandle.get<Int>("sessionDuration").let {
            if (it != -1) {
                if (it != null) {

                    viewModelScope.launch {
                            // Update the value
                            countdownTimerManager.currentTimeTargetInMillisFlow.value  = 10000L
                        countdownTimerManager.timeLeftInMillisFlow.value = 10000L
                        countdownTimerManager._sessionTotalDurationMillis.value = 10000L
                        Log.d("sessionInVMScope", "$it")
                    }

                /*    _countdownTimeMillis.value = _countdownTimeMillis.value.copy(
                        countdownTimeMillis = (it * 60000).toLong()
                    )*/

                    Log.d("sessionDuration", "$it")

                }
            }

        }
    }

    fun  startSession(openAndPopUp:(String,String)-> Unit){

        viewModelScope.launch {
countdownTimerManager.startSession(openAndPopUp)
        }
    }
    fun  cancelCountdown(){
        viewModelScope.launch {
            countdownTimerManager.resetCountdown()
        }
    }
    fun pauseResumeCountdown(state:Boolean,openAndPopUp:(String,String)-> Unit){

        if (state)
        viewModelScope.launch {
            countdownTimerManager.pauseCountdown()
        }
       else {
            viewModelScope.launch {
                countdownTimerManager.resumeCountdown(openAndPopUp)
            }
        }
    }










}
