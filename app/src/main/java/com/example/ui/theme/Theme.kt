package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = CollegeBlue,
    onPrimary = Color.White,
    primaryContainer = CollegeBlueLight,
    onPrimaryContainer = CollegeNavy,
    secondary = CollegeSlate,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1F5F9),
    onSecondaryContainer = CollegeTextPrimary,
    tertiary = EmeraldSuccess,
    background = GlassBackground,
    onBackground = CollegeTextPrimary,
    surface = GlassSurfaceWhite,
    onSurface = CollegeTextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = CollegeTextSecondary,
    outline = Color(0xFFE2E8F0),
    error = RoseError
)

@Composable
fun TeacherPlanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
