package com.ampliguitar.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BritishBlue,
    secondary = BritishRed,
    background = White,
    surface = White,
    onPrimary = White,
    onSecondary = White,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

private val LightColorScheme = lightColorScheme(
    primary = BritishBlue,
    secondary = BritishRed,
    background = White,
    surface = White,
    onPrimary = White,
    onSecondary = White,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

@Composable
fun AmpliGuitarTheme(
    darkTheme: Boolean = false, // Force light theme for British Flag colors
    dynamicColor: Boolean = false, // Disable dynamic color
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
