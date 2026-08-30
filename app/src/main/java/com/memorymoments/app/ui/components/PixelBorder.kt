package com.memorymoments.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.memorymoments.app.ui.theme.MmTheme

@Composable
fun PixelBorder(
    modifier: Modifier = Modifier,
    color: Color = MmTheme.colors.secondary.copy(alpha = 0.7f),
    corner: Dp = MmTheme.dimens.lg,
    stroke: Dp = MmTheme.dimens.borderThick,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.pixelCorners(color, corner, stroke),
        content = content
    )
}

fun Modifier.pixelCorners(
    color: Color,
    corner: Dp,
    stroke: Dp
): Modifier = drawWithContent {
    drawContent()
    val arm = corner.toPx()
    val t = stroke.toPx()
    val w = size.width
    val h = size.height
    drawRect(color, Offset(0f, 0f), Size(arm, t))
    drawRect(color, Offset(0f, 0f), Size(t, arm))
    drawRect(color, Offset(w - arm, 0f), Size(arm, t))
    drawRect(color, Offset(w - t, 0f), Size(t, arm))
    drawRect(color, Offset(0f, h - t), Size(arm, t))
    drawRect(color, Offset(0f, h - arm), Size(t, arm))
    drawRect(color, Offset(w - arm, h - t), Size(arm, t))
    drawRect(color, Offset(w - t, h - arm), Size(t, arm))
}
