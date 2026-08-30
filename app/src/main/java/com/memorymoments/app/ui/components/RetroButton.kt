package com.memorymoments.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memorymoments.app.ui.theme.MmColors
import com.memorymoments.app.ui.theme.MmTheme
import com.memorymoments.app.ui.theme.rememberReduceMotion

enum class RetroButtonStyle { Primary, Secondary, Ghost }

@Composable
fun RetroButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    style: RetroButtonStyle = RetroButtonStyle.Secondary,
    pulse: Boolean = false,
    enabled: Boolean = true,
    minHeight: Dp = MmTheme.dimens.buttonMin
) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens
    val shapes = MmTheme.shapes
    val isEasy = MmTheme.isEasyMode
    val reduceMotion = rememberReduceMotion()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val focused by interaction.collectIsFocusedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = tween(90, easing = FastOutSlowInEasing),
        label = "pressScale"
    )
    val pulseAnim by rememberInfiniteTransition(label = "idlePulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val idlePulse = if (pulse && !reduceMotion && !pressed && !isEasy) pulseAnim else 1f

    if (isEasy) {
        // Modern Calm Stitch Button
        val (bgColor, borderStroke, textColor) = when (style) {
            RetroButtonStyle.Primary -> Triple(
                colors.primary,
                null,
                colors.onPrimary
            )
            RetroButtonStyle.Secondary -> Triple(
                colors.panel,
                BorderStroke(1.5.dp, colors.border),
                colors.text
            )
            RetroButtonStyle.Ghost -> Triple(
                colors.panelInner,
                BorderStroke(1.dp, colors.border),
                colors.primary
            )
        }

        Surface(
            modifier = modifier
                .scale(pressScale)
                .fillMaxWidth()
                .defaultMinSize(minHeight = minHeight)
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick
                ),
            shape = shapes.button,
            color = if (enabled) bgColor else bgColor.copy(alpha = 0.5f),
            border = borderStroke,
            shadowElevation = if (style == RetroButtonStyle.Primary && enabled) 2.dp else 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.xl, vertical = dimens.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier
                            .padding(end = dimens.sm)
                            .size(24.dp)
                    )
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        letterSpacing = 0.02.sp
                    ),
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        // Retro Arcade Chunky Button
        val palette = buttonPalette(style, enabled, focused, pressed, colors)
        val depth = if (pressed) dimens.xs / 2 else dimens.xs

        Box(
            modifier = modifier
                .scale(pressScale * idlePulse)
                .fillMaxWidth()
                .defaultMinSize(minHeight = minHeight)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = depth)
                    .background(palette.shadow, shapes.button)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = minHeight)
                    .background(palette.fill, shapes.button)
                    .border(dimens.borderChunky, palette.border, shapes.button)
                    .semantics(mergeDescendants = true) {
                        contentDescription = text
                        role = Role.Button
                    }
                    .clickable(
                        interactionSource = interaction,
                        indication = null,
                        enabled = enabled,
                        role = Role.Button,
                        onClick = onClick
                    )
                    .padding(PaddingValues(horizontal = dimens.xl, vertical = dimens.md)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = palette.content,
                        modifier = Modifier
                            .padding(end = dimens.sm)
                            .size(dimens.xl)
                    )
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.content,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private data class ButtonPalette(
    val fill: Color,
    val border: Color,
    val content: Color,
    val shadow: Color
)

private fun buttonPalette(
    style: RetroButtonStyle,
    enabled: Boolean,
    focused: Boolean,
    pressed: Boolean,
    colors: MmColors
): ButtonPalette {
    val focusBorder = if (focused || pressed) colors.reward else null
    val dim = if (enabled) 1f else 0.45f
    return when (style) {
        RetroButtonStyle.Primary -> ButtonPalette(
            fill = colors.primary.copy(alpha = 0.92f * dim),
            border = (focusBorder ?: colors.reward).copy(alpha = dim),
            content = colors.text.copy(alpha = dim),
            shadow = colors.primary.copy(alpha = 0.35f * dim)
        )
        RetroButtonStyle.Secondary -> ButtonPalette(
            fill = colors.panel.copy(alpha = dim),
            border = (focusBorder ?: colors.secondary).copy(alpha = dim),
            content = colors.text.copy(alpha = dim),
            shadow = colors.secondary.copy(alpha = 0.22f * dim)
        )
        RetroButtonStyle.Ghost -> ButtonPalette(
            fill = colors.background.copy(alpha = 0.35f * dim),
            border = (focusBorder ?: colors.reward).copy(alpha = 0.75f * dim),
            content = colors.reward.copy(alpha = dim),
            shadow = Color.Transparent
        )
    }
}
