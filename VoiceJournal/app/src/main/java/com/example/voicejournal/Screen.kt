package com.example.voicejournal

sealed class Screen(val route: String) {
    object VoicesScreen: Screen("voice_screen")
    object AddEditNoteScreen: Screen("add_edit_note_screen")
}