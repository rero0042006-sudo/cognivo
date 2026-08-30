package com.memorymoments.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Northeast India Heritage & Stitch Design System Tokens (Calm, Warm Palette)
val NeBackground = Color(0xFFFBF9F5)            // Warm Ivory / Cream
val NeBackgroundElevated = Color(0xFFF5F3EF)    // Soft Muted Cream
val NeSurface = Color(0xFFFFFFFF)               // Soft Warm White
val NeSurfaceContainer = Color(0xFFEFEEEA)      // Subtle Card Inner Container
val NeSurfaceContainerHigh = Color(0xFFEAE8E4)  // Card High Container
val NeSurfaceContainerHighest = Color(0xFFE4E2DE)

val NePrimary = Color(0xFF154212)               // Deep Forest Green
val NeOnPrimary = Color(0xFFFFFFFF)
val NePrimaryContainer = Color(0xFFBCF0AE)      // Soft Muted Leaf Green
val NeOnPrimaryContainer = Color(0xFF002201)

val NeSecondary = Color(0xFF735C00)             // Muted Bamboo / Olive Gold
val NeOnSecondary = Color(0xFFFFFFFF)
val NeSecondaryContainer = Color(0xFFFED65B)    // Warm Muted Gold Sand
val NeOnSecondaryContainer = Color(0xFF241A00)

val NeTertiary = Color(0xFFA01317)              // Muted Terracotta Accent (used sparingly)
val NeTertiaryContainer = Color(0xFFFFDAD6)
val NeOnTertiaryContainer = Color(0xFF410003)

val NeTextPrimary = Color(0xFF1B1C1A)           // Deep Charcoal
val NeTextSecondary = Color(0xFF42493E)         // Muted Earth Slate
val NeTextMuted = Color(0xFF72796E)             // Outline Muted Gray

val NeBorder = Color(0xFFE4E2DE)                // Fine Card Border
val NeBorderSubtle = Color(0xFFEFEEEA)
val NeOutline = Color(0xFFC2C9BB)

val NeSuccess = Color(0xFF2D5A27)               // Forest Success
val NeWarning = Color(0xFFE9C349)               // Muted Gold
val NeError = Color(0xFFBA1A1A)

// Calm Game Mode Palette (Earthy Dark Forest & Bamboo Gold)
val ForestNight = Color(0xFF141C12)
val ForestNightElevated = Color(0xFF1B2418)
val PanelForest = Color(0xFF233020)
val PanelForestInner = Color(0xFF1B2618)
val ForestLeafGreen = Color(0xFF5F9E4B)
val BambooGold = Color(0xFFE2C15A)
val TerracottaAccent = Color(0xFFD97A62)
val SoftWarmWhite = Color(0xFFFBF9F5)
val MutedEarthySlate = Color(0xFFC8CCBF)
val BorderForest = Color(0xFF33452E)
val WarmClayOrange = Color(0xFFD98C45)

@Immutable
data class MmColors(
    val background: Color,
    val backgroundElevated: Color,
    val panel: Color,
    val panelInner: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val tertiaryContainer: Color,
    val reward: Color,
    val success: Color,
    val error: Color,
    val text: Color,
    val textMuted: Color,
    val border: Color,
    val combo: Color
) {
    companion object {
        val Easy = MmColors(
            background = NeBackground,
            backgroundElevated = NeBackgroundElevated,
            panel = NeSurface,
            panelInner = NeSurfaceContainer,
            primary = NePrimary,
            onPrimary = NeOnPrimary,
            primaryContainer = NePrimaryContainer,
            onPrimaryContainer = NeOnPrimaryContainer,
            secondary = NeSecondary,
            onSecondary = NeOnSecondary,
            secondaryContainer = NeSecondaryContainer,
            onSecondaryContainer = NeOnSecondaryContainer,
            tertiary = NeTertiary,
            tertiaryContainer = NeTertiaryContainer,
            reward = NeSecondary,
            success = NeSuccess,
            error = NeError,
            text = NeTextPrimary,
            textMuted = NeTextSecondary,
            border = NeBorder,
            combo = NeSecondaryContainer
        )

        val Default = MmColors(
            background = ForestNight,
            backgroundElevated = ForestNightElevated,
            panel = PanelForest,
            panelInner = PanelForestInner,
            primary = ForestLeafGreen,
            onPrimary = SoftWarmWhite,
            primaryContainer = PanelForestInner,
            onPrimaryContainer = ForestLeafGreen,
            secondary = BambooGold,
            onSecondary = ForestNight,
            secondaryContainer = PanelForestInner,
            onSecondaryContainer = BambooGold,
            tertiary = TerracottaAccent,
            tertiaryContainer = PanelForestInner,
            reward = BambooGold,
            success = ForestLeafGreen,
            error = TerracottaAccent,
            text = SoftWarmWhite,
            textMuted = MutedEarthySlate,
            border = BorderForest,
            combo = WarmClayOrange
        )
    }
}
