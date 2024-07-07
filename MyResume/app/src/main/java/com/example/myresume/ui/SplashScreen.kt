package com.example.myresume.ui
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myresume.R
import kotlinx.coroutines.delay

private const val SplashWaitTime: Long = 2000
@Composable
fun SplashScreen(
    modifier: Modifier=Modifier,
    onTimeout: () -> Unit
){

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center

    ) {
        val currentOnTimeout by rememberUpdatedState(newValue = onTimeout)
        LaunchedEffect(onTimeout){
            delay(SplashWaitTime)//Simulates loading things
            currentOnTimeout()
            //want to test if this will work direct.
            //  onTimeout()
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            Image(modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                ,
                painter = painterResource(id = R.drawable.dp),
                contentDescription = null
            )
            Spacer(
                Modifier.height(16.dp)
            )
            Text(
                text = "MyResume"
            )
        }
    }
}

@Composable
@Preview
fun SplashScreenPreview(
){
    SplashScreen(onTimeout = {})
}