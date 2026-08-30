package com.memorymoments.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.memorymoments.app.model.UiMode

val LocalMmColors = staticCompositionLocalOf { MmColors.Easy }
val LocalMmDimens = staticCompositionLocalOf { MmDimens() }
val LocalMmShapes = staticCompositionLocalOf { MmShapes() }
val LocalArcadeType = staticCompositionLocalOf { ArcadeTypography() }
val LocalUiMode = staticCompositionLocalOf { UiMode.EASY }

object MmTheme {
    val colors: MmColors
        @Composable
        @ReadOnlyComposable
        get() = LocalMmColors.current

    val dimens: MmDimens
        @Composable
        @ReadOnlyComposable
        get() = LocalMmDimens.current

    val shapes: MmShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalMmShapes.current

    val arcade: ArcadeTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalArcadeType.current

    val uiMode: UiMode
        @Composable
        @ReadOnlyComposable
        get() = LocalUiMode.current

    val isEasyMode: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalUiMode.current == UiMode.EASY
}

@Composable
fun MemoryMomentsTheme(
    uiMode: UiMode = UiMode.EASY,
    content: @Composable () -> Unit
) {
    val isEasy = uiMode == UiMode.EASY
    val colors = remember(isEasy) {
        if (isEasy) MmColors.Easy else MmColors.Default
    }
    val dimens = remember { MmDimens() }
    val shapes = remember { MmShapes() }
    val arcade = remember { ArcadeTypography() }

    val scheme = if (isEasy) {
        lightColorScheme(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            secondary = colors.secondary,
            onSecondary = colors.onSecondary,
            tertiary = colors.reward,
            onTertiary = colors.panel,
            background = colors.background,
            onBackground = colors.text,
            surface = colors.panel,
            onSurface = colors.text,
            surfaceVariant = colors.backgroundElevated,
            onSurfaceVariant = colors.textMuted,
            outline = colors.border,
            error = colors.error,
            onError = colors.panel
        )
    } else {
        darkColorScheme(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            secondary = colors.secondary,
            onSecondary = colors.onSecondary,
            tertiary = colors.reward,
            onTertiary = colors.background,
            background = colors.background,
            onBackground = colors.text,
            surface = colors.panel,
            onSurface = colors.text,
            surfaceVariant = colors.backgroundElevated,
            onSurfaceVariant = colors.textMuted,
            outline = colors.border,
            error = colors.error,
            onError = colors.text
        )
    }

    CompositionLocalProvider(
        LocalMmColors provides colors,
        LocalMmDimens provides dimens,
        LocalMmShapes provides shapes,
        LocalArcadeType provides arcade,
        LocalUiMode provides uiMode
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = Typography,
            content = content
        )
    }
}
