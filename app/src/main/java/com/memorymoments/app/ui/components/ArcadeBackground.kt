package com.memorymoments.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.memorymoments.app.ui.theme.MmTheme

@Composable
fun ArcadeBackground(modifier: Modifier = Modifier) {
    val colors = MmTheme.colors
    val isEasy = MmTheme.isEasyMode

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Subtle decorative Northeast India lattice pattern
        HeritageBackgroundPattern(
            patternColor = colors.primary,
            opacity = if (isEasy) 0.035f else 0.055f
        )
    }
}

