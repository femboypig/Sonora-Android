package ru.hippo.M2.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = M2AccentYellow,
    onPrimary = M2DarkBackground,
    background = M2DarkBackground,
    onBackground = M2DarkOnSurface,
    surface = M2DarkSurface,
    onSurface = M2DarkOnSurface,
    surfaceVariant = M2DarkSurfaceVariant,
    onSurfaceVariant = M2DarkSecondaryText
)

private val LightColorScheme = lightColorScheme(
    primary = M2AccentYellow,
    onPrimary = M2LightOnSurface,
    background = M2LightBackground,
    onBackground = M2LightOnSurface,
    surface = M2LightSurface,
    onSurface = M2LightOnSurface,
    surfaceVariant = M2LightSurfaceVariant,
    onSurfaceVariant = M2LightSecondaryText
)

@Composable
fun M2Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> DarkColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> LightColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
