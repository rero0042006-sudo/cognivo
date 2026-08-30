package com.memorymoments.app.ui.screens.distractor

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.memorymoments.app.model.DistractorCharacter
import com.memorymoments.app.ui.components.ArcadeScreen
import com.memorymoments.app.ui.components.ArcadeTopBar
import com.memorymoments.app.ui.components.MemberPortrait
import com.memorymoments.app.ui.components.RetroButton
import com.memorymoments.app.ui.components.RetroButtonStyle
import com.memorymoments.app.ui.components.RetroPanel
import com.memorymoments.app.ui.theme.MmTheme
import com.memorymoments.app.ui.theme.rememberReduceMotion
import kotlinx.coroutines.delay

@Composable
fun DistractorLabScreen(
    onBack: () -> Unit,
    onReady: () -> Unit,
    viewModel: DistractorLabViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    DistractorLabContent(
        state = state,
        onBack = onBack,
        onContinue = onReady,
        onRetry = viewModel::retry,
        onUseDemo = viewModel::useDemoDistractors,
        onRegenerateAi = viewModel::regenerateAiDistractors
    )
}

@Composable
fun DistractorLabContent(
    state: DistractorLabUiState,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onRetry: () -> Unit,
    onUseDemo: () -> Unit,
    onRegenerateAi: () -> Unit
) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens
    ArcadeScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = dimens.screenHorizontal, vertical = dimens.screenVertical)
        ) {
            ArcadeTopBar(title = "Distractor Lab", onBack = onBack)
            Text(
                text = "DISTRACTOR LAB",
                style = MmTheme.arcade.hud,
                color = colors.reward
            )
            Spacer(modifier = Modifier.height(dimens.lg))
            val isHard = state.style == com.memorymoments.app.model.DistractorStyle.CHALLENGE
            when (val phase = state.phase) {
                DistractorLabState.Idle,
                is DistractorLabState.Generating -> GeneratingPanel(phase, isHardMode = isHard)
                is DistractorLabState.Success -> SuccessPanel(
                    characters = phase.characters,
                    isHardMode = isHard,
                    onContinue = onContinue,
                    onRegenerate = onRegenerateAi
                )
                is DistractorLabState.Error -> ErrorPanel(
                    offline = phase.offline,
                    message = phase.message,
                    onRetry = onRetry,
                    onUseDemo = onUseDemo
                )
            }
        }
    }
}

@Composable
private fun GeneratingPanel(phase: DistractorLabState, isHardMode: Boolean = false) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens
    val reduceMotion = rememberReduceMotion()
    var pulseStep by remember { mutableIntStateOf(0) }
    var animating by remember { mutableIntStateOf(1) }
    LaunchedEffect(reduceMotion) {
        if (reduceMotion) return@LaunchedEffect
        repeat(10) { step ->
            pulseStep = step
            delay(450)
        }
        animating = 0
    }
    val glow by animateFloatAsState(
        targetValue = if (animating == 1 && !reduceMotion && pulseStep % 2 == 0) 1f else 0.55f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "starGlow"
    )
    val progress = phase as? DistractorLabState.Generating
    val completed = progress?.completed ?: 0
    val total = progress?.total ?: 1
    RetroPanel(borderColor = colors.secondary.copy(alpha = 0.85f)) {
        Text(
            text = "✨  ✨  ✨",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.reward,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(glow)
        )
        Spacer(modifier = Modifier.height(dimens.xl))
        Text(
            text = "GENERATING...",
            style = MmTheme.arcade.hud,
            color = colors.reward,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(dimens.md))
        Text(
            text = if (isHardMode) "Creating similar fictional characters..." else "Generating fictional game characters...",
            style = MaterialTheme.typography.titleLarge,
            color = colors.text,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(dimens.md))
        Text(
            text = "%02d / %02d".format(completed.coerceAtMost(total), total),
            style = MmTheme.arcade.titleSmall,
            color = colors.secondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        progress?.preview?.let { preview ->
            Spacer(modifier = Modifier.height(dimens.lg))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                MemberPortrait(
                    imageUri = preview.imageUri,
                    contentDescription = "Game character",
                    size = 160.dp
                )
            }
        }
        Spacer(modifier = Modifier.height(dimens.lg))
        Text(
            text = if (isHardMode) {
                "Hard mode uses AI to analyze broad visual features such as age group, hair and glasses. Generated characters are fictional and are not copies of your family members."
            } else {
                "Making fictional characters for the game. Your family photos stay on this device."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SuccessPanel(
    characters: List<DistractorCharacter>,
    isHardMode: Boolean,
    onContinue: () -> Unit,
    onRegenerate: () -> Unit
) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens
    val isDemoPool = characters.any { it.source == DistractorCharacter.Source.DEMO }
    RetroPanel(borderColor = colors.secondary.copy(alpha = 0.85f)) {
        Text(
            text = if (isDemoPool) "DEMO CHARACTERS LOADED" else if (isHardMode) "HARD MODE CHARACTERS READY" else "GAME CHARACTERS READY",
            style = MmTheme.arcade.hud,
            color = colors.reward,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(dimens.sm))
        Text(
            text = if (isDemoPool) {
                "Showing placeholder demo avatars. Tap below to create new fictional characters."
            } else if (isHardMode) {
                "Hard mode uses AI to analyze broad visual features such as age group, hair and glasses. Generated characters are fictional and are not copies of your family members."
            } else {
                "Fictional game characters are saved on this device for recognition questions."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(dimens.lg))
        characters.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimens.sm)
            ) {
                row.forEach { character ->
                    MemberPortrait(
                        imageUri = character.imageUri,
                        contentDescription = "Game character",
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                    )
                }
                repeat(2 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(dimens.sm))
        }
        Spacer(modifier = Modifier.height(dimens.md))
        RetroButton(
            text = "CONTINUE",
            onClick = onContinue,
            style = RetroButtonStyle.Primary,
            minHeight = dimens.playButtonMin
        )
        Spacer(modifier = Modifier.height(dimens.sm))
        RetroButton(
            text = "GENERATE MORE CHARACTERS",
            onClick = onRegenerate,
            style = RetroButtonStyle.Secondary
        )
    }
}

@Composable
private fun ErrorPanel(
    offline: Boolean,
    message: String?,
    onRetry: () -> Unit,
    onUseDemo: () -> Unit
) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens
    RetroPanel(borderColor = colors.error.copy(alpha = 0.85f)) {
        Text(
            text = if (offline) "NO INTERNET CONNECTION" else "COULDN'T CREATE CHARACTERS",
            style = MmTheme.arcade.hud,
            color = colors.reward,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(dimens.md))
        Text(
            text = when {
                offline -> "You can still play using saved or demo characters."
                !message.isNullOrBlank() -> message
                else -> "We couldn't create AI game characters right now. Demo characters still work."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = colors.text,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(dimens.xl))
        if (!offline) {
            RetroButton(
                text = "RETRY",
                onClick = onRetry,
                style = RetroButtonStyle.Primary,
                minHeight = dimens.playButtonMin
            )
            Spacer(modifier = Modifier.height(dimens.md))
        }
        RetroButton(
            text = "USE DEMO DISTRACTORS",
            onClick = onUseDemo,
            style = RetroButtonStyle.Ghost
        )
    }
}
