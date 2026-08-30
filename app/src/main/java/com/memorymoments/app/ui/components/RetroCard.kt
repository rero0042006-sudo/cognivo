package com.memorymoments.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.memorymoments.app.ui.theme.MmTheme

@Composable
fun RetroCard(
    modifier: Modifier = Modifier,
    borderColor: Color = MmTheme.colors.border,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens
    val shapes = MmTheme.shapes
    Column(
        modifier = modifier
            .background(colors.panel, shapes.card)
            .border(dimens.borderThick, borderColor, shapes.card)
            .padding(dimens.md),
        content = content
    )
}
