package com.aadhapaisa.shared.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.Primary,
    onPrimary = Color.Black,
    primaryContainer = AppColors.PrimaryVariant,
    onPrimaryContainer = Color.White,
    secondary = AppColors.Secondary,
    onSecondary = Color.White,
    secondaryContainer = AppColors.SurfaceVariant,
    onSecondaryContainer = AppColors.OnSurface,
    tertiary = AppColors.Warning,
    onTertiary = Color.Black,
    error = AppColors.Error,
    onError = Color.White,
    errorContainer = AppColors.SurfaceVariant,
    onErrorContainer = AppColors.Error,
    background = AppColors.Background,
    onBackground = AppColors.OnBackground,
    surface = AppColors.Surface,
    onSurface = AppColors.OnSurface,
    surfaceVariant = AppColors.SurfaceVariant,
    onSurfaceVariant = AppColors.OnSurfaceVariant,
    outline = AppColors.CardBorder,
    outlineVariant = AppColors.SecondaryText,
    scrim = Color.Black.copy(alpha = 0.5f)
)

@Composable
fun AadhaPaisaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

