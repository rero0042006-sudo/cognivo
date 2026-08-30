package com.memorymoments.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.memorymoments.app.ui.theme.MmTheme

@Composable
fun LevelBadge(
    levelLabel: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val colors = MmTheme.colors
    val text = if (compact) "LV $levelLabel" else "LEVEL $levelLabel"
    Text(
        text = text,
        style = MmTheme.arcade.hud,
        color = colors.reward,
        modifier = modifier.semantics { contentDescription = "Level $levelLabel" }
    )
}

@Composable
fun StarCounter(
    stars: Int,
    modifier: Modifier = Modifier,
    padded: Boolean = false,
    label: String = "STARS"
) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens
    val value = if (padded) stars.toString().padStart(3, '0') else stars.toString()
    Row(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = "$stars stars"
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.sm)
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = colors.reward,
            modifier = Modifier.size(dimens.xl)
        )
        Text(
            text = if (label.isEmpty()) value else "$value $label",
            style = MaterialTheme.typography.titleMedium,
            color = colors.text
        )
    }
}

@Composable
fun ComboBadge(
    combo: Int,
    modifier: Modifier = Modifier,
    label: String? = null
) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens
    val text = label ?: "BEST COMBO $combo"
    Row(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = "Combo $combo"
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.sm)
    ) {
        Icon(
            imageVector = Icons.Filled.LocalFireDepartment,
            contentDescription = null,
            tint = colors.combo,
            modifier = Modifier.size(dimens.xl)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = colors.combo
        )
    }
}

@Composable
fun ScoreDisplay(
    score: Int,
    modifier: Modifier = Modifier
) {
    val colors = MmTheme.colors
    Column(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = "Score $score"
        },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SCORE",
            style = MmTheme.arcade.label,
            color = colors.secondary
        )
        Spacer(modifier = Modifier.height(MmTheme.dimens.sm))
        Text(
            text = score.toString(),
            style = MmTheme.arcade.titleSmall,
            color = colors.reward,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun XpBar(
    current: Int,
    max: Int,
    modifier: Modifier = Modifier
) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens
    val shapes = MmTheme.shapes
    val progress = if (max <= 0) 0f else (current.toFloat() / max).coerceIn(0f, 1f)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = "XP $current of $max"
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "XP", style = MmTheme.arcade.label, color = colors.secondary)
            Text(
                text = "$current / $max",
                style = MaterialTheme.typography.labelMedium,
                color = colors.text
            )
        }
        Spacer(modifier = Modifier.height(dimens.sm))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimens.md)
                .background(colors.backgroundElevated, shapes.bar)
        ) {
            val widthFraction = if (current == 0) 0f else progress.coerceAtLeast(0.04f)
            Box(
                modifier = Modifier
                    .fillMaxWidth(widthFraction)
                    .fillMaxHeight()
                    .background(colors.reward, shapes.bar)
            )
        }
    }
}

@Composable
fun ArcadeHeader(
    levelLabel: String,
    stars: Int,
    combo: Int,
    modifier: Modifier = Modifier
) {
    RetroPanel(modifier = modifier, borderColor = MmTheme.colors.primary.copy(alpha = 0.7f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LevelBadge(levelLabel = levelLabel, compact = true)
            StarCounter(stars = stars, padded = true, label = "")
            ComboBadge(combo = combo, label = "x$combo")
        }
    }
}
