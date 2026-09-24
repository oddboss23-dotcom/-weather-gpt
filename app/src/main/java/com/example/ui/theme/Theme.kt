package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WeatherGPTColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = DeepNavyBg,
    primaryContainer = SurfaceCard,
    onPrimaryContainer = CyanAccent,
    secondary = ElectricTeal,
    onSecondary = DeepNavyBg,
    secondaryContainer = SurfaceNavy,
    onSecondaryContainer = ElectricTeal,
    tertiary = WeatherBlue,
    onTertiary = Color.White,
    background = DeepNavyBg,
    onBackground = TextPrimary,
    surface = SurfaceNavy,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorder,
    error = AlertRed,
    onError = Color.White
)

@Composable
fun WeatherGPTTheme(
    darkTheme: Boolean = true, // Scientific weather dashboard is default deep dark
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WeatherGPTColorScheme,
        typography = Typography,
        content = content
    )
}

