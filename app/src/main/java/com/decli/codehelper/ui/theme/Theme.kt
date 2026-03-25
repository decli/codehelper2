package com.decli.codehelper.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = AccentTerracotta,
    onPrimary = CardSurface,
    primaryContainer = PendingCardSurface,
    onPrimaryContainer = Ink,
    secondary = AccentBlue,
    onSecondary = CardSurface,
    secondaryContainer = HeroCool,
    onSecondaryContainer = Ink,
    tertiary = AccentGreen,
    onTertiary = CardSurface,
    tertiaryContainer = PickedCardSurface,
    onTertiaryContainer = Ink,
    background = AppBackground,
    surface = CardSurface,
    surfaceVariant = HeroSurface,
    onSurface = Ink,
    onSurfaceVariant = InkMuted,
    error = AccentTerracotta,
    errorContainer = PendingCardSurface,
)

private val DarkColors = darkColorScheme(
    primary = HeroWarm,
    onPrimary = Ink,
    secondary = AccentBlue,
    tertiary = AccentGreen,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceMuted,
    onSurface = CardSurface,
    onSurfaceVariant = AppBackground,
    error = AccentTerracotta,
)

@Composable
fun CodeHelperTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = CodeHelperTypography,
        content = content,
    )
}
