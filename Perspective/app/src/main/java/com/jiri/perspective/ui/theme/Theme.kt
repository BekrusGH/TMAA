package com.jiri.perspective.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PerspectiveDarkPrimary,
    onPrimary = PerspectiveDarkOnPrimary,
    primaryContainer = PerspectiveDarkPrimaryContainer,
    secondary = PerspectiveDarkSecondary,
    onSecondary = PerspectiveDarkOnSecondary,
    secondaryContainer = PerspectiveDarkSecondaryContainer,
    tertiary = PerspectiveDarkTertiary,
    background = PerspectiveDarkBackground,
    onBackground = PerspectiveDarkOnBackground,
    surface = PerspectiveDarkSurface,
    onSurface = PerspectiveDarkOnSurface,
    surfaceVariant = PerspectiveDarkSurfaceVariant,
    onSurfaceVariant = PerspectiveDarkOnSurfaceVariant,
    outline = PerspectiveDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = PerspectivePrimary,
    onPrimary = PerspectiveOnPrimary,
    primaryContainer = PerspectivePrimaryContainer,
    secondary = PerspectiveSecondary,
    onSecondary = PerspectiveOnSecondary,
    secondaryContainer = PerspectiveSecondaryContainer,
    tertiary = PerspectiveTertiary,
    background = PerspectiveBackground,
    onBackground = PerspectiveOnBackground,
    surface = PerspectiveSurface,
    onSurface = PerspectiveOnSurface,
    surfaceVariant = PerspectiveSurfaceVariant,
    onSurfaceVariant = PerspectiveOnSurfaceVariant,
    outline = PerspectiveOutline
)

@Composable
fun PerspectiveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}