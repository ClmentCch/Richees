package com.richyss.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.richyss.app.data.prefs.ThemeMode

private val DarkScheme = darkColorScheme(
    primary = RichyssAccent,
    onPrimary = RichyssTextPrimary,
    primaryContainer = RichyssAccent,
    onPrimaryContainer = RichyssTextPrimary,
    secondary = RichyssOnline,
    onSecondary = RichyssBackground,
    background = RichyssBackground,
    onBackground = RichyssTextPrimary,
    surface = RichyssSurface,
    onSurface = RichyssTextPrimary,
    surfaceVariant = RichyssCard,
    onSurfaceVariant = RichyssTextSecondary,
    outline = RichyssTextSecondary,
    error = RichyssDanger,
    onError = RichyssTextPrimary
)

private val LightScheme = lightColorScheme(
    primary = RichyssAccent,
    onPrimary = ColorWhite,
    primaryContainer = RichyssAccent,
    onPrimaryContainer = ColorWhite,
    secondary = RichyssOnline,
    onSecondary = ColorWhite,
    background = RichyssLightBackground,
    onBackground = RichyssLightTextPrimary,
    surface = RichyssLightSurface,
    onSurface = RichyssLightTextPrimary,
    surfaceVariant = RichyssLightCard,
    onSurfaceVariant = RichyssLightTextSecondary,
    outline = RichyssLightTextSecondary,
    error = RichyssDanger,
    onError = ColorWhite
)

private val RichyssTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 15.sp,
        lineHeight = 21.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp
    )
)

@Composable
fun RichyssTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val scheme = if (dark) DarkScheme else LightScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = scheme.background.toArgb()
            window.navigationBarColor = scheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !dark
        }
    }
    MaterialTheme(
        colorScheme = scheme,
        typography = RichyssTypography,
        content = content
    )
}
