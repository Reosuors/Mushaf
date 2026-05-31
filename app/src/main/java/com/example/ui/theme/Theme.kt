package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun MushafTheme(
    themeColors: AppThemeColors = DarkGoldTheme,
    content: @Composable () -> Unit
) {
    val colorScheme = if (themeColors.isDark) {
        darkColorScheme(
            primary = themeColors.primary,
            secondary = themeColors.primaryVariant,
            tertiary = themeColors.success,
            background = themeColors.background,
            surface = themeColors.surface,
            onPrimary = themeColors.background,
            onSecondary = themeColors.background,
            onTertiary = themeColors.background,
            onBackground = themeColors.text,
            onSurface = themeColors.text,
            surfaceVariant = themeColors.cardBg,
            onSurfaceVariant = themeColors.textSecondary,
            outline = themeColors.border
        )
    } else {
        lightColorScheme(
            primary = themeColors.primary,
            secondary = themeColors.primaryVariant,
            tertiary = themeColors.success,
            background = themeColors.background,
            surface = themeColors.surface,
            onPrimary = themeColors.surface,
            onSecondary = themeColors.surface,
            onTertiary = themeColors.surface,
            onBackground = themeColors.text,
            onSurface = themeColors.text,
            surfaceVariant = themeColors.cardBg,
            onSurfaceVariant = themeColors.textSecondary,
            outline = themeColors.border
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
