package com.memorymoments.app.ui.screens.placesgame

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Place
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.memorymoments.app.R
import com.memorymoments.app.game.places.PlacesOption
import com.memorymoments.app.model.TextSizeOption
import com.memorymoments.app.repository.GameSettingsRepository
import com.memorymoments.app.ui.components.ArcadeHeader
import com.memorymoments.app.ui.components.ArcadeScreen
import com.memorymoments.app.ui.components.ArcadeTopBar
import com.memorymoments.app.ui.components.RetroButton
import com.memorymoments.app.ui.components.RetroButtonStyle
import com.memorymoments.app.ui.components.RetroConfirmDialog
import com.memorymoments.app.ui.components.RetroPanel
import com.memorymoments.app.ui.components.XpBar
import com.memorymoments.app.ui.screens.game.GameSessionResult
import com.memorymoments.app.ui.theme.MmTheme
import com.memorymoments.app.utils.HapticManager
import com.memorymoments.app.utils.SoundManager
import java.io.File

@Composable
fun PlacesGameScreen(
    onBack: () -> Unit,
    onFinish: (GameSessionResult) -> Unit,
    onAddPlaces: () -> Unit,
    viewModel: PlacesGameViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val settingsRepo = remember { GameSettingsRepository(context) }
    val hapticManager = remember { HapticManager(context) }

    val soundEnabled by settingsRepo.soundEffects.collectAsStateWithLifecycle(initialValue = true)
    val hapticsEnabled by settingsRepo.haptics.collectAsStateWithLifecycle(initialValue = true)
    val textSize by settingsRepo.textSize.collectAsStateWithLifecycle(initialValue = TextSizeOption.DEFAULT)

    var showQuitDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = state is PlacesGameState.Playing) {
        showQuitDialog = true
    }

    LaunchedEffect(state) {
        if (state is PlacesGameState.Completed) {
            SoundManager.playResults(soundEnabled)
            hapticManager.success(hapticsEnabled)
            onFinish((state as PlacesGameState.Completed).result)
        }
    }

    PlacesGameScreenContent(
        state = state,
        textSize = textSize,
        onBack = {
            if (state is PlacesGameState.Playing) {
                showQuitDialog = true
            } else {
                onBack()
            }
        },
        onAddPlaces = onAddPlaces,
        onOptionClick = { optionId ->
            val playingState = state as? PlacesGameState.Playing
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
        }
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
private fun PlacesGameScreenContent(
    state: PlacesGameState,
    textSize: TextSizeOption,
    onBack: () -> Unit,
    onAddPlaces: () -> Unit,
    onOptionClick: (String) -> Unit
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
            ArcadeTopBar(
                title = stringResource(R.string.game_places_title),
                onBack = onBack
            )

            when (state) {
                is PlacesGameState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                }

                is PlacesGameState.NotEnoughPlaces -> {
                    NotEnoughPlacesPanel(onAddPlaces = onAddPlaces)
                }

                is PlacesGameState.Playing -> {
                    PlacesPlayingContent(
                        state = state,
                        textSize = textSize,
                        onOptionClick = onOptionClick
                    )
                }

                is PlacesGameState.Completed -> {
                    // Handled in LaunchedEffect
                }
            }
        }
    }
}

@Composable
private fun PlacesPlayingContent(
    state: PlacesGameState.Playing,
    textSize: TextSizeOption,
    onOptionClick: (String) -> Unit
) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens
    val context = LocalContext.current
    val target = state.question.targetPlace

    // Header Round & Score
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Round ${state.roundNumber} of ${state.totalRounds}",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.primary
        )
        Text(
            text = "Score: ${state.stars} ⭐",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.secondary
        )
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Place Large Photo Card
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f),
        shape = RoundedCornerShape(20.dp),
        color = colors.panel,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 1.dp
    ) {
        val photoUri = target.displayPhotoUri
        if (!photoUri.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(File(photoUri))
                    .crossfade(true)
                    .build(),
                contentDescription = target.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Landscape,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(64.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Question Prompt
    Text(
        text = stringResource(R.string.game_places_prompt),
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        ),
        color = colors.primary,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Large Stacked Answer Option Buttons (min 56dp)
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        state.question.options.forEach { option ->
            val isSelected = option.id == state.selectedOptionId
            val isResolved = state.selectedOptionId != null
            val isCorrect = option.isCorrect

            val cardBg = when {
                isResolved && isCorrect -> colors.primaryContainer.copy(alpha = 0.6f)
                isResolved && isSelected && !isCorrect -> colors.panelInner
                isSelected -> colors.primaryContainer.copy(alpha = 0.3f)
                else -> colors.panel
            }

            val borderColor = when {
                isResolved && isCorrect -> colors.primary
                isResolved && isSelected && !isCorrect -> colors.secondary
                else -> colors.border
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isResolved) { onOptionClick(option.id) },
                shape = RoundedCornerShape(18.dp),
                color = cardBg,
                border = BorderStroke(if (isResolved && isCorrect) 2.dp else 1.dp, borderColor),
                shadowElevation = if (isSelected) 2.dp else 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = option.text,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = colors.text,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    // Feedback Banner
    AnimatedVisibility(
        visible = state.selectedOptionId != null,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(200))
    ) {
        Spacer(modifier = Modifier.height(14.dp))
        val correctPlaceName = target.name
        if (state.isCorrect == true) {
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
                        text = "✓ THAT'S RIGHT! +10 XP",
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
                        contentDescription = "Info",
                        tint = colors.secondary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "THAT'S OK — The answer was $correctPlaceName",
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
private fun NotEnoughPlacesPanel(onAddPlaces: () -> Unit) {
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
                imageVector = Icons.Filled.Place,
                contentDescription = null,
                tint = colors.secondary,
                modifier = Modifier.size(54.dp)
            )
            Text(
                text = stringResource(R.string.game_places_empty_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = colors.primary,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.game_places_empty_desc),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textMuted,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            RetroButton(
                text = "+ ADD PLACE",
                icon = Icons.Filled.Add,
                onClick = onAddPlaces,
                style = RetroButtonStyle.Primary,
                minHeight = 56.dp
            )
        }
    }
}
