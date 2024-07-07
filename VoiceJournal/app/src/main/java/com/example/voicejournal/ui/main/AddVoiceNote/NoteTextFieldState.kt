package com.example.voicejournal.ui.main.AddVoiceNote

data class NoteContentTextFieldState(
    val text: String? = "",
    val hint: String = "",
    val isHintVisible: Boolean = true,
    val created: Long = System.currentTimeMillis()
)
data class NoteTextFieldState(
    var text: String = "",
    val hint: String = "",
    val isHintVisible: Boolean = true
)
