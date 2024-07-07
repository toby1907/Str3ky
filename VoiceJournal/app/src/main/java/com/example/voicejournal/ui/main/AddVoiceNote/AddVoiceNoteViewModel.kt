package com.example.voicejournal.ui.main.AddVoiceNote

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicejournal.Data.model.InvalidNoteException
import com.example.voicejournal.Data.SettingsRepository
import com.example.voicejournal.Data.model.VoiceJournal
import com.example.voicejournal.Data.VoiceJournalRepositoryImpl

import com.example.voicejournal.ui.main.AddVoiceNote.components.Tag
import com.example.voicejournal.ui.theme.Variables
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private const val LOG_TAG = "AudioRecordTest"

@HiltViewModel
class AddVoiceNoteViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val voiceJournalRepository: VoiceJournalRepositoryImpl,
    private val settingsRepository: SettingsRepository,
    private val context: Context,
    ) : ViewModel()
{
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private val formatter = SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.getDefault())
    private val now = Date()
    private var recentlyDeletedJournal: VoiceJournal? = null
    // recentlyDeletedJournal to be implemented later TODO()


    private val _noteTitle = mutableStateOf(
        NoteTextFieldState(
            hint = "Enter title..."
        )
    )

    private val _noteFileName = mutableStateOf(
        NoteFileNameFieldState()
    )
    private val _tempImageUris = mutableStateOf(
        UriState()
    )
    private val _favourite = mutableStateOf(
        FavouriteState()
    )
    private var tempUris: List<String> = listOf()
    private var tempFileName: String = ""
    private val _imageUris = mutableStateOf(
        UriState()
    )
    private val _playNoteState = mutableStateOf(
        false
    )
    private val _doneButtonState = MutableStateFlow<Boolean>(false)

    private val _tags = mutableStateOf(emptyList<Tag>())


    private val _recordState = MutableStateFlow<Boolean>(false)
    private val _playingState = MutableStateFlow<Boolean>(false)

    // Timer for the Recorder Panel
    private val _timer = MutableStateFlow(0L)
    val timer = _timer.asStateFlow()

    private var timerJob: Job? = null

    //Timer for the Play panel
    private val _timer2 = MutableStateFlow(0L)
    val timer2 = _timer2.asStateFlow()

    private var timerJob2: Job? = null


    val noteTitle: State<NoteTextFieldState> = _noteTitle
    val noteFileName: State<NoteFileNameFieldState> = _noteFileName
    val tempImageUris: State<UriState> = _tempImageUris
    val favourite: State<FavouriteState> = _favourite
   // val imageUris: State<UriState> =  _imageUris
    val playNoteState: State<Boolean> = _playNoteState
    val recordState: StateFlow<Boolean> = _recordState
    val playingState: StateFlow<Boolean> = _playingState
    val doneButtonState: StateFlow<Boolean> = _doneButtonState

    // A state variable to store the list of tags
    val tags: State<List<Tag>> = _tags
