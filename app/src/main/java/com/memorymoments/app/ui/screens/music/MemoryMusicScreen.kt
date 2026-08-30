package com.memorymoments.app.ui.screens.music

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.memorymoments.app.model.Memory
import com.memorymoments.app.ui.components.ArcadeScreen
import com.memorymoments.app.ui.components.ArcadeTopBar
import com.memorymoments.app.ui.components.RetroButton
import com.memorymoments.app.ui.components.RetroButtonStyle
import com.memorymoments.app.ui.theme.MmTheme
import java.io.File

@Composable
fun MemoryMusicScreen(
    onBack: () -> Unit,
    onViewMemories: () -> Unit,
    viewModel: MemoryMusicViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentPosMs by viewModel.currentPositionMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.durationMs.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopAudio()
        }
    }

    val colors = MmTheme.colors
    val dimens = MmTheme.dimens

    ArcadeScreen {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.primary)
            }
            return@ArcadeScreen
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = dimens.screenHorizontal, vertical = dimens.screenVertical),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ArcadeTopBar(title = "Memory Music", onBack = onBack)

            Text(
                text = "MEMORY MUSIC",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = colors.reward
            )

            Spacer(modifier = Modifier.height(dimens.xs))

            Text(
                text = "Listen and enjoy familiar songs that bring back memories.",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(dimens.lg))

            val currentSong = state.currentSong
            if (currentSong == null) {
                Text(
                    text = "No songs available in library.",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textMuted
                )
            } else {
                // Large Music Player Vinyl / Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = colors.panel,
                    border = BorderStroke(2.dp, colors.primary)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimens.xl),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(100.dp),
                            shape = CircleShape,
                            color = if (isPlaying) colors.primary else colors.secondary.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.MusicNote,
                                    contentDescription = null,
                                    tint = if (isPlaying) colors.onPrimary else colors.secondary,
                                    modifier = Modifier.size(54.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(dimens.md))

                        Text(
                            text = currentSong.title,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = colors.text,
                            textAlign = TextAlign.Center
                        )

                        if (!currentSong.artist.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(dimens.xs))
                            Text(
                                text = currentSong.artist,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp
                                ),
                                color = colors.secondary,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(dimens.md))

                        // Progress Slider
                        if (durationMs > 0) {
                            Slider(
                                value = currentPosMs.toFloat(),
                                onValueChange = { viewModel.seekTo(it.toInt()) },
                                valueRange = 0f..durationMs.toFloat(),
                                colors = SliderDefaults.colors(
                                    thumbColor = colors.primary,
                                    activeTrackColor = colors.primary,
                                    inactiveTrackColor = colors.border
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = formatMs(currentPosMs),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textMuted
                                )
                                Text(
                                    text = formatMs(durationMs),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(dimens.lg))

                        // Playback Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clickable(onClick = viewModel::previousSong),
                                shape = CircleShape,
                                color = colors.panelInner,
                                border = BorderStroke(1.dp, colors.border)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.SkipPrevious,
                                        contentDescription = "Previous Song",
                                        tint = colors.text,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            // Large Main Play/Pause Button
                            Surface(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clickable(onClick = viewModel::togglePlay),
                                shape = CircleShape,
                                color = colors.primary
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        contentDescription = if (isPlaying) "Pause" else "Play",
                                        tint = colors.onPrimary,
                                        modifier = Modifier.size(42.dp)
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clickable(onClick = viewModel::nextSong),
                                shape = CircleShape,
                                color = colors.panelInner,
                                border = BorderStroke(1.dp, colors.border)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.SkipNext,
                                        contentDescription = "Next Song",
                                        tint = colors.text,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(dimens.xl))

                // Reminiscence Prompt Section
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.panel,
                    border = BorderStroke(1.5.dp, colors.border)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimens.lg)
                    ) {
                        Text(
                            text = "💭 Does this song remind you of something?",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp
                            ),
                            color = colors.reward
                        )

                        Spacer(modifier = Modifier.height(dimens.sm))

                        val memory = state.associatedMemory
                        if (memory != null) {
                            LinkedMemoryPreview(memory = memory, onViewMemories = onViewMemories)
                        } else {
                            Text(
                                text = "Take a moment to listen and share memories with loved ones.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.text
                            )
                            Spacer(modifier = Modifier.height(dimens.md))
                            RetroButton(
                                text = "NEXT SONG",
                                icon = Icons.Filled.SkipNext,
                                onClick = viewModel::nextSong,
                                style = RetroButtonStyle.Secondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimens.xxl))
        }
    }
}

@Composable
private fun LinkedMemoryPreview(
    memory: Memory,
    onViewMemories: () -> Unit
) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens
    val context = LocalContext.current
    val photoUri = memory.photoUris.firstOrNull()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewMemories),
        shape = RoundedCornerShape(12.dp),
        color = colors.panelInner,
        border = BorderStroke(1.dp, colors.secondary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.md)
        ) {
            if (!photoUri.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(File(photoUri))
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = colors.secondary.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Filled.Book, contentDescription = null, tint = colors.secondary)
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = memory.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.text
                )
                if (!memory.date.isNullOrBlank()) {
                    Text(
                        text = memory.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.secondary
                    )
                }
            }

            RetroButton(
                text = "VIEW",
                onClick = onViewMemories,
                style = RetroButtonStyle.Primary,
                minHeight = 36.dp
            )
        }
    }
}

private fun formatMs(ms: Int): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%d:%02d", min, sec)
}
