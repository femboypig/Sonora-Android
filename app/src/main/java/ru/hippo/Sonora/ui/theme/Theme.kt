package ru.hippo.Sonora.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = SonoraAccentYellow,
    onPrimary = SonoraDarkBackground,
    background = SonoraDarkBackground,
    onBackground = SonoraDarkOnSurface,
    surface = SonoraDarkSurface,
    onSurface = SonoraDarkOnSurface,
    surfaceVariant = SonoraDarkSurfaceVariant,
    onSurfaceVariant = SonoraDarkSecondaryText
)

private val LightColorScheme = lightColorScheme(
    primary = SonoraAccentYellow,
    onPrimary = SonoraLightOnSurface,
    background = SonoraLightBackground,
    onBackground = SonoraLightOnSurface,
    surface = SonoraLightSurface,
    onSurface = SonoraLightOnSurface,
    surfaceVariant = SonoraLightSurfaceVariant,
    onSurfaceVariant = SonoraLightSecondaryText
)

@Composable
fun SonoraTheme(
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
