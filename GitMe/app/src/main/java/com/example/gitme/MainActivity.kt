package com.example.gitme

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import com.example.gitme.ui.theme.GitMeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GitMeTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ProfileScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    Surface(color = MaterialTheme.colorScheme.background) {
        LazyColumn {
            item {
                TopAppBar(
                    title = {
                        Text(
                            text = "Profile",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight(400),
                                color = Color(0xFF000000),
                                textAlign = TextAlign.Center,
                            )
                        )
                    }
                )
            }
            item {
                ProfilePicSection()
            }
            item {
                GithubButtonSection()
            }
        }
    }
}

@Composable
fun ProfilePicSection() {
    val profilePic = painterResource(id = R.drawable.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = profilePic,
            contentDescription = "Profile Picture",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .width(375.dp)
                .height(375.dp)
                .clickable {
                    // Add your animation logic here when the picture is clicked
                },
        )
        Surface(
            modifier = Modifier
                .width(340.dp)
                .height(75.dp)
                .align(Alignment.BottomCenter)
                .offset(y = 24.dp)
                .clip(RoundedCornerShape(size = 5.dp)),
            color = Color(0xFFFCFCFC)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SlackIdentitySection()
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(color = Color(0xFF0F0F0F), shape = CircleShape)
                            .width(35.dp)
                            .height(35.dp)
                            .clickable {
                                // Add your animation logic here when the picture is clicked
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_call_24),
                            contentDescription = "call",
                            tint = Color(0xFFFCFCFC)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(color = Color(0xFF0F0F0F), shape = CircleShape)
                            .width(35.dp)
                            .height(35.dp)
                            .clickable {
                                // Add your animation logic here when the picture is clicked
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_message_24),
                            contentDescription = "message",
                            tint = Color(0xFFFCFCFC)
                        )
                    }
                    Spacer(modifier = Modifier.size(width = 24.dp, height = 0.dp))
                }
            }
        }
    }
}

@Composable
fun SlackIdentitySection() {

    val slackName = "Toby1907"

    Text(
        text = slackName,
        style = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontSize = 16.sp,
            fontWeight = FontWeight(500),
            color = Color(0xFF000000),
        ),
        modifier = Modifier.padding(16.dp)
    )

}

@Composable
fun GithubButtonSection() {
    val githubUrl = "https://github.com/toby1907"
    val context = LocalContext.current
    val intent = Intent(context, WebViewActivity::class.java)
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = {
                // Open the GitHub profile in a WebView component within the app
                intent.putExtra("url", githubUrl)
                context.startActivity(intent)
            },
            modifier = Modifier
                .padding(16.dp)
                .width(236.dp)
                .height(45.dp)
        ) {
            Text(text = "Open GitHub")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GitMeTheme {
        ProfileScreen()
    }
}