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
private val GermanClashColorScheme = darkColorScheme(
    primary = GameColors.OptionSelected,
    onPrimary = Color.White,
    secondary = GameColors.TitleAccent,
    onSecondary = Color.White,
    background = GameColors.BackgroundBottom,
    onBackground = Color.White,
    surface = GameColors.CardBackground,
    onSurface = Color.White,
    error = GameColors.WrongRed,
    onError = Color.White
)

@Composable
fun GermanClashTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GermanClashColorScheme,
        content = content
    )
}
