package com.abhinav.taskwall.ui.theme

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

val PremiumBlack = Color(0xFF000000)
val PremiumCharcoal = Color(0xFF121212)
val PremiumAccent = Color(0xFFE0E0E0)

private val DarkColorScheme = darkColorScheme(
    primary = PremiumAccent,
    secondary = PremiumAccent,
    tertiary = PremiumAccent,
    background = PremiumBlack,
    surface = PremiumCharcoal,
    onPrimary = PremiumBlack,
    onSecondary = PremiumBlack,
    onTertiary = PremiumBlack,
    onBackground = Color.White,
    onSurface = Color.White,
)

// TaskWall is fundamentally a dark-theme/AMOLED app. We will use the dark color scheme regardless.
@Composable
fun TaskWallTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
