package com.artrubadur.teno.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF2774D6),
    onPrimary = Color(0xFFFFFFFF),

    primaryContainer = Color(0xFF184D95),
    onPrimaryContainer = Color(0xFFDCEBFF),

    secondary = Color(0xFF82B8FF),
    onSecondary = Color(0xFF082C56),

    secondaryContainer = Color(0xFF12355F),
    onSecondaryContainer = Color(0xFFDCEBFF),

    tertiary = Color(0xFF79C6F5),
    onTertiary = Color(0xFF002E44),

    tertiaryContainer = Color(0xFF0F445F),
    onTertiaryContainer = Color(0xFFD4F2FF),

    background = Color(0xFF0D0E12),
    onBackground = Color(0xFFF2F3F5),

    surface = Color(0xFF191A1F),
    onSurface = Color(0xFFE8EAED),

    surfaceVariant = Color(0xFF23252B),
    onSurfaceVariant = Color(0xFF9EA4AF),

    surfaceTint = Color(0xFF2774D6),

    outline = Color(0xFF3A3E47),
    outlineVariant = Color(0xFF2B2E35),

    inverseSurface = Color(0xFFE8EAED),
    inverseOnSurface = Color(0xFF1B1C20),
    inversePrimary = Color(0xFF1F63BA),

    error = Color(0xFFFF6B6B),
    onError = Color(0xFFFFFFFF),

    errorContainer = Color(0xFF5A1F22),
    onErrorContainer = Color(0xFFFFD9D9),
)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2774D6),
    onPrimary = Color(0xFFFFFFFF),

    primaryContainer = Color(0xFFDCEBFF),
    onPrimaryContainer = Color(0xFF002F67),

    secondary = Color(0xFF4F8FE6),
    onSecondary = Color(0xFFFFFFFF),

    secondaryContainer = Color(0xFFDDEBFF),
    onSecondaryContainer = Color(0xFF14365F),

    tertiary = Color(0xFF1E8ECF),
    onTertiary = Color(0xFFFFFFFF),

    tertiaryContainer = Color(0xFFD7F2FF),
    onTertiaryContainer = Color(0xFF00344D),

    background = Color(0xFFF6F8FB),
    onBackground = Color(0xFF14161A),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF181A1E),

    surfaceVariant = Color(0xFFF0F2F5),
    onSurfaceVariant = Color(0xFF5F6672),

    surfaceTint = Color(0xFF2774D6),

    outline = Color(0xFFD2D7DE),
    outlineVariant = Color(0xFFE5E8EC),

    inverseSurface = Color(0xFF202228),
    inverseOnSurface = Color(0xFFF5F6F7),
    inversePrimary = Color(0xFF82B8FF),

    error = Color(0xFFD32F2F),
    onError = Color(0xFFFFFFFF),

    errorContainer = Color(0xFFFFE2E0),
    onErrorContainer = Color(0xFF5E0000),
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}