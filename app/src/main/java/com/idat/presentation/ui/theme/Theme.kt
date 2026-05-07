package com.idat.presentation.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = ShopPeBlue,
    secondary = ShopPeRed,
    tertiary = ShopPeOrange,
    background = Neutral_20, // Fondo principal ligeramente gris
    surface = Neutral_10,    // Color para Cards, TopAppBar, etc.
    onPrimary = Neutral_10,  // Texto/íconos sobre el color primario
    onSecondary = Neutral_10, // Texto/íconos sobre el color secundario
    onTertiary = Neutral_90,
    onBackground = Neutral_90, // Texto/íconos sobre el fondo principal
    onSurface = Neutral_90,    // Texto/íconos sobre superficies
)

private val DarkColorScheme = darkColorScheme(
    primary = ShopPeBlue, // Se mantiene el azul para la marca
    secondary = ShopPeRed,
    tertiary = ShopPeOrange,
    background = Neutral_100, // Fondo oscuro
    surface = Neutral_90,    // Superficies un poco más claras
    onPrimary = Neutral_10,
    onSecondary = Neutral_10,
    onTertiary = Neutral_90,
    onBackground = Neutral_10,
    onSurface = Neutral_10,
)

@Composable
fun ShopPeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        // Aquí también se podría definir la tipografía (Typography) y las formas (Shapes)
        content = content
    )
}
