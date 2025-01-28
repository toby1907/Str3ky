package com.example.str3ky.ui.add_challenge_screen

import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.text.TextStyle
import com.example.str3ky.data.Duration
import com.example.str3ky.data.Goal
import com.example.str3ky.data.Occurrence
import com.example.str3ky.data.OccurrenceSelection

sealed class AddChallengeEvent {
    data class EnteredName(val value: String): AddChallengeEvent()
    data class EnteredDescription(val value: String): AddChallengeEvent()
    data class EnteredDate(val value: Long): AddChallengeEvent()
    data class ChangeColor(val color: Int) : AddChallengeEvent()
    data class DeleteGoal(val goal: Goal?) : AddChallengeEvent()
    data class Error(val message:String) : AddChallengeEvent()
    data class FocusTime(val focusDuration:Duration) : AddChallengeEvent()
    data class AlarmTime(val time:Long) : AddChallengeEvent()
    data class Frequency(val frequency: OccurrenceSelection) : AddChallengeEvent()
    data class Completed(val completed: Boolean) : AddChallengeEvent()
    object SaveNote: AddChallengeEvent()
}
sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    object SaveNote : UiEvent()

}