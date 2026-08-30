package com.memorymoments.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.memorymoments.app.ui.theme.MmTheme

/**
 * A subtle, reusable decorative background pattern inspired by Northeast India's
 * bamboo lattices and geometric woven textile motifs.
 * Keeps opacity extremely low (2–4%) so text and interactive content remain fully accessible.
 */
@Composable
fun HeritageBackgroundPattern(
    modifier: Modifier = Modifier,
    patternColor: Color = MmTheme.colors.primary,
    opacity: Float = 0.035f,
    spacing: Dp = 28.dp
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val strokeColor = patternColor.copy(alpha = opacity)
        val spacingPx = spacing.toPx()
        val strokeWidth = 1.dp.toPx()

        // 45-degree diagonal lattice lines (bamboo weave motif)
        var x = -size.height
        while (x < size.width + size.height) {
            drawLine(
                color = strokeColor,
                start = Offset(x, 0f),
                end = Offset(x + size.height, size.height),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = strokeColor,
                start = Offset(x, size.height),
                end = Offset(x + size.height, 0f),
                strokeWidth = strokeWidth
            )
            x += spacingPx
        }
    }
}
