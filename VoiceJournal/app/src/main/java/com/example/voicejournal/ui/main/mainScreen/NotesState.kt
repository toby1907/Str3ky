package com.example.voicejournal.ui.main.mainScreen

import com.example.voicejournal.Data.model.VoiceJournal

class NotesState(
    val notes: List<VoiceJournal> = emptyList(),
    val noteIndex: Int = 0
)