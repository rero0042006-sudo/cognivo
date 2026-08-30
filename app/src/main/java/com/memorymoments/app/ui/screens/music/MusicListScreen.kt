package com.memorymoments.app.ui.screens.music

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.memorymoments.app.model.Song
import com.memorymoments.app.ui.components.ArcadeScreen
import com.memorymoments.app.ui.components.ArcadeTopBar
import com.memorymoments.app.ui.components.RetroButton
import com.memorymoments.app.ui.components.RetroButtonStyle
import com.memorymoments.app.ui.components.RetroConfirmDialog
import com.memorymoments.app.ui.theme.MmTheme

@Composable
fun MusicListScreen(
    onBack: () -> Unit,
    onAddSong: () -> Unit,
    onEditSong: (String) -> Unit,
    onMemoryMusic: () -> Unit,
    viewModel: MusicListViewModel = viewModel()
) {
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentPath by viewModel.currentPath.collectAsStateWithLifecycle()
    val currentPosMs by viewModel.currentPositionMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.durationMs.collectAsStateWithLifecycle()

    var songToDelete by remember { mutableStateOf<Song?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopAudio()
        }
    }

    val colors = MmTheme.colors
    val dimens = MmTheme.dimens

    ArcadeScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimens.screenHorizontal, vertical = dimens.screenVertical)
        ) {
            ArcadeTopBar(title = "My Music", onBack = onBack)

            Text(
                text = "MY MUSIC",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = colors.reward
            )

            Spacer(modifier = Modifier.height(dimens.xs))

            Text(
                text = "Familiar songs, melodies, and musical memories:",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textMuted
            )

            Spacer(modifier = Modifier.height(dimens.md))

            // Action Buttons Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimens.sm)
            ) {
                RetroButton(
                    text = "+ ADD SONG",
                    icon = Icons.Filled.Add,
                    onClick = onAddSong,
                    style = RetroButtonStyle.Primary,
                    modifier = Modifier.weight(1f),
                    minHeight = 52.dp
                )

                RetroButton(
                    text = "MEMORY MUSIC",
                    icon = Icons.Filled.LibraryMusic,
                    onClick = onMemoryMusic,
                    style = RetroButtonStyle.Secondary,
                    modifier = Modifier.weight(1f),
                    minHeight = 52.dp
                )
            }

            Spacer(modifier = Modifier.height(dimens.lg))

            if (songs.isEmpty()) {
                EmptyMusicPanel(
                    onAddSong = onAddSong,
                    onLoadDemo = viewModel::loadDemoSongs
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(dimens.md)
                ) {
                    items(songs, key = { it.id }) { song ->
                        val isCurrentSong = currentPath == song.localAudioUri
                        SongCardItem(
                            song = song,
                            isPlaying = isCurrentSong && isPlaying,
                            isCurrentTrack = isCurrentSong,
                            currentPosMs = if (isCurrentSong) currentPosMs else 0,
                            durationMs = if (isCurrentSong) durationMs else 0,
                            onTogglePlay = { viewModel.togglePlaySong(song) },
                            onSeek = viewModel::seekTo,
                            onEdit = { onEditSong(song.id) },
                            onDelete = { songToDelete = song }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(dimens.xl))
                    }
                }
            }
        }
    }

    songToDelete?.let { song ->
        RetroConfirmDialog(
            title = "REMOVE SONG?",
            message = "This will remove \"${song.title}\" from Memory Moments.",
            confirmLabel = "REMOVE",
            cancelLabel = "CANCEL",
            onConfirm = {
                val id = song.id
                songToDelete = null
                viewModel.deleteSong(id)
            },
            onCancel = {
                songToDelete = null
            }
        )
    }
}

@Composable
private fun SongCardItem(
    song: Song,
    isPlaying: Boolean,
    isCurrentTrack: Boolean,
    currentPosMs: Int,
    durationMs: Int,
    onTogglePlay: () -> Unit,
    onSeek: (Int) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isCurrentTrack && isPlaying) colors.primary.copy(alpha = 0.08f) else colors.panel,
        border = BorderStroke(if (isCurrentTrack && isPlaying) 2.dp else 1.5.dp, if (isCurrentTrack && isPlaying) colors.primary else colors.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.md)
            ) {
                // Play / Pause Circle Button
                Surface(
                    modifier = Modifier
                        .size(54.dp)
                        .clickable(onClick = onTogglePlay),
                    shape = RoundedCornerShape(14.dp),
                    color = if (isPlaying) colors.primary else colors.secondary.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause ${song.title}" else "Play ${song.title}",
                            tint = if (isPlaying) colors.onPrimary else colors.secondary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = colors.text
                    )

                    if (!song.artist.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            ),
                            color = colors.secondary
                        )
                    }
                }

                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit song",
                        tint = colors.textMuted
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete song",
                        tint = colors.error.copy(alpha = 0.8f)
                    )
                }
            }

            // Inline Playback Progress Slider if active
            if (isCurrentTrack && durationMs > 0) {
                Spacer(modifier = Modifier.height(dimens.xs))
                Slider(
                    value = currentPosMs.toFloat(),
                    onValueChange = { onSeek(it.toInt()) },
                    valueRange = 0f..durationMs.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = colors.primary,
                        activeTrackColor = colors.primary,
                        inactiveTrackColor = colors.border
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun EmptyMusicPanel(
    onAddSong: () -> Unit,
    onLoadDemo: () -> Unit
) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.panel,
        border = BorderStroke(1.5.dp, colors.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimens.md)
        ) {
            Icon(
                imageVector = Icons.Filled.MusicNote,
                contentDescription = null,
                tint = colors.reward,
                modifier = Modifier.size(56.dp)
            )
            Text(
                text = "NO MUSIC YET",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = colors.reward,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Add a song they love.",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.text,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(dimens.sm))
            RetroButton(
                text = "+ ADD SONG FROM DEVICE",
                icon = Icons.Filled.Add,
                onClick = onAddSong,
                style = RetroButtonStyle.Primary,
                minHeight = dimens.playButtonMin
            )
            RetroButton(
                text = "LOAD DEMO SONGS",
                icon = Icons.Filled.AutoAwesome,
                onClick = onLoadDemo,
                style = RetroButtonStyle.Ghost,
                minHeight = 48.dp
            )
        }
    }
}
