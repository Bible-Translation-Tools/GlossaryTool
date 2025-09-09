package org.bibletranslationtools.glossary.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0056D1),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = Color(0xFF0D47A1),

    secondary = Color(0xFF455A64),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFECEFF1),
    onSecondaryContainer = Color(0xFF263238),

    tertiary = Color(0xFF7B1FA2),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF3E5F5),
    onTertiaryContainer = Color(0xFF4A148C),

    error = Color(0xFFD32F2F),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFB71C1C),

    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF212121),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF212121),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF616161),

    surfaceTint = Color(0xFF1976D2),
    inverseSurface = Color(0xFF303030),
    inverseOnSurface = Color(0xFFF5F5F5),

    outline = Color(0xFFBDBDBD),
    outlineVariant = Color(0xFFE0E0E0),

    scrim = Color(0xFF000000)
)

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1565C0),
    onPrimaryContainer = Color(0xFFE3F2FD),

    secondary = Color(0xFFB0BEC5),
    onSecondary = Color(0xFF263238),
    secondaryContainer = Color(0xFF37474F),
    onSecondaryContainer = Color(0xFFECEFF1),

    tertiary = Color(0xFFCE93D8),
    onTertiary = Color(0xFF4A148C),
    tertiaryContainer = Color(0xFF6A1B9A),
    onTertiaryContainer = Color(0xFFF3E5F5),

    error = Color(0xFFFF5252),
    onError = Color(0xFFB71C1C),
    errorContainer = Color(0xFFD32F2F),
    onErrorContainer = Color(0xFFFFEBEE),

    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),

    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFBDBDBD),

    surfaceTint = Color(0xFF90CAF9),
    inverseSurface = Color(0xFFE0E0E0),
    inverseOnSurface = Color(0xFF303030),

    outline = Color(0xFF616161),
    outlineVariant = Color(0xFF424242),

    scrim = Color(0xFF000000)
)

@Composable
fun MainAppTheme(
    themeColorScheme: ColorScheme? = null,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        themeColorScheme != null -> themeColorScheme
        isSystemInDarkTheme() -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
