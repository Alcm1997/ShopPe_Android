package com.idat

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.idat.data.local.preferences.UserPreferencesManager

// 🎨 Color definitions
object AppColors {
    // Brand Colors
    val ShoppePink = Color(0xFFAB005A)
    val ShoppePinkLight = Color(0xFFFFE8ED)
    val ShoppePinkDark = Color(0xFFD80073)

    // Light theme colors
    val LightPrimary = ShoppePink
    val LightSecondary = Color(0xFF455F88)
    val LightBackground = Color(0xFFFDFDFD)
    val LightSurface = Color(0xFFFFFFFF)
    val LightOnPrimary = Color(0xFFFFFFFF)
    val LightOnSecondary = Color(0xFFFFFFFF)
    val LightOnBackground = Color(0xFF1A1A1A)
    val LightOnSurface = Color(0xFF1A1A1A)
    val LightOutline = Color(0xFFEEEEEE)
    
    // Dark theme colors
    val DarkPrimary = Color(0xFFFFB1C8) // Softer pink for Dark Mode
    val DarkSecondary = Color(0xFFADC6FF)
    val DarkBackground = Color(0xFF121212)
    val DarkSurface = Color(0xFF1E1E1E) // Fondo principal
    val DarkSurfaceVariant = Color(0xFF282828) // Fondo sutil para tarjetas (poco más claro)
    val DarkOnPrimary = Color(0xFF600033)
    val DarkOnSecondary = Color(0xFF002E69)
    val DarkOnBackground = Color(0xFFE6E1E5)
    val DarkOnSurface = Color(0xFFE6E1E5)
    val DarkOutline = Color(0xFF444444)
}

@Composable
fun getGradientColors(isDarkTheme: Boolean): List<Color> {
    return if (isDarkTheme) {
        listOf(
            Color(0xFF1A1A2E),
            Color(0xFF16213E),
            Color(0xFF0F3460)
        )
    } else {
        listOf(
            Color(0xFF7F00FF).copy(alpha = 0.4f),
            Color(0xFFE100FF).copy(alpha = 0.35f),
            Color(0xFF00C6FF).copy(alpha = 0.35f)
        )
    }
}

private val LightColorScheme = lightColorScheme(
    primary = AppColors.LightPrimary,
    secondary = AppColors.LightSecondary,
    background = AppColors.LightBackground,
    surface = AppColors.LightSurface,
    surfaceVariant = Color(0xFFFFF0F3), // Rosado Pastel Crema para contenedores
    outlineVariant = Color(0xFFE2BDC6), // Borde suave rosado
    onPrimary = AppColors.LightOnPrimary,
    onSecondary = AppColors.LightOnSecondary,
    onBackground = AppColors.LightOnBackground,
    onSurface = AppColors.LightOnSurface
)

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.DarkPrimary,
    secondary = AppColors.DarkSecondary,
    background = AppColors.DarkBackground,
    surface = AppColors.DarkSurface,
    surfaceVariant = Color(0xFF1A0F12), // Vino Profundo para contenedores
    outlineVariant = Color(0xFF3D262B), // Borde suave vino
    onPrimary = AppColors.DarkOnPrimary,
    onSecondary = AppColors.DarkOnSecondary,
    onBackground = AppColors.DarkOnBackground,
    onSurface = AppColors.DarkOnSurface,
    outline = AppColors.DarkOutline
)

@Composable
fun AppTheme(
    userPreferencesManager: UserPreferencesManager,
    content: @Composable () -> Unit
) {
    val isDarkTheme by userPreferencesManager.isDarkTheme.collectAsState(initial = false)
    
    val colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
