package com.memorymoments.app.ui.screens.gamesetup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.memorymoments.app.model.DistractorStyle
import com.memorymoments.app.model.GameMode
import com.memorymoments.app.ui.components.ArcadeScreen
import com.memorymoments.app.ui.components.ArcadeTopBar
import com.memorymoments.app.ui.components.RetroButton
import com.memorymoments.app.ui.components.RetroButtonStyle
import com.memorymoments.app.ui.components.RetroPanel
import com.memorymoments.app.ui.theme.MmTheme

@Composable
fun GameSetupScreen(
    isDemo: Boolean,
    onBack: () -> Unit,
    onStartGame: (DistractorStyle) -> Unit,
    viewModel: GameSetupViewModel = viewModel()
) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens
    val selectedMode by viewModel.mode.collectAsStateWithLifecycle()
    val selectedRounds by viewModel.rounds.collectAsStateWithLifecycle()
    val distractorStyle by viewModel.distractorStyle.collectAsStateWithLifecycle()
    val mode = selectedMode

    ArcadeScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = dimens.screenHorizontal, vertical = dimens.screenVertical)
        ) {
            ArcadeTopBar(title = "Game Setup", onBack = onBack)
            if (isDemo) {
                RetroPanel(borderColor = colors.reward.copy(alpha = 0.8f)) {
                    Text(
                        text = "DEMO MODE",
                        style = MmTheme.arcade.hud,
                        color = colors.reward
                    )
                    Spacer(modifier = Modifier.height(dimens.sm))
                    Text(
                        text = "Try a round without setting up your family yet. Full gameplay arrives in a later phase.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted
                    )
                }
                Spacer(modifier = Modifier.height(dimens.lg))
            }

            Text(
                text = "Difficulty",
                style = MmTheme.arcade.label,
                color = colors.secondary
            )
            Spacer(modifier = Modifier.height(dimens.sm))
            ChoiceRow(
                options = GameMode.entries.map { it.displayName.uppercase() },
                selected = mode.displayName.uppercase(),
                onSelect = { label ->
                    viewModel.setMode(GameMode.entries.first { it.displayName.uppercase() == label })
                }
            )
            Spacer(modifier = Modifier.height(dimens.sm))
            Text(
                text = mode.memberRange + " / " + mode.questionTypes,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMuted
            )

            Spacer(modifier = Modifier.height(dimens.xl))
            Text(
                text = "Game length",
                style = MmTheme.arcade.label,
                color = colors.secondary
            )
            Spacer(modifier = Modifier.height(dimens.sm))
            ChoiceRow(
                options = listOf("5 ROUNDS", "10 ROUNDS", "15 ROUNDS"),
                selected = "$selectedRounds ROUNDS",
                onSelect = { viewModel.setRounds(it.substringBefore(" ").toInt()) }
            )

            Spacer(modifier = Modifier.height(dimens.xl))
            Text(
                text = "DISTRACTOR STYLE",
                style = MmTheme.arcade.label,
                color = colors.secondary
            )
            Spacer(modifier = Modifier.height(dimens.sm))
            ChoiceRow(
                options = DistractorStyle.entries.map { it.displayName.uppercase() },
                selected = distractorStyle.displayName.uppercase(),
                onSelect = { label ->
                    viewModel.setDistractorStyle(
                        DistractorStyle.entries.first { it.displayName.uppercase() == label }
                    )
                }
            )
            Spacer(modifier = Modifier.height(dimens.sm))
            Text(
                text = distractorStyle.caption,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.text
            )

            Spacer(modifier = Modifier.height(dimens.xxl))
            RetroButton(
                text = "START",
                icon = Icons.Filled.PlayArrow,
                onClick = { onStartGame(distractorStyle) },
                style = RetroButtonStyle.Primary,
                minHeight = dimens.playButtonMin
            )
        }
    }
}

@Composable
private fun ChoiceRow(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    val dimens = MmTheme.dimens
    val colors = MmTheme.colors
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val stacked = maxWidth < 420.dp
        if (stacked) {
            Column(verticalArrangement = Arrangement.spacedBy(dimens.sm)) {
                options.forEach { option ->
                    ChoiceChip(
                        label = option,
                        selected = option == selected,
                        onClick = { onSelect(option) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimens.sm)
            ) {
                options.forEach { option ->
                    ChoiceChip(
                        label = option,
                        selected = option == selected,
                        onClick = { onSelect(option) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    RetroButton(
        text = label,
        onClick = onClick,
        modifier = modifier,
        style = if (selected) RetroButtonStyle.Primary else RetroButtonStyle.Ghost,
        minHeight = MmTheme.dimens.touch
    )
}
