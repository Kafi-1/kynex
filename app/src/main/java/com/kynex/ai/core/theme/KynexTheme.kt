package com.kynex.ai.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

data class KynexColors(
    val bg: Color,
    val bgSidebar: Color,
    val bgInput: Color,
    val bgHover: Color,
    val bgUserBubble: Color,
    val text: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val border: Color,
    val accent: Color,
    val accentHover: Color
)

private val LightColors = KynexColors(
    bg = Color(0xFFFAF9F5),
    bgSidebar = Color(0xFFF0EEE6),
    bgInput = Color(0xFFFFFFFF),
    bgHover = Color(0x0E000000),
    bgUserBubble = Color(0xFFECECEC),
    text = Color(0xFF1F1E1D),
    textSecondary = Color(0xFF6B6A66),
    textTertiary = Color(0xFF97968F),
    border = Color(0xFFE5E3DC),
    accent = Color(0xFFD97757),
    accentHover = Color(0xFFC2603F)
)

private val DarkColors = KynexColors(
    bg = Color(0xFF262624),
    bgSidebar = Color(0xFF1F1E1D),
    bgInput = Color(0xFF30302E),
    bgHover = Color(0x14FFFFFF),
    bgUserBubble = Color(0xFF3A3A38),
    text = Color(0xFFF5F4EE),
    textSecondary = Color(0xFFA3A29C),
    textTertiary = Color(0xFF71706B),
    border = Color(0xFF3D3D3A),
    accent = Color(0xFFD97757),
    accentHover = Color(0xFFC2603F)
)

val LocalKynexColors = staticCompositionLocalOf { LightColors }

val SerifDisplay = FontFamily.Serif

@Composable
fun KynexTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val kynex = if (darkTheme) DarkColors else LightColors
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = kynex.accent,
            onPrimary = Color.White,
            background = kynex.bg,
            onBackground = kynex.text,
            surface = kynex.bg,
            onSurface = kynex.text,
            surfaceVariant = kynex.bgSidebar,
            onSurfaceVariant = kynex.textSecondary,
            outline = kynex.border,
            error = Color(0xFFE57373)
        )
    } else {
        lightColorScheme(
            primary = kynex.accent,
            onPrimary = Color.White,
            background = kynex.bg,
            onBackground = kynex.text,
            surface = kynex.bg,
            onSurface = kynex.text,
            surfaceVariant = kynex.bgSidebar,
            onSurfaceVariant = kynex.textSecondary,
            outline = kynex.border,
            error = Color(0xFFC0392B)
        )
    }

    CompositionLocalProvider(LocalKynexColors provides kynex) {
        MaterialTheme(
            colorScheme = scheme,
            typography = Typography(),
            content = content
        )
    }
}

object Kynex {
    val colors: KynexColors
        @Composable get() = LocalKynexColors.current
}
