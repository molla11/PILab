package com.example.pilab.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object TerminalColors {
    val Background = Color(0xFF0A0A0A)
    val Primary = Color(0xFF33FF00)
    val Secondary = Color(0xFFFFB000)
    val Muted = Color(0xFF1F521F)
    val Error = Color(0xFFFF3333)
    val SurfaceVariant = Color(0xFF0E160E)
}

private val PilabColorScheme = darkColorScheme(
    primary = TerminalColors.Primary,
    onPrimary = TerminalColors.Background,
    primaryContainer = TerminalColors.SurfaceVariant,
    onPrimaryContainer = TerminalColors.Primary,
    secondary = TerminalColors.Secondary,
    onSecondary = TerminalColors.Background,
    secondaryContainer = Color(0xFF241A00),
    onSecondaryContainer = TerminalColors.Secondary,
    background = TerminalColors.Background,
    onBackground = TerminalColors.Primary,
    surface = TerminalColors.Background,
    onSurface = TerminalColors.Primary,
    surfaceVariant = TerminalColors.SurfaceVariant,
    onSurfaceVariant = Color(0xFF78A878),
    outline = TerminalColors.Muted,
    outlineVariant = TerminalColors.Muted,
    error = TerminalColors.Error,
    onError = TerminalColors.Background,
    errorContainer = Color(0xFF2A0A0A),
    onErrorContainer = TerminalColors.Error
)

private val TerminalTypography = Typography(
    displayLarge = terminalTextStyle(48, 56, FontWeight.Bold),
    displayMedium = terminalTextStyle(40, 48, FontWeight.Bold),
    displaySmall = terminalTextStyle(32, 40, FontWeight.Bold),
    headlineLarge = terminalTextStyle(30, 38, FontWeight.Bold),
    headlineMedium = terminalTextStyle(26, 34, FontWeight.Bold),
    headlineSmall = terminalTextStyle(22, 30, FontWeight.Bold),
    titleLarge = terminalTextStyle(20, 28, FontWeight.SemiBold),
    titleMedium = terminalTextStyle(16, 24, FontWeight.SemiBold),
    titleSmall = terminalTextStyle(14, 20, FontWeight.SemiBold),
    bodyLarge = terminalTextStyle(16, 24, FontWeight.Normal),
    bodyMedium = terminalTextStyle(14, 21, FontWeight.Normal),
    bodySmall = terminalTextStyle(12, 18, FontWeight.Normal),
    labelLarge = terminalTextStyle(14, 20, FontWeight.SemiBold),
    labelMedium = terminalTextStyle(12, 16, FontWeight.Medium),
    labelSmall = terminalTextStyle(11, 14, FontWeight.Medium)
)

private val TerminalShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
)

@Composable
fun PilabTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PilabColorScheme,
        typography = TerminalTypography,
        shapes = TerminalShapes,
        content = content
    )
}

private fun terminalTextStyle(
    fontSize: Int,
    lineHeight: Int,
    fontWeight: FontWeight
): TextStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = fontWeight,
    fontSize = fontSize.sp,
    lineHeight = lineHeight.sp
)
