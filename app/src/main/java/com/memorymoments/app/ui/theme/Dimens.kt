package com.memorymoments.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class MmDimens(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val screenHorizontal: Dp = 20.dp,
    val screenVertical: Dp = 16.dp,
    val touch: Dp = 48.dp,
    val buttonMin: Dp = 56.dp,
    val playButtonMin: Dp = 72.dp,
    val border: Dp = 2.dp,
    val borderThick: Dp = 3.dp,
    val borderChunky: Dp = 4.dp,
    val elevation: Dp = 4.dp,
    val contentMax: Dp = 560.dp,
    val portraitSlot: Dp = 96.dp
)
