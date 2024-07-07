package com.example.voicejournal.ui.main.AddVoiceNote

import com.example.voicejournal.ui.main.AddVoiceNote.components.Tag

data class NoteFileNameFieldState(
    val text: String = "",
    val imageFileUris: List<String>? = emptyList()
)
data class UriState(
    val imageFileUris: List<String>? = emptyList()
)
data class FavouriteState(
    val favourite: Boolean = false
)
data class TagState(
    var tags: List<Tag>? = emptyList()
)
