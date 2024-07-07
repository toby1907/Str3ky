package com.example.voicejournal.ui.main.AddVoiceNote

import android.net.Uri
import android.util.Log
import android.widget.DatePicker
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.example.voicejournal.R
import com.example.voicejournal.ui.main.AddVoiceNote.components.AlignStyleBottomSheet
import com.example.voicejournal.ui.main.AddVoiceNote.components.BottomAppPanel
import com.example.voicejournal.ui.main.AddVoiceNote.components.BottomSheet
import com.example.voicejournal.ui.main.AddVoiceNote.components.EditScreenTopAppBar
import com.example.voicejournal.ui.main.AddVoiceNote.components.FileChooser
import com.example.voicejournal.ui.main.AddVoiceNote.components.ImageBottomSheet
import com.example.voicejournal.ui.main.AddVoiceNote.components.PlayRecordPanel
import com.example.voicejournal.ui.main.AddVoiceNote.components.RecordPanelComponent
import com.example.voicejournal.ui.main.AddVoiceNote.components.SetStatusBarContentColor
import com.example.voicejournal.ui.main.AddVoiceNote.components.TagDialog
import com.example.voicejournal.ui.main.AddVoiceNote.components.TransparentHintTextField
import com.example.voicejournal.ui.theme.Variables
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun AddVoiceNoteScreen(
    navController: NavController,
    noteColor: Int,
    addVoiceNoteViewModel: AddVoiceNoteViewModel = hiltViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    note: String,
    snackbarHostState: SnackbarHostState
) {
    val state = rememberRichTextState()
    val scope = rememberCoroutineScope()

   LaunchedEffect(Unit) {
      // val titleState = addVoiceNoteViewModel.noteTitle.value.text
       state.setHtml(note)
       Log.d("htmlString", note)
   }

    LaunchedEffect(state.annotatedString) {
        addVoiceNoteViewModel.onEvent(AddEditNoteEvent.EnteredTitle(state.toHtml()))

    }




    val context = LocalContext.current
    //Date data
    val year: Int
    val month: Int
    val day: Int

    val calendar = Calendar.getInstance()
    year = calendar.get(Calendar.YEAR)
    month = calendar.get(Calendar.MONTH)
    day = calendar.get(Calendar.DAY_OF_MONTH)
    calendar.time = Date()
    val date = remember {
        mutableStateOf("$day/$month/$year")
    }


    val datePickerDialog = android.app.DatePickerDialog(
        context, { _: DatePicker, yearr: Int, monthh: Int, dayOfMonth: Int ->
            val selectedDateTime = Calendar.getInstance().apply {
                set(yearr, monthh, dayOfMonth)
            }.timeInMillis
            addVoiceNoteViewModel.onEvent(AddEditNoteEvent.EnteredDate(selectedDateTime))


        }, year, month, day
    )


    val lifecycle = lifecycleOwner.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                addVoiceNoteViewModel.removeSelectedImageUris()
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }

    }


    /* LaunchedEffect(Unit){
         addVoiceNoteViewModel.getSelectedImageUris()
     }*/
    val onSave = remember { mutableStateOf(false) }
    //record panel state
    val myState by addVoiceNoteViewModel.recordState.collectAsState()
    val timerValue by addVoiceNoteViewModel.timer.collectAsState()

    //auto focus
    val focusRequester = remember {
        FocusRequester()
    }
    //uris
    val uris = addVoiceNoteViewModel.tempImageUris.value.imageFileUris?.filter { it.isNotEmpty() }
        ?.map { Uri.parse(it) }

    // else addVoiceNoteViewModel.noteFileName.value.imageFileUris?.map { Uri.parse(it) }

    // A composable function to handle the back button press from the edit screen
    BackHandler(onBack = {
        // Remove the selected image URIs
        addVoiceNoteViewModel.removeSelectedImageUris()
        addVoiceNoteViewModel.onEvent(AddEditNoteEvent.StopPlay)

        // Navigate back to the main screen
        navController.popBackStack()
    })

    SetStatusBarContentColor(true)

    val contentState = addVoiceNoteViewModel.noteContent.value
    val fileNameState = addVoiceNoteViewModel.noteFileName.value

    //  Log.d("SetText1", titleState.text)

    val contentSaved = remember { mutableStateOf(false) }
    // val selectedImageUris = galleryScreenViewModel.selectedUris.collectAsState(initial = emptySet())

    /* LaunchedEffect( Unit) {

         state.setHtml(titleState.text)
         Log.d("SetText2", titleState.text)

     }*/




    val isImportant = remember { mutableStateOf(false) }


    val isImportants = remember { mutableStateOf(false) }

    val textStyle = if (addVoiceNoteViewModel.noteContent.value.text?.isEmpty() == true) {
        TextStyle(
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            fontStyle = FontStyle.Italic
        )
    } else {
        TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }


    val note = addVoiceNoteViewModel.noteState.value
    val playNoteState = addVoiceNoteViewModel.playNoteState.value
    var playState by remember { mutableStateOf(false) }
    val doneButton = remember {
        mutableStateOf(false)
    }

    val showDialog = remember { mutableStateOf(false) }

    // A function to show the dialog
    fun showDialog() {
        showDialog.value = true
    }

    // A function to hide the dialog
    fun hideDialog() {
        showDialog.value = false
    }

    var mediaState by remember {
        mutableStateOf(false)
    }
    var colorIntState by remember {
        mutableStateOf(Int.MAX_VALUE)
    }
    var cardVisibleState by remember {
        mutableStateOf(false)
    }


    var openBottomSheet by remember { mutableStateOf(false) }
    var openImageSheet by rememberSaveable {
        mutableStateOf(false)
    }
    var openAlignStyleBottomSheet by remember { mutableStateOf(false) }
    var fileChooserState by rememberSaveable {
        mutableStateOf(false)
    }

    fun showFileChooser() {
        fileChooserState = true
    }

    fun hideFileChooser() {
        fileChooserState = false
    }

    val skipPartiallyExpanded by remember { mutableStateOf(false) }
    val edgeToEdgeEnabled by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    val ImageSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    val alignStylebottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )

    val noteBackgroundAnimatable = remember {
        Animatable(
            Color(if (noteColor != -1) noteColor else addVoiceNoteViewModel.noteColor.value)
        )
    }

    val doneButtonState = addVoiceNoteViewModel.doneButtonState.collectAsState()
    LaunchedEffect(key1 = true) {

        addVoiceNoteViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AddVoiceNoteViewModel.UiEvent.Recording -> {

                }

                is AddVoiceNoteViewModel.UiEvent.StopRecord -> {

                }

                is AddVoiceNoteViewModel.UiEvent.PlayNote -> {
                    playState = true
                }

                is AddVoiceNoteViewModel.UiEvent.StopPlay -> {
                    mediaState = true
                }

                is AddVoiceNoteViewModel.UiEvent.SaveNote -> {
                    onSave.value = true
                    navController.navigateUp()
                    scope.launch  {
                        snackbarHostState.showSnackbar(
                            message = "Journal Saved"
                        )
                    }
                }

                is AddVoiceNoteViewModel.UiEvent.ShowSnackbar -> {
                   scope.launch {
                        snackbarHostState.showSnackbar(
                            message = event.message
                        )
                    }
                }
            }
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.primary
    ) {
        Scaffold(modifier = Modifier.imePadding(),
            topBar = {
                /*  SmallTopAppBar(
                      title = {
                          Text(
                              if (titleState.text != "") "Edit Journal" else "New Journal",
                              maxLines = 1,
                              overflow = TextOverflow.Ellipsis
                          )
                      },
                      navigationIcon = {
                          IconButton(onClick = { navController.navigate(Screen.VoicesScreen.route) }) {
                              Icon(
                                  imageVector = Icons.Filled.Close,
                                  contentDescription = "Cancel"
                              )
                          }
                      },
                      actions = {

                          IconButton(onClick = {
                              addVoiceNoteViewModel.onEvent(AddEditNoteEvent.DeleteJournal(note.voiceJournal))
                              scope.launch {
                                  navController.navigate(Screen.VoicesScreen.route)
                                  val result = scaffoldState.snackbarHostState.showSnackbar(
                                      message = "Note deleted",
                                      actionLabel = "Undo"
                                  )*//*
                                if(result == SnackbarResult.ActionPerformed) {
                                    viewModel.onEvent(NotesEvent.RestoreNote)
                                }*//*
                            }
                        }) {
                            if (titleState.text != "") {
                                Icon(
                                    painter = painterResource(R.drawable.ic_baseline_delete_forever_24),
                                    contentDescription = "Delete Journal"
                                )
                            }
                        }
                        IconButton(onClick = {
                            addVoiceNoteViewModel.onEvent(AddEditNoteEvent.SaveNote)
                            navController.navigate(Screen.VoicesScreen.route)
                        })
                        {
                            Text(text = "Save")
                        }

                    }
                )*/
                EditScreenTopAppBar(
                    navController = navController,
                    addVoiceNoteViewModel = addVoiceNoteViewModel,
                    note = note,
                    scope = scope,
                    snackbarHostState = snackbarHostState,
                    nav = {
                        if (addVoiceNoteViewModel.noteContent.value.text?.isNotEmpty() == true) {
                            //    navController.navigate(Screen.VoicesScreen.route)
                            addVoiceNoteViewModel.onEvent(AddEditNoteEvent.StopPlay)
                        } else {
                            addVoiceNoteViewModel.onEvent(AddEditNoteEvent.Error("Kindly Enter a title be for you save"))
                        }
                    },
                    datePickerDialog = {
                        datePickerDialog.show()
                    },
                    contentSaved = {
                        contentSaved.value = true
                    }
                )
            },
            floatingActionButtonPosition = FabPosition.Center,
            floatingActionButton = {
                /*   FloatingActionButton(
                       modifier = Modifier.shadow(
                           0.dp,
                           spotColor = Color(0xFF000000),
                           ambientColor = Color(0xFF000000)
                       ),
                       shape = RoundedCornerShape(size = 100.dp),
                       containerColor = Variables.SchemesOnPrimaryContainer,
                       contentColor = Color(0xFFffffff),
                       onClick = {
                           playState = !playState

                           if (playState && playNoteState) {
                               addVoiceNoteViewModel.onEvent(AddEditNoteEvent.Play(fileNameState.text))
                           } else {
                               addVoiceNoteViewModel.onEvent(AddEditNoteEvent.StopPlay)
                           }
                       }
                   )
                   {

                           if (playNoteState) {

                               Icon(
                                   painter = painterResource(if (!playState || mediaState) R.drawable.play_button_24 else R.drawable.pause_button_24),
                                   contentDescription = "Localized description"
                               )
                           }

                   }*/

                if (myState) {
                    RecordPanelComponent(
                        onTimerStart = {
                            addVoiceNoteViewModel.startTimer()
                        },
                        timerValue = timerValue,
                        onCancelRecord = {
                            addVoiceNoteViewModel.onEvent(AddEditNoteEvent.StopRecording)
                            addVoiceNoteViewModel.stopTimer()
                            addVoiceNoteViewModel.changeRecordState(false)
                            addVoiceNoteViewModel.onCancelRecord()
                        },
                        onDoneClick = {
                            addVoiceNoteViewModel.doneButtonState(true)
                            addVoiceNoteViewModel.onEvent(AddEditNoteEvent.StopRecording)
                            addVoiceNoteViewModel.stopTimer()
                            addVoiceNoteViewModel.changeRecordState(false)
                            doneButton.value = true
                        },


                        )
                } else {
                    if (fileNameState.text.isEmpty()) {
                        FloatingActionButton(
                            onClick = {
                                addVoiceNoteViewModel.onEvent(
                                    AddEditNoteEvent.Recording(
                                        fileNameState.text
                                    )
                                )
                                addVoiceNoteViewModel.changeRecordState(true)
                            },
                            modifier = Modifier.shadow(
                                0.dp,
                                spotColor = Color(0xFF000000),
                                ambientColor = Color(0xFF000000)
                            ),
                            shape = RoundedCornerShape(size = 100.dp),
                            containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            contentColor = Color(0xFFffffff),

                            ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_mic_24),
                                contentDescription = "Record icon"
                            )
                        }
                    }
                }

            },
            bottomBar = {

                Log.d("uris", "$uris")
                //   val uriList = fileNameState.imageFileUris?.map { Uri.parse(it) }

                BottomAppPanel(
                    cardVisibleState = {
                        // cardVisibleState = !cardVisibleState
                        openBottomSheet = !openBottomSheet
                    },

                    imageUri = uris,

                    imageSheetVisibleState = {
                        openImageSheet = !openImageSheet
                    },
                    tagVisiblestate = { showDialog() },
                    boldButtonState = {
                        isImportants.value = !isImportants.value
                        isImportant.value = !isImportant.value
                        if (isImportant.value) {
                            addVoiceNoteViewModel.onEvent(
                                AddEditNoteEvent.ChangeStyle(
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        color = Color.Gray
                                    )
                                )
                            )
                        } else {
                            addVoiceNoteViewModel.onEvent(
                                AddEditNoteEvent.ChangeStyle(
                                    style = TextStyle(
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            )

                        }
                    },
                    onBoldClick = {
                        state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))

                    },

                    onItalicClick = {
                        state.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    },
                    onStrikethroughClick = {
                        state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
                    },
                    onUnderlineClick = {
                        state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                    },
                    state = state
                ) {
                    openAlignStyleBottomSheet = !openAlignStyleBottomSheet
                }


                /* BottomAppBar(
                     Modifier
                         .wrapContentWidth()
                         .background(Color(colorIntState)),
                     containerColor = Color(colorIntState)
                 )
                 {
                     // Leading icons should typically have a high content alpha
                     CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {

                     }
                     // The actions should be at the end of the BottomAppBar. They use the default medium
                     // content alpha provided by BottomAppBar
                     Spacer(Modifier.weight(1f, true))
                     IconButton(onClick = {

                         recordState = !recordState
                         mediaState = false
                         playState = false
                         if (recordState) {
                             playButtonState = false

                             addVoiceNoteViewModel.onEvent(AddEditNoteEvent.Recording(fileNameState.text))
                         } else {

                             addVoiceNoteViewModel.onEvent(AddEditNoteEvent.StopRecording)
                             playButtonState = true
                         }


                     }) {
                         if (!playNoteState) {
                             Icon(
                                 painter = painterResource(if (recordState == false) R.drawable.baseline_mic_black_36 else R.drawable.stop_button_36),
                                 contentDescription = "Localized description"
                             )
                         }

                     }

                 }*/
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }

        ) { innerPadding ->

            Column(
                // consume insets as scaffold doesn't do it by default (yet)
                modifier = Modifier
                    .fillMaxSize()
                    .background(noteBackgroundAnimatable.value)
                    .padding(innerPadding),


                ) {/*
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    VoiceJournal.noteColors.forEach { color ->
                        val colorInt = color.toArgb()
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .shadow(15.dp, CircleShape)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = 3.dp,
                                    color = if (addVoiceNoteViewModel.noteColor.value == colorInt) {
                                        Color.Black
                                    } else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable {
                                    colorIntState = color.toArgb()
                                    scope.launch {
                                        noteBackgroundAnimatable.animateTo(
                                            targetValue = Color(colorInt),
                                            animationSpec = tween(
                                                durationMillis = 500
                                            )
                                        )
                                    }
                                    addVoiceNoteViewModel.onEvent(
                                        AddEditNoteEvent.ChangeColor(
                                            colorInt
                                        )
                                    )
                                }
                        )
                    }
                }*/
                Spacer(modifier = Modifier.height(16.dp))



                TransparentHintTextField(
                    text = contentState.text?.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.ROOT
                        ) else it.toString()
                    }
                        ?: "",
                    hint = contentState.hint,
                    onValueChange = {
                        addVoiceNoteViewModel.onEvent(AddEditNoteEvent.EnteredContent(it))
                    },
                    onFocusChange = {
                        addVoiceNoteViewModel.onEvent(AddEditNoteEvent.ChangeContentFocus(it))
                    },
                    isHintVisible = contentState.isHintVisible,
                    textStyle = textStyle, //androidx.compose.material.MaterialTheme.typography.body1
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    //
                )

                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }

                Spacer(modifier = Modifier.height(16.dp))


                    RichTextEditor(
                        placeholder = { Text("Enter you content here!") },
                        modifier = Modifier
                            .height(if (doneButtonState.value) IntrinsicSize.Min else IntrinsicSize.Max),
                        state = state,
                        colors = RichTextEditorDefaults.richTextEditorColors(
                            containerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            textColor = Variables.SchemesOnSurface

                        )
                    )

                Spacer(modifier = Modifier.size(8.dp))
                val timerValue by addVoiceNoteViewModel.timer2.collectAsState()
                val playingState by addVoiceNoteViewModel.playingState.collectAsState()
                val timerValue2 = addVoiceNoteViewModel.getDuration()
                if (
                    doneButtonState.value || playNoteState || (fileNameState.text.isNotEmpty() && !myState)
                ) {
                    PlayRecordPanel(
                        timerValue = timerValue,
                        timerValue2 = timerValue2,
                        onPlay = {
                            addVoiceNoteViewModel.startTimer2()
                            addVoiceNoteViewModel.onEvent(AddEditNoteEvent.Play(filename = fileNameState.text))
                        },
                        onCancelRecord = {
                            addVoiceNoteViewModel.onEvent(AddEditNoteEvent.StopPlay)
                            addVoiceNoteViewModel.stopTimer2()
                            Log.d("PlayCancel", "Clicked")
                        },
                        onRemove = {

                            addVoiceNoteViewModel.doneButtonState(false)
                            addVoiceNoteViewModel.onCancelRecord()
                            addVoiceNoteViewModel.onEvent(AddEditNoteEvent.StopPlay)
                        },
                        playingState = playingState
                    )
                }


            }


        }
        // Sheet content
        if (openBottomSheet) {
            val windowInsets = if (edgeToEdgeEnabled)
                WindowInsets(0) else BottomSheetDefaults.windowInsets
            BottomSheet(
                onDismissRequest = { openBottomSheet = false },
                sheetState = bottomSheetState,
                windowInsets = windowInsets,
                onClick = {
                    scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                        if (!bottomSheetState.isVisible) {
                            openBottomSheet = false
                        }
                    }
                },
                onImageClick = {

                    if (note.voiceJournal?.id != null) {
                        navController.navigate(
                            "gallery" +
                                    "?noteId=${note.voiceJournal.id}&noteColor=${noteColor}"
                        )
                    } else {
                        navController.navigate(
                            "gallery"
                        )
                    }
                },
                showFileChooser = {
                    showFileChooser()
                },
                onCameraClick = {
                    navController.navigate("camera")
                }
            )

        }
        // Sheet content
        if (openImageSheet) {

            val windowInsets = if (edgeToEdgeEnabled)
                WindowInsets(0) else BottomSheetDefaults.windowInsets
            if (uris != null) {
                ImageBottomSheet(
                    onDismissRequest = { openImageSheet = false },
                    sheetState = ImageSheetState,
                    windowInsets = windowInsets,
                    onClick = {

                        scope.launch { ImageSheetState.hide() }.invokeOnCompletion {
                            openBottomSheet = true
                            if (!ImageSheetState.isVisible) {
                                openImageSheet = false
                            }

                        }
                    },
                    onImageClick = {
                        navController.navigate("preview")
                    },
                    imageUris = uris
                )
            }
        }
        //Align style sheet
        if (openAlignStyleBottomSheet) {
            val windowInsets = if (edgeToEdgeEnabled)
                WindowInsets(0) else BottomSheetDefaults.windowInsets

            AlignStyleBottomSheet(
                onDismissRequest = { openAlignStyleBottomSheet = false },
                sheetState = alignStylebottomSheetState,
                windowInsets = windowInsets,
                onClick = {
                    scope.launch { alignStylebottomSheetState.hide() }.invokeOnCompletion {

                    }
                },
                state = state,
                onStartAlignClick = {
                    state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Start))
                },
                onEndAlignClick = {
                    state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.End))
                },
                onCenterAlignClick = {
                    state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Center))
                },
                onOrderedListClick = { state.toggleOrderedList() },
                onUnorderedListClick = { state.toggleUnorderedList() }
            )

        }

        if (showDialog.value) {

            TagDialog(
                addVoiceNoteViewModel = addVoiceNoteViewModel,
                onTagChecked = addVoiceNoteViewModel::onTagChecked,
                onCancel = {

                    hideDialog()

                },
                onOk = {
                    hideDialog()/* do something with the selected tags */
                }
            )
        }
        if (fileChooserState) {

            FileChooser(
                onOk = {
                    hideFileChooser()
                    openBottomSheet = false
                },
                saveSelectedUris = addVoiceNoteViewModel::saveSelectedUris,
                fileChooserState = fileChooserState
            )
        }

    }

}


@Composable
@Preview
fun AddVoiceNoteScreenPreview() {
    val context = LocalContext.current
    AddVoiceNoteScreen(
        navController = NavController(context),
        noteColor = Color.Blue.toArgb(),
        note = "note",
        snackbarHostState = SnackbarHostState()
    )
}
