package com.example.shoppi.ui.components
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shoppi.R
import com.example.shoppi.ui.theme.ShoppiTheme

@Composable
fun SegmentedControl() {
    val items = listOf(
        Pair("", ""),
        Pair(painterResource(R.drawable.nike_logo), "Nike"),
        Pair(painterResource(R.drawable.adidas_logo), "Adidas"),
        Pair(painterResource(R.drawable.puma_logo), "Puma")
    )
    var selectedItemIndex by remember { mutableStateOf(0) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(42.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                    selectedItemIndex = index
                }
            ) {
                if(index == 0) {
                   Text(
                       text = "All",
                       color =  if(selectedItemIndex ==index) Color.Blue else Color.Gray
                   )
                } else if (index == 1) {
                    Image(
                        painter = painterResource(id = R.drawable.nike_logo),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        colorFilter =  if (selectedItemIndex == index) ColorFilter.tint(Color.Blue) else ColorFilter.tint(Color.Gray)
                    )
                } else if (index == 2) {
                    Image(
                        painter = painterResource(id = R.drawable.adidas_logo),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        colorFilter = if (selectedItemIndex == index) ColorFilter.tint(Color.Blue) else ColorFilter.tint(Color.Gray)
                    )
                } else if (index == 3) {
                    Image(
                        painter = painterResource(id = R.drawable.puma_logo),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        colorFilter = if (selectedItemIndex == index) ColorFilter.tint(Color.Blue) else ColorFilter.tint(Color.Gray)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.nike_logo),
                        contentDescription = null,
                        tint = if (selectedItemIndex == index) Color.Blue else Color.Gray
                    )
                }
               if(item.second.isNotBlank()) {
                    Text(
                        text = item.second,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selectedItemIndex == index) Color.Blue else Color.Gray
                    )
                }
            }
        }
    }

}
@Preview(showBackground = true)
@Composable
fun SegmentButtonPreview() {
    ShoppiTheme {
        SegmentedControl()
    }
}