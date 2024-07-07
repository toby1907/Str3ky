package com.example.shoppi.ui.home
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import com.example.shoppi.R
import com.example.shoppi.ui.theme.ShoppiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerNav(
    navController: NavController,
    onSwitch: () -> Unit
){
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
// icons to mimic drawer destinations
    val navItems = listOf(
        NavItem("All Categories"),
        NavItem("Track Order"),
        NavItem("Discover All"),
        NavItem("Location"),
        NavItem("Payment Cards"),
        NavItem("Orders"),
        NavItem("Scan"),
        NavItem("Settings")
    )

    val items = listOf(Icons.Default.Favorite, Icons.Default.Face, Icons.Default.Email)
    val selectedItem = remember { mutableStateOf(navItems[0]) }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(40.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Image(
                            painterResource(id =R .drawable.profile),
                            contentDescription = null,
                            modifier = Modifier
                                .size(74.dp)
                                .clip(CircleShape)
                        )
                    }
                        Column(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .align(Alignment.CenterVertically)
                        ) {
                            Text(text = "Alexender Hussain",
                                fontSize = 18.sp,
                                    fontWeight = FontWeight(700),
                                    color = Color(0xFF152354)
                            )
                            Text(text = "Edit Profile", fontSize = 14.sp)
                        }

                }
                Spacer(Modifier.height(12.dp))
               navItems.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(
                                text = item.label,
                            fontSize = 18.sp,
                                fontWeight = FontWeight(600),
                                color = Color(0xFF152354),
                                textAlign = TextAlign.Center
                        ) },
                        selected = item == selectedItem.value,
                        onClick = {
                            scope.launch { drawerState.close() }
                            selectedItem.value = item
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            Row() {
                Spacer(modifier = Modifier.size(16.dp))
                Image(painter = painterResource(id = R.drawable.share), contentDescription ="share icon" )
                Text(
                    text = "Sign Out",
                    fontSize = 18.sp,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF152354),
                    textAlign = TextAlign.Center
                )
            }
            }
        },
        content = {
            HomeScreen(menuOnClick = { scope.launch { drawerState.open() }
            },
                navController= navController,
                onSwitch = onSwitch
                )

        }
    )
}



data class NavItem(val label: String)

