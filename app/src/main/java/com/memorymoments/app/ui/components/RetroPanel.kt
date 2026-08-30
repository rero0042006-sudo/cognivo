package com.memorymoments.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.memorymoments.app.ui.theme.MmTheme

@Composable
fun RetroPanel(
    modifier: Modifier = Modifier,
    borderColor: Color = MmTheme.colors.secondary.copy(alpha = 0.75f),
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens
    val shapes = MmTheme.shapes
    Column(
        modifier = modifier
            .fillMaxWidth()
            .offset(y = dimens.xs)
            .background(colors.primary.copy(alpha = 0.18f), shapes.panel)
            .offset(y = -dimens.xs)
            .background(colors.panel, shapes.panel)
            .border(dimens.borderThick, borderColor, shapes.panel)
            .padding(dimens.xs)
            .background(colors.panelInner, shapes.card)
            .padding(horizontal = dimens.lg, vertical = dimens.lg),
        content = content
    )
}
