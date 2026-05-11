package com.mywordsmyway.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF356859),
    onPrimary = Color.White,
    secondary = Color(0xFF7D5A50),
    tertiary = Color(0xFF4D6A9A),
    background = Color(0xFFFAF8F3),
    onBackground = Color(0xFF202124),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF202124),
    surfaceVariant = Color(0xFFE9E2D6),
    onSurfaceVariant = Color(0xFF4D4A45),
    error = Color(0xFF9B2C2C),
)

@Composable
fun MyWordsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content,
    )
}
