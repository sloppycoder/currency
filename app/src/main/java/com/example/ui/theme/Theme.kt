package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

data class AppColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val elevatedBackground: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val divider: Color,
    val accentGreen: Color,
    val baseRowHighlight: Color,
    val normalPillBg: Color,
    val normalPillText: Color,
    val basePillBg: Color,
    val basePillText: Color,
    val offlinePillBg: Color,
    val offlinePillText: Color,
    val searchBarBg: Color,
    val searchBarBorder: Color,
    val keypadNumberBg: Color,
    val keypadOperatorBg: Color,
    val keypadPresetBg: Color
)

val DarkAppColors = AppColors(
    isDark = true,
    background = Color(0xFF000000),
    surface = Color(0xFF141414),
    cardBackground = Color(0xFF1C1C1E),
    cardBorder = Color(0xFF2C2C2E),
    elevatedBackground = Color(0xFF2C2C2E),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFF8E8E93),
    textTertiary = Color(0xFF636366),
    divider = Color(0xFF1C1C1E),
    accentGreen = Color(0xFF34C759),
    baseRowHighlight = Color(0x1F34C759),
    normalPillBg = Color(0xFF0F2615),
    normalPillText = Color(0xFF30D158),
    basePillBg = Color(0xFF34C759),
    basePillText = Color(0xFF000000),
    offlinePillBg = Color(0xFF261D10),
    offlinePillText = Color(0xFFFF9F0A),
    searchBarBg = Color(0xFF1C1C1E),
    searchBarBorder = Color(0xFF2C2C2E),
    keypadNumberBg = Color(0xFF2C2C2E),
    keypadOperatorBg = Color(0xFF3A3A3C),
    keypadPresetBg = Color(0xFF1E3A24)
)

val LightAppColors = AppColors(
    isDark = false,
    background = Color(0xFFF7F8FA),
    surface = Color(0xFFFFFFFF),
    cardBackground = Color(0xFFFFFFFF),
    cardBorder = Color(0xFFE5E7EB),
    elevatedBackground = Color(0xFFF1F3F5),
    textPrimary = Color(0xFF111827),
    textSecondary = Color(0xFF6B7280),
    textTertiary = Color(0xFF9CA3AF),
    divider = Color(0xFFE5E7EB),
    accentGreen = Color(0xFF10B981),
    baseRowHighlight = Color(0x1410B981),
    normalPillBg = Color(0xFFE6F7EF),
    normalPillText = Color(0xFF059669),
    basePillBg = Color(0xFF10B981),
    basePillText = Color(0xFFFFFFFF),
    offlinePillBg = Color(0xFFFEF3C7),
    offlinePillText = Color(0xFFD97706),
    searchBarBg = Color(0xFFFFFFFF),
    searchBarBorder = Color(0xFFD1D5DB),
    keypadNumberBg = Color(0xFFFFFFFF),
    keypadOperatorBg = Color(0xFFEAEBED),
    keypadPresetBg = Color(0xFFE6F7EF)
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }

object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF34C759),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF0F2615),
    onPrimaryContainer = Color(0xFF30D158),
    secondary = Color(0xFF34C759),
    onSecondary = Color.Black,
    background = Color(0xFF000000),
    onBackground = Color.White,
    surface = Color(0xFF000000),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1C1C1E),
    onSurfaceVariant = Color(0xFF8E8E93)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF10B981),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE6F7EF),
    onPrimaryContainer = Color(0xFF059669),
    secondary = Color(0xFF10B981),
    onSecondary = Color.White,
    background = Color(0xFFF7F8FA),
    onBackground = Color(0xFF111827),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFF6B7280)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val appColors = if (darkTheme) DarkAppColors else LightAppColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
