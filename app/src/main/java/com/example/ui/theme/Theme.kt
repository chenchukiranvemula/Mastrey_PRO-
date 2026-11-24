package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class ThemeModePreference(val label: String) {
    DARK("Dark"),
    LIGHT("Light"),
    SYSTEM("System")
}

enum class AccentColorPreference(val label: String, val primary: Color, val secondary: Color, val tertiary: Color) {
    ELECTRIC_BLUE("Electric Blue", ElectricBlue, VividViolet, CyberCyan),
    VIVID_VIOLET("Violet Pulse", VividViolet, ElectricBlue, CyberCyan),
    CYBER_CYAN("Cyber Cyan", CyberCyan, ElectricBlue, VividViolet),
    EMERALD_FOCUS("Emerald Focus", EmeraldAccent, CyberCyan, ElectricBlue)
}

enum class TextSizePreference(val label: String, val scale: Float) {
    SMALL("Small", 0.90f),
    DEFAULT("Default", 1.0f),
    LARGE("Large", 1.14f),
    EXTRA_LARGE("Extra Large", 1.28f)
}

enum class InterfaceDensityPreference(val label: String, val cardPaddingDp: Int) {
    COMPACT("Compact", 12),
    COMFORTABLE("Comfortable", 16),
    SPACIOUS("Spacious", 20)
}

@Immutable
data class MastreyDesignConfig(
    val isDark: Boolean = true,
    val reducedMotion: Boolean = false,
    val highContrast: Boolean = false,
    val largerControls: Boolean = false,
    val density: InterfaceDensityPreference = InterfaceDensityPreference.COMFORTABLE,
    val accent: AccentColorPreference = AccentColorPreference.ELECTRIC_BLUE
)

val LocalMastreyDesignConfig = staticCompositionLocalOf { MastreyDesignConfig() }

private val MastreyShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MastreyProTheme(
    themeMode: ThemeModePreference = ThemeModePreference.DARK,
    accent: AccentColorPreference = AccentColorPreference.ELECTRIC_BLUE,
    textSize: TextSizePreference = TextSizePreference.DEFAULT,
    lineSpacingMultiplier: Float = 1.0f,
    highContrast: Boolean = false,
    reducedMotion: Boolean = false,
    largerControls: Boolean = false,
    density: InterfaceDensityPreference = InterfaceDensityPreference.COMFORTABLE,
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        ThemeModePreference.DARK -> true
        ThemeModePreference.LIGHT -> false
        ThemeModePreference.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (useDark) {
        darkColorScheme(
            primary = accent.primary,
            onPrimary = Color.White,
            primaryContainer = accent.primary.copy(alpha = 0.22f),
            onPrimaryContainer = TextPrimaryDark,
            secondary = accent.secondary,
            onSecondary = Color.White,
            secondaryContainer = accent.secondary.copy(alpha = 0.20f),
            onSecondaryContainer = TextPrimaryDark,
            tertiary = accent.tertiary,
            onTertiary = MidnightBlack,
            tertiaryContainer = accent.tertiary.copy(alpha = 0.20f),
            onTertiaryContainer = TextPrimaryDark,
            background = if (highContrast) Color.Black else MidnightBlack,
            onBackground = if (highContrast) Color.White else TextPrimaryDark,
            surface = if (highContrast) Color(0xFF0A0F1A) else DeepNavy,
            onSurface = if (highContrast) Color.White else TextPrimaryDark,
            surfaceVariant = if (highContrast) Color(0xFF162038) else CharcoalSurface,
            onSurfaceVariant = if (highContrast) Color(0xFFE2E8F0) else TextSecondaryDark,
            surfaceContainerLowest = MidnightBlack,
            surfaceContainerLow = DeepNavy,
            surfaceContainer = CharcoalSurface,
            surfaceContainerHigh = ElevatedCardDark,
            surfaceContainerHighest = SubtleBorderDark,
            outline = if (highContrast) Color(0xFF64748B) else SubtleBorderDark,
            outlineVariant = SubtleBorderDark.copy(alpha = 0.6f),
            error = ErrorRed,
            onError = Color.White,
            errorContainer = ErrorContainerDark,
            onErrorContainer = Color(0xFFFECACA)
        )
    } else {
        lightColorScheme(
            primary = accent.primary,
            onPrimary = Color.White,
            primaryContainer = accent.primary.copy(alpha = 0.14f),
            onPrimaryContainer = TextPrimaryLight,
            secondary = accent.secondary,
            onSecondary = Color.White,
            secondaryContainer = accent.secondary.copy(alpha = 0.14f),
            onSecondaryContainer = TextPrimaryLight,
            tertiary = accent.tertiary,
            onTertiary = Color.White,
            tertiaryContainer = accent.tertiary.copy(alpha = 0.14f),
            onTertiaryContainer = TextPrimaryLight,
            background = if (highContrast) Color.White else SnowBackground,
            onBackground = if (highContrast) Color.Black else TextPrimaryLight,
            surface = CrispSurfaceLight,
            onSurface = if (highContrast) Color.Black else TextPrimaryLight,
            surfaceVariant = SoftContainerLight,
            onSurfaceVariant = if (highContrast) Color(0xFF1E293B) else TextSecondaryLight,
            surfaceContainerLowest = Color.White,
            surfaceContainerLow = SnowBackground,
            surfaceContainer = CrispSurfaceLight,
            surfaceContainerHigh = SoftContainerLight,
            surfaceContainerHighest = Color(0xFFDCE6F4),
            outline = if (highContrast) Color(0xFF334155) else SubtleBorderLight,
            outlineVariant = SubtleBorderLight.copy(alpha = 0.65f),
            error = ErrorRed,
            onError = Color.White,
            errorContainer = Color(0xFFFEE2E2),
            onErrorContainer = Color(0xFF7F1D1D)
        )
    }

    val typography = createMastreyTypography(
        textScale = textSize.scale,
        lineSpacingMultiplier = lineSpacingMultiplier
    )

    val designConfig = MastreyDesignConfig(
        isDark = useDark,
        reducedMotion = reducedMotion,
        highContrast = highContrast,
        largerControls = largerControls,
        density = density,
        accent = accent
    )

    CompositionLocalProvider(LocalMastreyDesignConfig provides designConfig) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = MastreyShapes,
            content = content
        )
    }
}
