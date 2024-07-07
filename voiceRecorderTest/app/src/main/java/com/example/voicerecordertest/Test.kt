package com.example.voicerecordertest

import android.media.MediaRecorder
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VoiceChatRecorder(
    startRecording: () -> Unit,
    stopRecording: () -> Unit,
    startPlaying: () -> Unit,
    stopPlaying: () -> Unit,
    isFinishPlaying: Boolean
) {
    var isRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var audioFilePath by remember { mutableStateOf("") }
    var recordingTime by remember { mutableStateOf(0) }
    var isDragging by remember { mutableStateOf(false) }
    var audioPath = ""
    val formatter = SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.getDefault())
    val now = Date()
    val context = LocalContext.current

    val recorder1 = remember {
        MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        }
    }

    /*  val startRecording = {

   audioPath = "${context.getDir("WhatsappClonei",0)?.absolutePath}/Media+Recording+${System.currentTimeMillis()}+.3gp"

          recorder1.setOutputFile(audioPath)
          recorder1.apply {
              prepare()
              start()
          }
          isRecording = true
      }

      val stopRecording = {
          recorder1.apply {
              stop()
              release()
          }
          isRecording = false
      }*/




    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        Box(contentAlignment = Alignment.CenterEnd,
            modifier = Modifier
                .width(257.dp)
                .height(60.dp)
                .background(color = Color(0xFF1EBE71), shape = RoundedCornerShape(size = 10.dp))
                .padding(16.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            if (!isRecording) {
                                isDragging = true
                                startRecording()
                            }
                        },
                        onDragEnd = {
                            if (isDragging) {
                                isDragging = false
                                stopRecording()
                            }
                        },
                        onDragCancel = {
                            if (isDragging) {
                                isDragging = false
                                stopRecording()
                            }
                        },
                        onDrag = { change, dragAmount ->
                            if (isDragging) {
                                recordingTime++
                                change.consumeAllChanges()
                            }
                        }
                    )
                }
        ) {
            val longClickDuration = 1000 // in milliseconds

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = if (isRecording) "Stop Recording" else "Hold to Record")

                if (isDragging) {
                    Text(text = "$recordingTime s", modifier = Modifier.padding(start = 8.dp))
                }
                IconButton(
                    onClick = {
                        Log.d("voice", "worked")
                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .pointerInput(Unit) {
                            var downTime: Long = 0
                            detectTapGestures(
                                onLongPress = {
                                    CountTimer()
                                    startRecording()
                                },
                            )
                        }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.mic),
                        contentDescription = "record"
                    )
                }
            }
            Button(onClick = {
                isPlaying = !isPlaying
                if (isPlaying) startPlaying() else stopPlaying()
            }
            ) {
                Text(
                    text = if (isPlaying && isFinishPlaying) {
                        "Hold to Play"
                    } else if (isPlaying) {
                        "Stop Playing"
                    } else {
                        "Hold to Play"
                    }
                )
            }

            if (audioFilePath.isNotEmpty()) {
                Text(
                    text = "Audio file saved at $audioFilePath",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }


}

fun countTimer() {
    var timeUsed by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (timeUsed < 30) {
            delay(1000)
            timeUsed++
        }
    }

}