//The content is used in place of the Title and the Title in place of the content
    private val _noteContent = mutableStateOf(
        NoteContentTextFieldState(
            hint = " "
        )
    )
    val noteContent: State<NoteContentTextFieldState> = _noteContent

    private  val _created = mutableStateOf(
        NoteContentTextFieldState(
            created = System.currentTimeMillis()
        )
    )

    val created: State<NoteContentTextFieldState> = _created


    private val _noteColor = mutableStateOf(
        Variables.SchemesSurface.toArgb()
    /*VoiceJournal.noteColors.random().toArgb()*/
    )
    private val _noteState = mutableStateOf(NoteState())
    val noteState: State<NoteState> = _noteState
    val noteColor: State<Int> = _noteColor


    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentNoteId: Int? = null


    init {
        savedStateHandle.get<Int>("noteId")?.let { noteId ->
            if (noteId != -1) {
                //_playNoteState.value = true
                viewModelScope.launch {
                    Log.d("NoteId", noteId.toString())
                    voiceJournalRepository.getNote(noteId).collect { note ->
                        _noteState.value = noteState.value.copy(voiceJournal = note)
                        if (note != null) {
                            currentNoteId = note.id
                        }
                        if (note != null) {
                            _noteTitle.value = _noteTitle.value.copy(text =note.title, isHintVisible = false )
                            Log.d("NoteTitleNew", note.title)
                        }
                        if (note != null) {
                            _noteContent.value = _noteContent.value.copy(
                                text = note.content,
                                isHintVisible = false)
                        }

                        if (note != null) {
                            _created.value = _created.value.copy(
                                created = note.created,
                                isHintVisible = false
                            )
                        }

                        if (note != null) {
                            _noteColor.value = note.color
                        }
                        if (note != null) {
                            tempFileName = note.fileName
                            _noteFileName.value = _noteFileName.value.copy(
                                text = tempFileName
                            )
                        }
                        if (note != null) {
                           if (note.imageUris?.isNotEmpty()== true) {
                               tempUris = note.imageUris!!
                               _tempImageUris.value = _tempImageUris.value.copy(
                                   imageFileUris = note.imageUris
                               )
                               Log.d("Image from file", "${note.imageUris}")
                           }
                        }
                        if (note != null) {
                            if (note.tags != null) {
                                _tags.value = note.tags!!
                            }
                        }
                        if (note != null) {
                           if(note.favourite!=null) {
                                _favourite.value = _favourite.value.copy(
                                    favourite = note.favourite!!
                                )
                            }

                        }

                    }

                }
            }
        }
        Log.d("_NoteTitle",_noteTitle.value.text)
        getSelectedImageUris()

    }


    // A function to update the checked state of a tag
    fun onTagChecked(tagName: String, isChecked: Boolean) {
        // Find the index of the tag with the given name
        val index = _tags.value.indexOfFirst { it.name == tagName }
        // If the tag is found, update its checked state
        if (index != -1) {
            _tags.value[index].isChecked = isChecked
        } else {
            // If the tag is not found, add it to the list and check it
            _tags.value = _tags.value + Tag(tagName, isChecked)
            // Sort the list by isChecked in descending order only when a new tag is added
            _tags.value = _tags.value.sortedByDescending { it.isChecked }
        }
    }

    fun onEvent(event: AddEditNoteEvent) {
        when (event) {
            is AddEditNoteEvent.EnteredTitle -> {
                _noteTitle.value = _noteTitle.value.copy(
                    text = event.value
                )

            }
            is AddEditNoteEvent.EnteredDate -> {
                _created.value = created.value.copy(
                    created = event.value
                )

            }
            is AddEditNoteEvent.ChangeTitleFocus -> {
                _noteTitle.value = noteTitle.value.copy(
                    isHintVisible = !event.focusState.isFocused &&
                            noteTitle.value.text.isBlank()
                )
            }

            is AddEditNoteEvent.EnteredContent -> {
                _noteContent.value = _noteContent.value.copy(
                    text = event.value
                )
            }

            is AddEditNoteEvent.ChangeContentFocus -> {
                _noteContent.value = _noteContent.value.copy(
                    isHintVisible = !event.focusState.isFocused &&
                            _noteContent.value.text!!.isBlank()
                )
            }

            is AddEditNoteEvent.SaveNote -> {

                viewModelScope.launch {
                    try {
                        voiceJournalRepository.save(
                            VoiceJournal(
                                title = noteTitle.value.text,
                                content = noteContent.value.text,
                                created = created.value.created,
                                fileName = noteFileName.value.text,
                                id = currentNoteId,
                                color = noteColor.value,
                                imageUris = tempImageUris.value.imageFileUris,
                                tags = tags.value,
                                favourite = favourite.value.favourite
                            )
                        )
                        _eventFlow.emit(UiEvent.SaveNote)
                    } catch (e: InvalidNoteException) {
                        Log.d("Note", "could not save")
                        _eventFlow.emit(
                            UiEvent.ShowSnackbar(
                                message = e.message ?: "Couldn't save note"
                            )
                        )
                    }
                }
                removeSelectedImageUris()
            }

            is AddEditNoteEvent.Play -> {
                viewModelScope.launch {
                    startPlaying()
                    _eventFlow.emit(UiEvent.PlayNote)
                }
            }

            is AddEditNoteEvent.Recording -> {
                viewModelScope.launch {
                    startRecording()
                    changeRecordState(true)
                    _eventFlow.emit(UiEvent.Recording)
                }
            }

            is AddEditNoteEvent.StopRecording -> {
                viewModelScope.launch {
                    stopRecording()
                    changeRecordState(false)
                    _eventFlow.emit(UiEvent.StopRecord)

                }
            }

            is AddEditNoteEvent.StopPlay -> {
                viewModelScope.launch {
                    stopPlaying()
                    _eventFlow.emit(UiEvent.StopPlay)
                }
            }

            is AddEditNoteEvent.ChangeColor -> {
                _noteColor.value = event.color
            }

            is AddEditNoteEvent.DeleteJournal -> {
                viewModelScope.launch {
                    event.voiceJournal?.let { voiceJournalRepository.delete(it) }
                    recentlyDeletedJournal = event.voiceJournal

                }
            }
            is AddEditNoteEvent.RestoreJournal -> {
                viewModelScope.launch {
                    voiceJournalRepository.save(recentlyDeletedJournal ?: return@launch)
                    recentlyDeletedJournal = null
                }
            }

            is AddEditNoteEvent.ChangeStyle -> {


            }
            is AddEditNoteEvent.Error ->{
               viewModelScope.launch {
                    _eventFlow.emit(UiEvent.ShowSnackbar(event.message))
                }
            }
        }
    }


    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object
        SaveNote : UiEvent()

        object PlayNote : UiEvent()
        object StopPlay : UiEvent()
        object Recording : UiEvent()
        object StopRecord : UiEvent()
    }

    data class NoteState(
        val voiceJournal: VoiceJournal? = null,
    )

    private suspend fun startPlaying() {

        player = MediaPlayer().apply {
            setOnCompletionListener {
                viewModelScope.launch { _eventFlow.emit(UiEvent.StopPlay) }
            }

            try {
                setDataSource(noteFileName.value.text)
                prepare()
                start()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }
        }

    }

    private suspend fun audioDuration(): Int {
        return withContext(Dispatchers.IO) {
              var player: MediaPlayer? = null
            try {
                player = MediaPlayer()
                player.setDataSource(noteFileName.value.text)
                player.prepare()
                return@withContext player.duration ?: 0 // return 0 if duration is null
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed", e)
                return@withContext 0 // return 0 if an error occurs
            } finally {
                player?.release() // close the player in the finally block
            }
        }
    }

    private fun stopPlaying() {
        try {

            player?.let {
                if (it.isPlaying) {
                    it.pause()
                    it.release()
                }
                player = null
            }
        } catch (e: IllegalStateException) {
            // Handle the exception (e.g., log it or show an error message)
        }
    }

    private fun startRecording() {
        recorder = MediaRecorder().apply {
            _noteFileName.value = _noteFileName.value.copy(
                text = "${
                    context.getDir(
                        "AudioJournal",
                        0
                    )?.absolutePath
                }/Recording+${formatter.format(now)}+.3gp"
            )
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(noteFileName.value.text)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()

            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }

            start()
        }
    }

    private fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }

    fun onCancelRecord(){
        _noteFileName.value = _noteFileName.value.copy(text = "")
        _playNoteState.value = false
    }
    private fun pauseRecording() {
        // Check the device's version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Pause the recording
            recorder?.pause()
        } else {
            // Pause is not supported on lower versions
            Toast.makeText(context, "Pause is not supported on this device", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun resumeRecording() {
        // Check the device's version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Pause the recording
            recorder?.resume()
        } else {
            // Pause is not supported on lower versions
            Toast.makeText(context, "Resume is not supported on this device", Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun changeRecordState(newState: Boolean) {
        //is to switch the recorder panel
        _recordState.value = newState
    }

    fun doneButtonState(newState: Boolean) {
        _doneButtonState.value = newState

    }

    fun noteFileName(imageFileUris: List<String>?) {
        _noteFileName.value = _noteFileName.value.copy(
            imageFileUris = imageFileUris
        )

    }

    fun getDuration(): Int {
        // use runBlocking to wait for the result
        return runBlocking {
            // use async to get a Deferred object
            val deferred = async {
                // use withContext to switch to IO dispatcher
                withContext(Dispatchers.IO) {
                    // call your audioDuration function
                    audioDuration()
                }
            }
            // use await to get the result from the Deferred object
            deferred.await()
        }
    }

    // Timer for Record panel
    fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _timer.value++
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
    }

    fun stopTimer() {
        _timer.value = 0
        timerJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        timerJob2?.cancel()
    }

    // Play panel Timer
    fun startTimer2() {
        // get the duration value from the audio file
        val duration = getDuration()
        // convert the duration value from milliseconds to seconds
        val durationInSeconds = duration / 1000
        timerJob2?.cancel()
        timerJob2 = viewModelScope.launch {
            while (true) {
                _playingState.value = true
                delay(1000)
                _timer2.value++
                // check if the timer value is equal to or greater than the duration value
                if (_timer2.value >= durationInSeconds) {
                    // stop the timer
                    _timer2.value = 0
                    timerJob2?.cancel()
                    _playingState.value = false
                    // break the loop
                    break
                }
            }
        }
    }

    fun pauseTimer2() {
        _playingState.value = false
        timerJob2?.cancel()
    }

    fun stopTimer2() {
        _timer2.value = 0
        _playingState.value = false
        timerJob2?.cancel()
    }

    fun getSelectedImageUris() {
        viewModelScope.launch {
            settingsRepository.getSelectedUris()
                .collect {
                    if (it.isNotEmpty()) {
                        // Add the new URIs to the existing list

                        _tempImageUris.value = _tempImageUris.value.copy(
                            imageFileUris = _tempImageUris.value.imageFileUris?.plus(it.toList())
                        )
                        Log.d("Temp fr get", "$tempUris")
                    }
                }
        }



    }

    fun removeSelectedImageUris() {
        viewModelScope.launch {
            settingsRepository.removeSelectedUris()
            _tempImageUris.value = _tempImageUris.value.copy(
                imageFileUris = tempUris
            )
            Log.d("Temp fr remove", "$tempUris")
        }

    }

    fun saveSelectedUris(selectedUris: List<String>) {
        viewModelScope.launch {
            settingsRepository.saveSelectedUris(selectedUris)
        }
    }


}
