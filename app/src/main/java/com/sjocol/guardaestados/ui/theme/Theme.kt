package com.sjocol.guardaestados.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.sjocol.guardaestados.AppPalette
import androidx.compose.ui.graphics.Color

@Composable
fun GuardaEstadosTheme(
    palette: AppPalette = AppPalette.DEFAULT,
    content: @Composable () -> Unit
) {
    val colorScheme = when (palette) {
        AppPalette.DEFAULT -> if (isSystemInDarkTheme()) {
            darkColorScheme(
                primary = DefaultPrimary,
                secondary = DefaultSecondary,
                background = DefaultBackground,
                surface = DefaultSurface,
                onPrimary = DefaultOnPrimary,
                onSecondary = DefaultOnSecondary,
                onBackground = DefaultOnBackground,
                onSurface = DefaultOnSurface
            )
        } else {
            lightColorScheme(
                primary = DefaultPrimary,
                secondary = DefaultSecondary,
                background = DefaultBackground,
                surface = DefaultSurface,
                onPrimary = DefaultOnPrimary,
                onSecondary = DefaultOnSecondary,
                onBackground = DefaultOnBackground,
                onSurface = DefaultOnSurface
            )
        }
        AppPalette.LIGHT -> lightColorScheme(
            primary = Color(0xFF1976D2), // Azul
            secondary = Color(0xFF90CAF9),
            background = Color(0xFFFFFFFF),
            surface = Color(0xFFF5F5F5),
            onPrimary = Color.White,
            onSecondary = Color.Black,
            onBackground = Color(0xFF222222),
            onSurface = Color(0xFF222222)
        )
        AppPalette.DARK -> darkColorScheme(
            primary = Color(0xFF222831), // Gris oscuro
            secondary = Color(0xFF393E46),
            background = Color(0xFF121212),
            surface = Color(0xFF23272F),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFFE0E0E0),
            onSurface = Color(0xFFE0E0E0)
        )
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            // Usar fondo claro solo para la paleta LIGHT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = palette == AppPalette.LIGHT
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}