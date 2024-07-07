package com.example.shoppi.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoppi.R
import com.example.shoppi.ui.TopAppBarEnum
import com.example.shoppi.ui.theme.ShoppiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun AppBar(
    actionOnClick : () -> Unit?,
    menuOnClick: () -> Unit,
    topAppBarEnum: TopAppBarEnum,
    appBarTitle: String,
    @DrawableRes navigationIcon : Int?,
    @DrawableRes actionIcon : Int?,
    scrollBehavior: TopAppBarScrollBehavior,
    onSwitch: () -> Unit
){
   // val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    LargeTopAppBar(
        title = {

            Text(
                text = appBarTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            fontSize = 20.sp,
                fontWeight = FontWeight(700),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                letterSpacing = 0.8.sp)
        },
        navigationIcon = {
            IconButton(
                onClick = {

                             menuOnClick.invoke()

                },
                modifier = Modifier
                    .clip(CircleShape)
                    .size(width = 56.dp, height = 56.dp)
                    .border((-1).dp, Color.LightGray)
            ) {
                navigationIcon?.let { painterResource(id = it) }?.let {
                    Icon(
                        painter = it,
                        contentDescription = "Localized description"
                    )
                }
            }
        },
        actions = {
            if(topAppBarEnum.value =="home") {
                Switcher(onSwitch=onSwitch)
            }
            else if (topAppBarEnum.value =="discovery"){
                Icon(
                    painter = painterResource(R.drawable.baseline_notifications_none_24),
                    contentDescription = "Localized description"
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}
