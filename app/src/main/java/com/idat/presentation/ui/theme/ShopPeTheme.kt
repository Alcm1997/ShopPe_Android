package com.idat.presentation.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object ShopPeColors {
    val Background = Color(0xFFFFF8F8)
    val OnBackground = Color(0xFF27171C)
    
    val Surface = Color(0xFFFFF8F8)
    val OnSurface = Color(0xFF27171C)
    val SurfaceVariant = Color(0xFFF8DBE2)
    val OnSurfaceVariant = Color(0xFF5A3F47)
    
    val SurfaceContainerLowest = Color(0xFFFFFFFF)
    val SurfaceContainerLow = Color(0xFFFFF0F2)
    val SurfaceContainerHigh = Color(0xFFFEE1E7)
    val SurfaceContainerHighest = Color(0xFFF8DBE2)

    val Primary = Color(0xFFAB005A)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFFD80073)
    val OnPrimaryContainer = Color(0xFFFFF0F2)

    val Secondary = Color(0xFF455F88)
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFFB6D0FF)
    val OnSecondaryContainer = Color(0xFF3F5882)

    val Tertiary = Color(0xFF725000)
    val OnTertiary = Color(0xFFFFFFFF)
    val TertiaryFixed = Color(0xFFFFDEA8)
    val OnTertiaryFixed = Color(0xFF271900)

    val Outline = Color(0xFF8E6F77)
    val OutlineVariant = Color(0xFFE2BDC6)
}

val ShopPeLightColorScheme = lightColorScheme(
    primary = ShopPeColors.Primary,
    onPrimary = ShopPeColors.OnPrimary,
    primaryContainer = ShopPeColors.PrimaryContainer,
    onPrimaryContainer = ShopPeColors.OnPrimaryContainer,
    secondary = ShopPeColors.Secondary,
    onSecondary = ShopPeColors.OnSecondary,
    secondaryContainer = ShopPeColors.SecondaryContainer,
    onSecondaryContainer = ShopPeColors.OnSecondaryContainer,
    tertiary = ShopPeColors.Tertiary,
    onTertiary = ShopPeColors.OnTertiary,
    background = ShopPeColors.Background,
    onBackground = ShopPeColors.OnBackground,
    surface = ShopPeColors.Surface,
    onSurface = ShopPeColors.OnSurface,
    surfaceVariant = ShopPeColors.SurfaceVariant,
    onSurfaceVariant = ShopPeColors.OnSurfaceVariant,
    outline = ShopPeColors.Outline,
    outlineVariant = ShopPeColors.OutlineVariant
)

// Dark Theme support keeping the palette close but inverted backgrounds
val ShopPeDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB1C7),
    onPrimary = Color(0xFF650033),
    primaryContainer = ShopPeColors.PrimaryContainer,
    onPrimaryContainer = Color(0xFFFFD9E2),
    secondary = Color(0xFFADC7F7),
    onSecondary = Color(0xFF153155),
    secondaryContainer = Color(0xFF2D476F),
    onSecondaryContainer = Color(0xFFD6E3FF),
    tertiary = Color(0xFFFFBA20),
    onTertiary = Color(0xFF3C2900),
    background = Color(0xFF140C0E), // Custom dark background
    onBackground = Color(0xFFF0D3D9),
    surface = Color(0xFF140C0E),
    onSurface = Color(0xFFF0D3D9),
    surfaceVariant = Color(0xFF5A3F47),
    onSurfaceVariant = Color(0xFFE2BDC6),
    outline = Color(0xFFA88991),
    outlineVariant = Color(0xFF5A3F47)
)

@androidx.compose.runtime.Composable
fun ShopPeUITheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @androidx.compose.runtime.Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ShopPeDarkColorScheme else ShopPeLightColorScheme
    androidx.compose.material3.MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
