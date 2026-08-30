package com.memorymoments.app.ui.screens.game

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.memorymoments.app.R
import com.memorymoments.app.model.AnswerOption
import com.memorymoments.app.model.TextSizeOption
import com.memorymoments.app.repository.GameSettingsRepository
import com.memorymoments.app.ui.components.ArcadeHeader
import com.memorymoments.app.ui.components.ArcadeScreen
import com.memorymoments.app.ui.components.ArcadeTopBar
import com.memorymoments.app.ui.components.MemberPortrait
import com.memorymoments.app.ui.components.RetroButton
import com.memorymoments.app.ui.components.RetroButtonStyle
import com.memorymoments.app.ui.components.RetroConfirmDialog
import com.memorymoments.app.ui.components.RetroPanel
import com.memorymoments.app.ui.components.XpBar
import com.memorymoments.app.ui.theme.MmTheme
import com.memorymoments.app.utils.HapticManager
import com.memorymoments.app.utils.SoundManager

@Composable
fun GameScreen(
    onBack: () -> Unit,
    onFinish: (GameSessionResult) -> Unit,
    onAddFamily: () -> Unit = {},
    viewModel: GameViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val settingsRepo = remember { GameSettingsRepository(context) }
    val hapticManager = remember { HapticManager(context) }

    val soundEnabled by settingsRepo.soundEffects.collectAsStateWithLifecycle(initialValue = true)
    val hapticsEnabled by settingsRepo.haptics.collectAsStateWithLifecycle(initialValue = true)
    val textSize by settingsRepo.textSize.collectAsStateWithLifecycle(initialValue = TextSizeOption.DEFAULT)

    var showQuitDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = state is GameState.Playing) {
        showQuitDialog = true
    }

    LaunchedEffect(state) {
        if (state is GameState.Completed) {
            SoundManager.playResults(soundEnabled)
            hapticManager.success(hapticsEnabled)
            onFinish((state as GameState.Completed).result)
        }
    }

    GameScreenContent(
        state = state,
        textSize = textSize,
        onBack = {
            if (state is GameState.Playing) {
                showQuitDialog = true
            } else {
                onBack()
            }
        },
        onAddFamily = onAddFamily,
        onOptionClick = { optionId ->
            val playingState = state as? GameState.Playing
            if (playingState != null && playingState.selectedOptionId == null) {
                val opt = playingState.question.options.firstOrNull { it.id == optionId }
                if (opt?.isCorrect == true) {
                    SoundManager.playCorrect(soundEnabled)
                    hapticManager.success(hapticsEnabled)
                } else {
                    SoundManager.playIncorrect(soundEnabled)
                    hapticManager.neutralRetry(hapticsEnabled)
                }
            }
            viewModel.onOptionSelected(optionId)
        },
        onNextRound = viewModel::advanceNextRound
    )

    if (showQuitDialog) {
        RetroConfirmDialog(
            title = "LEAVE GAME?",
            message = "Your current game progress will be lost.",
            confirmLabel = "LEAVE GAME",
            cancelLabel = "KEEP PLAYING",
            onConfirm = {
                showQuitDialog = false
                onBack()
            },
            onCancel = {
                showQuitDialog = false
            }
        )
    }
}

@Composable
fun GameScreenContent(
    state: GameState,
    textSize: TextSizeOption,
    onBack: () -> Unit,
    onAddFamily: () -> Unit,
    onOptionClick: (String) -> Unit,
    onNextRound: () -> Unit
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
            ArcadeTopBar(title = stringResource(R.string.game_whos_who_title), onBack = onBack)

            when (val current = state) {
                is GameState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                }

                is GameState.NotEnoughFamily -> {
                    NotEnoughFamilyPanel(onAddFamily = onAddFamily)
                }

                is GameState.Playing -> {
                    PlayingGameContent(
                        state = current,
                        textSize = textSize,
                        onOptionClick = onOptionClick,
                        onNextRound = onNextRound
                    )
                }

                is GameState.Completed -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "GAME COMPLETE!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = colors.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotEnoughFamilyPanel(onAddFamily: () -> Unit) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.panel,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = colors.secondary,
                modifier = Modifier.size(54.dp)
            )
            Text(
                text = "ADD AT LEAST 3 FAMILY MEMBERS TO PLAY",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = colors.primary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Add photos and names of your family members first. The game uses them to build gentle memory recognition rounds.",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textMuted,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            RetroButton(
                text = "+ ADD FAMILY",
                icon = Icons.Filled.Add,
                onClick = onAddFamily,
                style = RetroButtonStyle.Primary,
                minHeight = 56.dp
            )
        }
    }
}

