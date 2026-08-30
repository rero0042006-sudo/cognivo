package com.memorymoments.app.ui.screens.musicgame

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.memorymoments.app.R
import com.memorymoments.app.game.music.MusicOption
import com.memorymoments.app.repository.GameSettingsRepository
import com.memorymoments.app.ui.components.ArcadeScreen
import com.memorymoments.app.ui.components.ArcadeTopBar
import com.memorymoments.app.ui.components.RetroButton
import com.memorymoments.app.ui.components.RetroButtonStyle
import com.memorymoments.app.ui.components.RetroConfirmDialog
import com.memorymoments.app.ui.screens.game.GameSessionResult
import com.memorymoments.app.ui.theme.MmTheme
import com.memorymoments.app.utils.HapticManager
import com.memorymoments.app.utils.SoundManager

@Composable
fun MusicGameScreen(
    onBack: () -> Unit,
    onFinish: (GameSessionResult) -> Unit,
    onAddSongs: () -> Unit,
    viewModel: MusicGameViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isAudioPlaying by viewModel.isAudioPlaying.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val settingsRepo = remember { GameSettingsRepository(context) }
    val soundEnabled by settingsRepo.soundEffects.collectAsStateWithLifecycle(initialValue = true)
    val hapticsEnabled by settingsRepo.haptics.collectAsStateWithLifecycle(initialValue = true)
    val hapticManager = remember { HapticManager(context) }

    var showQuitDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.audioPlayer.stop()
        }
    }

    LaunchedEffect(state) {
        if (state is MusicGameState.Completed) {
            onFinish((state as MusicGameState.Completed).result)
        }
    }

    val colors = MmTheme.colors
    val dimens = MmTheme.dimens

    ArcadeScreen {
        when (val currentState = state) {
            is MusicGameState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.primary)
                }
            }

            is MusicGameState.NotEnoughMusic -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    ArcadeTopBar(title = stringResource(R.string.game_music_title), onBack = onBack)
                    Spacer(modifier = Modifier.height(24.dp))
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
                                imageVector = Icons.Filled.MusicNote,
                                contentDescription = null,
                                tint = colors.secondary,
                                modifier = Modifier.size(54.dp)
                            )
                            Text(
                                text = "ADD MORE MUSIC",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = colors.primary
                            )
                            Text(
                                text = "Add at least 3 songs to your music library to play Name That Tune.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.textMuted,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            RetroButton(
                                text = "+ ADD SONGS",
                                icon = Icons.Filled.Add,
                                onClick = onAddSongs,
                                style = RetroButtonStyle.Primary,
                                minHeight = 56.dp
                            )
                        }
                    }
                }
            }

            is MusicGameState.Playing -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ArcadeTopBar(
                        title = stringResource(R.string.game_music_title),
                        onBack = { showQuitDialog = true }
                    )

                    // Round Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Round ${currentState.roundNumber} of ${currentState.totalRounds}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.primary
                        )
                        Text(
                            text = "Score: ${currentState.stars} ⭐",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.secondary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Audio Clip Banner (Play / Pause Button)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleAudio() },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isAudioPlaying) colors.primaryContainer.copy(alpha = 0.5f) else colors.panel,
                        border = BorderStroke(1.5.dp, if (isAudioPlaying) colors.primary else colors.border),
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(56.dp),
                                shape = CircleShape,
                                color = if (isAudioPlaying) colors.primary else colors.secondaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isAudioPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        contentDescription = if (isAudioPlaying) "Pause clip" else "Play clip",
                                        tint = if (isAudioPlaying) colors.onPrimary else colors.onSecondaryContainer,
                                        modifier = Modifier.size(34.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isAudioPlaying) "PLAYING SONG..." else "TAP TO HEAR SONG",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 19.sp
                                    ),
                                    color = colors.text
                                )
                                Text(
                                    text = stringResource(R.string.game_music_listen),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textMuted
                                )
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = colors.secondary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Question Prompt
                    Text(
                        text = stringResource(R.string.game_music_prompt),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = colors.primary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Large Answer Choice Buttons
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        currentState.question.options.forEach { option ->
                            val isSelected = currentState.selectedOptionId == option.id
                            val showFeedback = currentState.selectedOptionId != null

                            val optionBorder = when {
                                showFeedback && option.isCorrect -> colors.primary
                                showFeedback && isSelected && !option.isCorrect -> colors.secondary
                                isSelected -> colors.primary
                                else -> colors.border
                            }

                            val optionBg = when {
                                showFeedback && option.isCorrect -> colors.primaryContainer.copy(alpha = 0.6f)
                                showFeedback && isSelected && !option.isCorrect -> colors.panelInner
                                else -> colors.panel
                            }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = currentState.selectedOptionId == null) {
                                        if (option.isCorrect) {
                                            SoundManager.playCorrect(soundEnabled)
                                            hapticManager.success(hapticsEnabled)
                                        } else {
                                            SoundManager.playIncorrect(soundEnabled)
                                            hapticManager.neutralRetry(hapticsEnabled)
                                        }
                                        viewModel.onOptionSelected(option.id)
                                    },
                                shape = RoundedCornerShape(18.dp),
                                color = optionBg,
                                border = BorderStroke(if (showFeedback && option.isCorrect) 2.dp else 1.dp, optionBorder),
                                shadowElevation = if (isSelected) 2.dp else 1.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.MusicNote,
                                        contentDescription = null,
                                        tint = if (showFeedback && option.isCorrect) colors.primary else colors.secondary,
                                        modifier = Modifier.size(28.dp)
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = option.title,
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 20.sp
                                            ),
                                            color = colors.text
                                        )
                                        if (!option.artist.isNullOrBlank()) {
                                            Text(
                                                text = option.artist,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = colors.textMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Feedback Notice
                    if (currentState.isCorrect != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = if (currentState.isCorrect == true) colors.primaryContainer.copy(alpha = 0.6f) else colors.panelInner,
                            border = BorderStroke(1.5.dp, if (currentState.isCorrect == true) colors.primary else colors.secondary)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (currentState.isCorrect == true) "✓ THAT'S RIGHT! +10 XP" else "THAT'S OK — THE SONG WAS:",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (currentState.isCorrect == true) colors.primary else colors.text
                                )
                                Text(
                                    text = "\"${currentState.question.targetSong.title}\"" + (currentState.question.targetSong.artist?.let { " by $it" } ?: ""),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.primary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            is MusicGameState.Completed -> {
                // Handled in LaunchedEffect
            }
        }
    }

    if (showQuitDialog) {
        RetroConfirmDialog(
            title = "LEAVE GAME?",
            message = "Your current game progress will be lost.",
            confirmLabel = "LEAVE GAME",
            cancelLabel = "KEEP PLAYING",
            onConfirm = {
                showQuitDialog = false
                viewModel.audioPlayer.stop()
                onBack()
            },
            onCancel = {
                showQuitDialog = false
            }
        )
    }
}
