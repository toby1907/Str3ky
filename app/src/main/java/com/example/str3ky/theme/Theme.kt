package com.example.str3ky.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xffD9C76F),
    secondary = Color(0xff272209),
    tertiary = Color(0xff032816),
    background = Color(0xFF15130B),
    surface = Color(0xFF222017),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFF363016),
    onTertiary = Color(0xffFFFFFF),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFFe6e0e9),
    primaryContainer = Color(0xff292200),
    secondaryContainer = Color(0xff3c3930),
    surfaceVariant = Color(0xff222017)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xffD9C76F),
    secondary = Color(0xffD0C6A2),
    tertiary = Color(0xff032816),
    background = Color(0xFF15130B),
    surface = Color(0xFF15130B),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFF363016),
    onTertiary = Color(0xffFFFFFF),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE8E2D4),
    primaryContainer = Color(0xff524600),
    onSecondaryContainer = Color(0xffede2bc),
    surfaceVariant = Color(0xff222017)


)

@Composable
fun Str3kyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        /*dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }*/

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primaryContainer.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}