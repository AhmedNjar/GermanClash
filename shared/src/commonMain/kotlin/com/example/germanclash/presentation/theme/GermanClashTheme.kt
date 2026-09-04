package com.example.germanclash.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Previously every screen painted its own gradient Box and hardcoded
 * Color.White on each Text as a workaround for MaterialTheme defaulting to
 * a light scheme. This is the actual dark theme, so those workarounds can
 * come back out - Surface(color = colorScheme.background) now gives every
 * descendant the right default text color automatically.
 */
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
)

private val GermanClashColorScheme = darkColorScheme(
    primary = GameColors.Primary,
    onPrimary = Color.White,
    secondary = GameColors.TitleAccent,
    onSecondary = Color.White,
    background = GameColors.Background,
    onBackground = Color.White,
    surface = GameColors.Surface,
    onSurface = Color.White,
    surfaceVariant = GameColors.OptionDefault,
    onSurfaceVariant = Color.White,
    error = GameColors.WrongRed,
    onError = Color.White,
    outline = GameColors.TitleAccent.copy(alpha = 0.12f)
)

@Composable
fun GermanClashTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GermanClashColorScheme,
        typography = AppTypography,
        content = content
    )
}
