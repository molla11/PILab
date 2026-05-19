package com.example.pilab.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PilabColorScheme = lightColorScheme(
    primary = Color(0xFF255C6B),
    onPrimary = Color.White,
    secondary = Color(0xFF7A4E2D),
    tertiary = Color(0xFF49672D),
    background = Color(0xFFF7F8F5),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE6ECE5),
    error = Color(0xFFB3261E)
)

@Composable
fun PilabTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PilabColorScheme,
        content = content
    )
}
