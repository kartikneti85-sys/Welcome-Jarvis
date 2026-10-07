package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JarvisColorScheme = darkColorScheme(
    primary = JarvisCyan,
    onPrimary = Color(0xFF002026),
    primaryContainer = JarvisSurfaceVariant,
    onPrimaryContainer = JarvisCyanGlow,
    secondary = JarvisCyanDim,
    onSecondary = Color.White,
    secondaryContainer = JarvisSurfaceDark,
    onSecondaryContainer = JarvisTextPrimary,
    tertiary = JarvisGold,
    onTertiary = Color.Black,
    error = JarvisCrimson,
    onError = Color.White,
    background = JarvisBgDark,
    onBackground = JarvisTextPrimary,
    surface = JarvisSurfaceDark,
    onSurface = JarvisTextPrimary,
    surfaceVariant = JarvisSurfaceVariant,
    onSurfaceVariant = JarvisTextSecondary,
    outline = JarvisBorderCyan
)

@Composable
fun JarvisTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = Typography,
        content = content
    )
}
