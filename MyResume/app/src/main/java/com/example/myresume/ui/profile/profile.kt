package com.example.myresume.ui.profile

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myresume.R
import com.example.myresume.ui.theme.MyResumeTheme
import com.example.myresume.ui.theme.Pink80
import kotlinx.coroutines.flow.collectLatest


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Profile(viewModel: ProfileViewModel){

    val disabledItem =1
    var menuExpanded by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current.applicationContext
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    IconButton(
                        onClick = { menuExpanded=true }
                    ) {
                        Icon(painter = painterResource(id = R.drawable.ic_baseline_settings_24), contentDescription = "Mode switch")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = {
                            menuExpanded = false
                        }
                    ) {
                        // adding items
                       DropdownMenuItem(leadingIcon = {
                           Icon(painter = painterResource(id = R.drawable.ic_baseline_dark_mode_24), contentDescription ="dark mode" )
                                                      },text = {
                                               Text(text = "Dark mode")
                       }, onClick = { viewModel.onEvent(ProfileViewModel.UiState.DarkMode) })
                        DropdownMenuItem(leadingIcon = {
                                                       Icon(painter = painterResource(id = R.drawable.ic_baseline_light_mode_24), contentDescription ="light mode" )
                        }, text = {
                            Text(text = "Light mode")
                        }, onClick = { viewModel.onEvent(ProfileViewModel.UiState.LightMode) })

                    }
                }
            )
        },
        bottomBar = {
                    BottomAppBar() {
                        Spacer(Modifier.weight(1f, true))
                        IconButton(
                            onClick = { /* "Open nav drawer" */ }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.suitcases), contentDescription = "Mode switch")
                        }
                        Spacer(Modifier.weight(1f, true))
                    }
        },

        content = { innerPadding ->
            Surface(modifier = Modifier.padding( innerPadding)) {
                LazyColumn(
                    contentPadding = innerPadding,
                ) {
                    item {    HeroSection()
                        Spacer(modifier =Modifier.size(8.dp))
                        AboutMeSection()
                        Spacer(modifier =Modifier.size(8.dp))
                        SkillsSection()
                        Spacer(modifier =Modifier.size(8.dp))
                        Experience()
                        Spacer(modifier = Modifier.size(8.dp))
                        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                            ContactButton()

                        }
                    }

                }
            }
        }
    )
}


@Composable
fun HeroSection(modifier: Modifier = Modifier){
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Center,
        modifier= Modifier
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
        ) {
            Image(
                painterResource(id = com.example.myresume.R.drawable.dp),
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Pink80, RoundedCornerShape(4.dp))
            )
        }
        Spacer(Modifier.padding(8.dp))
        Column(
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(8.dp)
        ){
            Text(
                text = "Olaleye Paul Tobi",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(text = "Android Dev")
            Text(text = "@Toby1907")
        }
    }
}

@Composable
fun AboutMeSection() {

    var isExpanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.clickable { isExpanded = !isExpanded }) {
        Text(
            text = "About Me",
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.titleSmall
        )
        val maxLineAnim by animateIntAsState(
            if (isExpanded) Int.MAX_VALUE else 6,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Surface(shape = MaterialTheme.shapes.medium, shadowElevation = 1.dp) {

                Text(
                    overflow = TextOverflow.Ellipsis,
                    maxLines = maxLineAnim.coerceAtLeast(0),
                    style = MaterialTheme.typography.bodyMedium,
                    text = stringResource(com.example.myresume.R.string.about_me),
                    modifier = Modifier.padding(all = 4.dp),

                    )


        }
    }
}
@Composable
fun SkillsSection(){
    Column(  verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Skills",
           style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary
        )
            Skills()


    }
}
@Composable
fun ExperienceSection(){
    Column(  verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {

        Experience()


    }
}
@Composable
fun SkillItem(modifier: Modifier = Modifier,@DrawableRes id:Int, skillName:String){

        Card(
            Modifier
                .padding(start = 24.dp, top = 8.dp)
                .size(80.dp)
            ,shape = MaterialTheme.shapes.small,

        ) {
            Column(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    alignment = Alignment.Center,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(top = 8.dp),
                    painter = painterResource(id = id),
                    contentDescription = "",
                    contentScale = ContentScale.Fit
                )
                Text(text = skillName, style =MaterialTheme.typography.bodyMedium )
            }
        }


}
@Composable
fun Skills(modifier: Modifier = Modifier){
        Row(
            Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkillItem(id= com.example.myresume.R.drawable.android_logo_icon_png_svg, skillName = "Android")
            SkillItem(id = R.drawable.kotlin_1_logo_png_transparent, skillName = "Kotlin")
            SkillItem(id= R.drawable.figma, skillName = "Figma")
        }

}

@Composable
fun Experience(modifier: Modifier=Modifier) {
  Column {
      Text(
          text = "Experience",
          style = MaterialTheme.typography.headlineSmall
          ,
          color = MaterialTheme.colorScheme.secondary
      )

        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(8.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(80.dp)
            ) {
                Image(
                    painterResource(id = com.example.myresume.R.drawable.black_dot_png_20),
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Pink80, RoundedCornerShape(4.dp))
                )
            }
            Spacer(Modifier.padding(8.dp))
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = "Chrisland Uni",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(text = "System analysis")
                Text(text = "2021-till date",
                    style= MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun ContactButton(modifier: Modifier=Modifier){

    val mUriHandler= LocalUriHandler.current

    Row(){
       Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.DarkGray
            ),
            onClick = { mUriHandler.openUri("https://github.com/toby1907") },
            // Uses ButtonDefaults.ContentPadding by default
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 12.dp,
                end = 20.dp,
                bottom = 12.dp
            )
        ) {
            // Inner content including an icon and a text label
            Icon(
                painter = painterResource(id = R.drawable.github),
                contentDescription = "Favorite",
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )

            Text(text = "GitHub Repo")
        }
        Spacer(modifier = Modifier.padding(24.dp))
        OutlinedButton(colors = ButtonDefaults.buttonColors(
            containerColor = Color.DarkGray
        ),
            onClick = {  mUriHandler.openUri("https://www.linkedin.com/in/olaleye-paul-tobi-6abaa411b")  },
            // Uses ButtonDefaults.ContentPadding by default
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 12.dp,
                end = 20.dp,
                bottom = 12.dp
            )
        ) {
            // Inner content including an icon and a text label
            Icon(
                painter = painterResource(id = R.drawable.linkedin__2_),
                contentDescription = "Favorite",
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )

            Text(text = "LinkedIn")
        }
    }

}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyResumeTheme {
        HeroSection( )
    }

}
@Preview(showBackground = true)
@Composable
fun AboutMeSectionPreview() {
    MyResumeTheme {
        Experience()
    }
}
@Preview(showBackground = true)
@Composable
fun SkillsPreview(){
    MyResumeTheme {
        val viewModel = ProfileViewModel()
       Profile(viewModel)
    }
}