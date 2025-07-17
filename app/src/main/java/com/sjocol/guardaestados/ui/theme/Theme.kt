package com.sjocol.guardaestados.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.sjocol.guardaestados.ui.theme.*
import androidx.compose.ui.graphics.Color

enum class ThemeType { DEFAULT, BLUE, PURPLE, ORANGE, GRAY }
enum class ThemeMode { SYSTEM, LIGHT, DARK }

private fun getLightColors(themeType: ThemeType) = when (themeType) {
    ThemeType.DEFAULT -> lightColorScheme(
        primary = DefaultPrimary,
        onPrimary = DefaultOnPrimary,
        secondary = DefaultSecondary,
        onSecondary = DefaultOnSecondary,
        background = DefaultBackground,
        onBackground = DefaultOnBackground
    )
    ThemeType.BLUE -> lightColorScheme(
        primary = BluePrimary,
        onPrimary = BlueOnPrimary,
        secondary = BlueSecondary,
        onSecondary = BlueOnSecondary,
        background = BlueBackground,
        onBackground = BlueOnBackground
    )
    ThemeType.PURPLE -> lightColorScheme(
        primary = PurplePrimary,
        onPrimary = PurpleOnPrimary,
        secondary = PurpleSecondary,
        onSecondary = PurpleOnSecondary,
        background = PurpleBackground,
        onBackground = PurpleOnBackground
    )
    ThemeType.ORANGE -> lightColorScheme(
        primary = OrangePrimary,
        onPrimary = OrangeOnPrimary,
        secondary = OrangeSecondary,
        onSecondary = OrangeOnSecondary,
        background = OrangeBackground,
        onBackground = OrangeOnBackground
    )
    ThemeType.GRAY -> lightColorScheme(
        primary = GrayPrimary,
        onPrimary = GrayOnPrimary,
        secondary = GraySecondary,
        onSecondary = GrayOnSecondary,
        background = GrayBackground,
        onBackground = GrayOnBackground
    )
}

private fun getDarkColors(themeType: ThemeType) = when (themeType) {
    ThemeType.DEFAULT -> darkColorScheme(
        primary = DefaultPrimary,
        onPrimary = Color.White, // Siempre blanco para contraste
        secondary = DefaultSecondary,
        onSecondary = Color.White,
        background = DefaultOnBackground,
        onBackground = DefaultBackground
    )
    ThemeType.BLUE -> darkColorScheme(
        primary = BluePrimary,
        onPrimary = Color.White,
        secondary = BlueSecondary,
        onSecondary = Color.White,
        background = BlueOnBackground,
        onBackground = BlueBackground
    )
    ThemeType.PURPLE -> darkColorScheme(
        primary = PurplePrimary,
        onPrimary = Color.White,
        secondary = PurpleSecondary,
        onSecondary = Color.White,
        background = PurpleOnBackground,
        onBackground = PurpleBackground
    )
    ThemeType.ORANGE -> darkColorScheme(
        primary = OrangePrimary,
        onPrimary = Color.Black, // Mejor contraste sobre naranja
        secondary = OrangeSecondary,
        onSecondary = Color.Black,
        background = OrangeOnBackground,
        onBackground = OrangeBackground
    )
    ThemeType.GRAY -> darkColorScheme(
        primary = GrayPrimary,
        onPrimary = Color.White,
        secondary = GraySecondary,
        onSecondary = Color.Black,
        background = GrayOnBackground,
        onBackground = GrayBackground
    )
}

@Composable
fun GuardaEstadosTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    themeType: ThemeType = ThemeType.DEFAULT,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colorScheme = if (darkTheme) getDarkColors(themeType) else getLightColors(themeType)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}