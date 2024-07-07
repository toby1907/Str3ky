package com.example.voicejournal.ui.main.AddVoiceNote

import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.text.TextStyle
import com.example.voicejournal.Data.model.VoiceJournal

sealed class AddEditNoteEvent {

    data class EnteredTitle(val value: String): AddEditNoteEvent()
    data class EnteredDate(val value: Long): AddEditNoteEvent()
    data class ChangeTitleFocus(val focusState: FocusState): AddEditNoteEvent()
    data class EnteredContent(val value: String): AddEditNoteEvent()
    data class ChangeContentFocus(val focusState: FocusState): AddEditNoteEvent()
    data class Play(val filename:String): AddEditNoteEvent()
    data class Recording(val filename: String):AddEditNoteEvent()
    data class ChangeColor(val color: Int) : AddEditNoteEvent()
    data class DeleteJournal(val voiceJournal: VoiceJournal?): AddEditNoteEvent()
    data class ChangeStyle( val style: TextStyle): AddEditNoteEvent()
    data class Error(val message:String):AddEditNoteEvent()

    //   data class ChangeColor(val color: Int): AddEditNoteEvent()
    object SaveNote: AddEditNoteEvent()
    object StopPlay: AddEditNoteEvent()
    object StopRecording: AddEditNoteEvent()
    object RestoreJournal: AddEditNoteEvent()

}