@Composable
private fun PlayingGameContent(
    state: GameState.Playing,
    textSize: TextSizeOption,
    onOptionClick: (String) -> Unit,
    onNextRound: () -> Unit
) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens

    // Clean Progress & Round Header
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.game_round_counter, state.roundNumber, state.totalRounds),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.primary
        )
        Text(
            text = stringResource(R.string.stars_label, state.stars),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.secondary
        )
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Question Prompt Banner
    val promptFontSize = (24f * textSize.scaleFactor).sp
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.panel,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 1.dp
    ) {
        Text(
            text = state.question.displayText,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = promptFontSize,
                fontWeight = FontWeight.Bold
            ),
            color = colors.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        )
    }

    Spacer(modifier = Modifier.height(18.dp))

    // 2x2 Option Choices Grid (Accessible, High Contrast, Min 56dp Touch Target)
    val chunkedOptions = state.question.options.chunked(2)
    chunkedOptions.forEach { rowOptions ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            rowOptions.forEach { option ->
                val accessibleLabel = if (option.isCorrect) {
                    state.question.targetMember.name
                } else {
                    "Game character"
                }

                GameChoiceCard(
                    option = option,
                    accessibleLabel = accessibleLabel,
                    isSelected = option.id == state.selectedOptionId,
                    isRoundResolved = state.selectedOptionId != null,
                    isCorrectSelection = state.isCorrect == true && option.id == state.selectedOptionId,
                    isIncorrectSelection = state.isCorrect == false && option.id == state.selectedOptionId,
                    onClick = { onOptionClick(option.id) },
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                )
            }
            repeat(2 - rowOptions.size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
    }

    // Gentle Feedback Notice (Zero Flashing, Supportive Phrasing)
    AnimatedVisibility(
        visible = state.selectedOptionId != null,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(200))
    ) {
        if (state.isCorrect == true) {
            val encouragementText = state.question.encouragement
                ?: "THAT'S RIGHT! +10 XP"
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.primaryContainer.copy(alpha = 0.6f),
                border = BorderStroke(1.5.dp, colors.primary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Correct",
                        tint = colors.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "✓ $encouragementText",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (state.isCorrect == false) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.panelInner,
                border = BorderStroke(1.5.dp, colors.secondary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Gentle retry",
                        tint = colors.secondary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "THAT'S OK — LET'S TRY AGAIN.",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.text,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun GameChoiceCard(
    option: AnswerOption,
    accessibleLabel: String,
    isSelected: Boolean,
    isRoundResolved: Boolean,
    isCorrectSelection: Boolean,
    isIncorrectSelection: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MmTheme.colors

    val targetBorderColor = when {
        isCorrectSelection -> colors.primary
        isIncorrectSelection -> colors.secondary
        isSelected -> colors.primary
        else -> colors.border
    }

    val animatedBorderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = tween(200),
        label = "choiceBorder"
    )

    Surface(
        modifier = modifier
            .semantics {
                role = Role.Button
                contentDescription = accessibleLabel
            }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = colors.panel,
        border = BorderStroke(
            width = if (isSelected) 3.dp else 1.5.dp,
            color = animatedBorderColor
        ),
        shadowElevation = if (isSelected) 2.dp else 1.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            MemberPortrait(
                imageUri = option.imageUri,
                contentDescription = accessibleLabel,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
