package com.memorymoments.app.ui.screens.memories

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.memorymoments.app.ui.components.RetroConfirmDialog
import com.memorymoments.app.ui.theme.MmTheme
import java.io.File

@Composable
fun MemoriesScreen(
    onBack: () -> Unit,
    onAddMemory: () -> Unit,
    onEditMemory: (String) -> Unit,
    onMemoryTalk: (String?) -> Unit,
    onOpenTimeline: () -> Unit,
    viewModel: MemoriesViewModel = viewModel()
) {
    val items by viewModel.memoryItems.collectAsStateWithLifecycle()
    val colors = MmTheme.colors
    var deletingMemoryId by remember { mutableStateOf<String?>(null) }
    var deletingMemoryTitle by remember { mutableStateOf<String?>(null) }

    ArcadeScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            ArcadeTopBar(title = "My Memories", onBack = onBack)

            Text(
                text = "My Memories",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = colors.text
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Special life stories, milestones, and moments:",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
                color = colors.textMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action row: Add Memory, Life Timeline, Memory Talk
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RetroButton(
                    text = "ADD MEMORY",
                    icon = Icons.Filled.Add,
                    onClick = onAddMemory,
                    style = RetroButtonStyle.Primary,
                    modifier = Modifier.weight(1.2f),
                    minHeight = 48.dp
                )

                RetroButton(
                    text = "MY LIFE",
                    icon = Icons.Filled.Timeline,
                    onClick = onOpenTimeline,
                    style = RetroButtonStyle.Secondary,
                    modifier = Modifier.weight(1f),
                    minHeight = 48.dp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (items.isEmpty()) {
                EmptyMemoriesPanel(
                    onAdd = onAddMemory,
                    onLoadDemo = viewModel::loadDemoMemories
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(items, key = { it.memory.id }) { item ->
                        MemoryCardItem(
                            item = item,
                            onTalk = { onMemoryTalk(item.memory.id) },
                            onEdit = { onEditMemory(item.memory.id) },
                            onDelete = {
                                deletingMemoryId = item.memory.id
                                deletingMemoryTitle = item.memory.title
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    deletingMemoryId?.let { id ->
        RetroConfirmDialog(
            title = "DELETE THIS MEMORY?",
            message = "This will remove \"${deletingMemoryTitle ?: "this memory"}\". Stored family members, places, and songs will NOT be removed.",
            confirmLabel = "DELETE",
            onConfirm = {
                viewModel.deleteMemory(id)
                deletingMemoryId = null
                deletingMemoryTitle = null
            },
            onCancel = {
                deletingMemoryId = null
                deletingMemoryTitle = null
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MemoryCardItem(
    item: MemoryDisplayItem,
    onTalk: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val memory = item.memory
    val colors = MmTheme.colors
    val context = LocalContext.current
    val photoUri = memory.photoUris.firstOrNull()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.panel,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (!photoUri.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(File(photoUri))
                            .crossfade(true)
                            .build(),
                        contentDescription = "Photo for ${memory.title}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = memory.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = colors.text
                )

                if (!memory.date.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarToday,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = memory.date,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.primary
                        )
                    }
                }

                if (!memory.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = memory.description,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                        color = colors.textMuted
                    )
                }

                // Entity Chips: People, Place, Song, Heritage
                val hasChips = item.peopleNames.isNotEmpty() || item.placeName != null || item.songTitle != null || memory.heritageCategory != null
                if (hasChips) {
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item.peopleNames.forEach { pName ->
                            EntityTagChip(icon = Icons.Filled.Groups, text = pName, containerColor = colors.primaryContainer, contentColor = colors.onPrimaryContainer)
                        }
                        item.placeName?.let { plName ->
                            EntityTagChip(icon = Icons.Filled.Landscape, text = plName, containerColor = colors.secondaryContainer, contentColor = colors.onSecondaryContainer)
                        }
                        item.songTitle?.let { sTitle ->
                            EntityTagChip(icon = Icons.Filled.MusicNote, text = sTitle, containerColor = colors.panelInner, contentColor = colors.primary)
                        }
                        memory.heritageCategory?.let { hCat ->
                            EntityTagChip(icon = Icons.Filled.AutoAwesome, text = hCat, containerColor = colors.secondary, contentColor = colors.onSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: TALK, EDIT, DELETE
                RetroButton(
                    text = "🎤 TALK ABOUT THIS",
                    icon = Icons.Filled.Mic,
                    onClick = onTalk,
                    style = RetroButtonStyle.Primary,
                    minHeight = 48.dp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RetroButton(
                        text = "EDIT",
                        icon = Icons.Filled.Edit,
                        onClick = onEdit,
                        style = RetroButtonStyle.Ghost,
                        modifier = Modifier.weight(1f),
                        minHeight = 44.dp
                    )
                    RetroButton(
                        text = "DELETE",
                        icon = Icons.Filled.Delete,
                        onClick = onDelete,
                        style = RetroButtonStyle.Ghost,
                        modifier = Modifier.weight(1f),
                        minHeight = 44.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun EntityTagChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = contentColor
            )
        }
    }
}

@Composable
private fun EmptyMemoriesPanel(
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Book,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(56.dp)
            )
            Text(
                text = "START WITH A SPECIAL MEMORY",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = colors.text,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Add photos, stories, family members, places, and songs to preserve meaningful life moments.",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textMuted,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            RetroButton(
                text = "ADD MEMORY",
                icon = Icons.Filled.Add,
                onClick = onAdd,
                style = RetroButtonStyle.Primary,
                minHeight = 52.dp
            )
            RetroButton(
                text = "LOAD DEMO MEMORIES",
                icon = Icons.Filled.AutoAwesome,
                onClick = onLoadDemo,
                style = RetroButtonStyle.Ghost,
                minHeight = 48.dp
            )
        }
    }
}
