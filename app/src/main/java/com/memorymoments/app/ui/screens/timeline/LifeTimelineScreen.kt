package com.memorymoments.app.ui.screens.timeline

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.memorymoments.app.ui.components.ArcadeScreen
import com.memorymoments.app.ui.components.ArcadeTopBar
import com.memorymoments.app.ui.components.RetroButton
import com.memorymoments.app.ui.components.RetroButtonStyle
import com.memorymoments.app.ui.theme.MmTheme
import java.io.File

@Composable
fun LifeTimelineScreen(
    onBack: () -> Unit,
    onAddEvent: () -> Unit,
    onEditEvent: (String) -> Unit,
    onOpenPerson: (String) -> Unit,
    onOpenPlace: (String) -> Unit,
    onOpenMemory: (String) -> Unit,
    viewModel: LifeTimelineViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MmTheme.colors

    DisposableEffect(Unit) {
        onDispose {
            viewModel.audioPlaybackManager.stop()
        }
    }

    ArcadeScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            ArcadeTopBar(title = "My Life", onBack = onBack)

            Text(
                text = "My Life",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = colors.text
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Moments from your life.",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                color = colors.textMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RetroButton(
                    text = "+ ADD MOMENT",
                    icon = Icons.Filled.Add,
                    onClick = onAddEvent,
                    style = RetroButtonStyle.Primary,
                    modifier = Modifier.weight(1f),
                    minHeight = 48.dp
                )

                RetroButton(
                    text = if (state.ascending) "OLDEST FIRST" else "NEWEST FIRST",
                    icon = Icons.Filled.Sort,
                    onClick = viewModel::toggleSortOrder,
                    style = RetroButtonStyle.Secondary,
                    modifier = Modifier.weight(1f),
                    minHeight = 48.dp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.primary)
                }
            } else if (state.items.isEmpty()) {
                EmptyTimelinePanel(
                    onAdd = onAddEvent,
                    onLoadDemo = viewModel::loadDemoEvents
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    itemsIndexed(state.items, key = { _, item -> item.event.id }) { index, item ->
                        TimelineNodeItem(
                            item = item,
                            isFirst = index == 0,
                            isLast = index == state.items.lastIndex,
                            isPlaying = state.isPlayingSongId == item.event.songId,
                            onPlaySong = { viewModel.togglePlaySong(item.event.songId, item.songAudioUri) },
                            onClick = { onEditEvent(item.event.id) },
                            onOpenPerson = onOpenPerson,
                            onOpenPlace = onOpenPlace,
                            onOpenMemory = onOpenMemory
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineNodeItem(
    item: LifeEventItemUi,
    isFirst: Boolean,
    isLast: Boolean,
    isPlaying: Boolean,
    onPlaySong: () -> Unit,
    onClick: () -> Unit,
    onOpenPerson: (String) -> Unit,
    onOpenPlace: (String) -> Unit,
    onOpenMemory: (String) -> Unit
) {
    val colors = MmTheme.colors
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Vertical Timeline Column (Left)
        Column(
            modifier = Modifier
                .width(44.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top connecting line
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(20.dp)
                    .background(if (isFirst) Color.Transparent else colors.primary.copy(alpha = 0.4f))
            )

            // Timeline Node Circle
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = colors.primaryContainer,
                border = BorderStroke(2.dp, colors.primary)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = null,
                        tint = colors.onPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Bottom connecting line
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .weight(1f)
                    .background(if (isLast) Color.Transparent else colors.primary.copy(alpha = 0.4f))
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Event Card (Right)
        Surface(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 20.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(20.dp),
            color = colors.panel,
            border = BorderStroke(1.dp, colors.border),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                val photoUri = item.event.photoUri
                if (!photoUri.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(File(photoUri))
                            .crossfade(true)
                            .build(),
                        contentDescription = item.event.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // Header Row: Year Badge & Category
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!item.yearDisplay.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = colors.primaryContainer
                            ) {
                                Text(
                                    text = item.yearDisplay,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = colors.panelInner
                            ) {
                                Text(
                                    text = "UNDATED",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textMuted,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if (!item.event.category.isNullOrBlank()) {
                            Text(
                                text = item.event.category,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.textMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = item.event.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = colors.text
                    )

                    if (!item.event.description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = item.event.description,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                            color = colors.textMuted
                        )
                    }

                    // Linked Relations Chips
                    if (item.placeName != null || item.personNames.isNotEmpty() || item.songTitle != null || item.memoryTitle != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Place Link
                            if (item.placeName != null && item.event.placeId != null) {
                                Surface(
                                    modifier = Modifier.clickable { onOpenPlace(item.event.placeId) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = colors.panelInner
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(imageVector = Icons.Filled.Place, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                                        Text(text = item.placeName, style = MaterialTheme.typography.labelMedium, color = colors.text)
                                    }
                                }
                            }

                            // People Links
                            item.event.personIds.forEachIndexed { i, pid ->
                                val name = item.personNames.getOrNull(i) ?: "Family"
                                Surface(
                                    modifier = Modifier.clickable { onOpenPerson(pid) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = colors.panelInner
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(imageVector = Icons.Filled.Person, contentDescription = null, tint = colors.secondary, modifier = Modifier.size(16.dp))
                                        Text(text = name, style = MaterialTheme.typography.labelMedium, color = colors.text)
                                    }
                                }
                            }

                            // Song Link with Play / Stop
                            if (item.songTitle != null && item.event.songId != null) {
                                Surface(
                                    modifier = Modifier.clickable { onPlaySong() },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isPlaying) colors.secondaryContainer else colors.panelInner
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                            contentDescription = null,
                                            tint = colors.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(text = "🎵 ${item.songTitle}", style = MaterialTheme.typography.labelMedium, color = colors.text)
                                    }
                                }
                            }

                            // Memory Link
                            if (item.memoryTitle != null && item.event.memoryId != null) {
                                Surface(
                                    modifier = Modifier.clickable { onOpenMemory(item.event.memoryId) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = colors.panelInner
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(imageVector = Icons.Filled.Book, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                                        Text(text = "📖 ${item.memoryTitle}", style = MaterialTheme.typography.labelMedium, color = colors.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyTimelinePanel(
    onAdd: () -> Unit,
    onLoadDemo: () -> Unit
) {
    val colors = MmTheme.colors

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.panel,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CalendarToday,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(56.dp)
            )
            Text(
                text = "MY LIFE",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = colors.text,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Add important moments and milestones from your life.",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textMuted,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            RetroButton(
                text = "+ ADD MOMENT",
                icon = Icons.Filled.Add,
                onClick = onAdd,
                style = RetroButtonStyle.Primary,
                minHeight = 50.dp
            )
            RetroButton(
                text = "LOAD DEMO TIMELINE",
                icon = Icons.Filled.AutoAwesome,
                onClick = onLoadDemo,
                style = RetroButtonStyle.Secondary,
                minHeight = 50.dp
            )
        }
    }
}
