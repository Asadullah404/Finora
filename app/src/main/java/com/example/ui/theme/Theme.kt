package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val FinoraDarkColorScheme = darkColorScheme(
    primary = Emerald400,
    onPrimary = Navy900,
    primaryContainer = Emerald700,
    onPrimaryContainer = Color.White,
    secondary = Cyan400,
    onSecondary = Navy900,
    secondaryContainer = Navy500,
    onSecondaryContainer = Slate100,
    tertiary = Amber400,
    onTertiary = Navy900,
    background = Navy800,
    onBackground = Slate50,
    surface = Navy700,
    onSurface = Slate50,
    surfaceVariant = Navy600,
    onSurfaceVariant = Slate300,
    outline = Slate600,
    outlineVariant = Slate700,
    error = Rose400,
    onError = Color.White
)

private val FinoraLightColorScheme = lightColorScheme(
    primary = Emerald600,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Emerald700,
    secondary = Navy600,
    onSecondary = Color.White,
    secondaryContainer = Slate100,
    onSecondaryContainer = Navy800,
    tertiary = Amber500,
    onTertiary = Color.White,
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    outline = Slate300,
    outlineVariant = Slate200,
    error = Rose600,
    onError = Color.White
)

@Composable
fun FinoraTheme(
    darkTheme: Boolean = true, // Default to sophisticated dark theme for fintech feel
    dynamicColor: Boolean = false, // Preserve crafted luxury midnight emerald palette
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> FinoraDarkColorScheme
        else -> FinoraLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    FinoraTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
