package com.example.shoppi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shoppi.R
import com.example.shoppi.ui.theme.ShoppiTheme


@Composable
fun Switcher(onSwitch: () -> Unit) {
    var isOn by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .width(84.dp)
            .height(44.dp)
            .background(Color.Transparent, CircleShape)
            .clickable(onClick = {
                isOn = !isOn
                onSwitch()
            }),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
       Icon(
            painter = if (isOn) painterResource(id = R.drawable.baseline_light_mode_24) else painterResource(
                id = R.drawable.baseline_nightlight_24
            ) ,
            contentDescription = "Switcher",
            tint = Color(0xffFFBC47),
            modifier = Modifier.size(24.dp)
        )
       Icon(
            painter = if (isOn) painterResource(id = R.drawable.baseline_nightlight_24) else painterResource(
                id = R.drawable.baseline_light_mode_24),
            tint = Color.Gray,
           contentDescription = "Switcher"
        )
    }
}
@Preview(showBackground = true)
@Composable
fun ThemeSwitchPreview() {
   ShoppiTheme {
       Switcher({})
    }
}
