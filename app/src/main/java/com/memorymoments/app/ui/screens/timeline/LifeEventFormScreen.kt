package com.memorymoments.app.ui.screens.timeline

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.memorymoments.app.ui.components.RetroConfirmDialog
import com.memorymoments.app.ui.theme.MmTheme
import java.io.File

private val EVENT_CATEGORIES = listOf(
    "Birth", "Childhood", "School", "Work", "Marriage", "Family", "Travel", "Home", "Achievement", "Other"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LifeEventFormScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: LifeEventFormViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MmTheme.colors
    val context = LocalContext.current

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onSaved()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onPhotoPicked(uri)
        }
    }

    ArcadeScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            ArcadeTopBar(
                title = if (state.isEditing) "Edit Moment" else "Add Moment",
                onBack = onBack
            )

            Text(
                text = if (state.isEditing) "Edit Life Moment" else "Add Life Moment",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = colors.text
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Photo Selection Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                shape = RoundedCornerShape(20.dp),
                color = colors.panel,
                border = BorderStroke(1.dp, colors.border),
                shadowElevation = 1.dp
            ) {
                if (state.photoUri != null) {
                    Column {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(File(state.photoUri!!))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Event photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            RetroButton(
                                text = "REMOVE PHOTO",
                                icon = Icons.Filled.Delete,
                                onClick = viewModel::removePhoto,
                                style = RetroButtonStyle.Ghost,
                                minHeight = 40.dp
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddPhotoAlternate,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "ADD PHOTO (OPTIONAL)",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = colors.primary
                        )
                        Text(
                            text = "Tap to choose a picture for this memory",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Event Details Fields
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
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Title
                    OutlinedTextField(
                        value = state.title,
                        onValueChange = viewModel::onTitleChange,
                        label = { Text("Event Title *") },
                        placeholder = { Text("e.g. Wedding Day, Born in Chennai") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border
                        )
                    )

                    // Date / Year
                    OutlinedTextField(
                        value = state.date,
                        onValueChange = viewModel::onDateChange,
                        label = { Text("Year / Date (Optional)") },
                        placeholder = { Text("e.g. 1978, June 1978, 1978-06-15") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border
                        )
                    )

                    // Category Selection Chips using FlowRow
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "CATEGORY",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textMuted
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            EVENT_CATEGORIES.forEach { cat ->
                                FilterChip(
                                    selected = state.category == cat,
                                    onClick = { viewModel.onCategoryChange(cat) },
                                    label = { Text(cat) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colors.primaryContainer,
                                        selectedLabelColor = colors.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }

                    // Description
                    OutlinedTextField(
                        value = state.description,
                        onValueChange = viewModel::onDescriptionChange,
                        label = { Text("Description (Optional)") },
                        placeholder = { Text("Add special stories or details about this moment...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Associated Connections (People, Place, Song, Memory)
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
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "CONNECTED MEMORIES & PEOPLE",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.text
                    )

                    // Associated People
                    if (state.availablePeople.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "People in this moment:", style = MaterialTheme.typography.labelMedium, color = colors.textMuted)
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.availablePeople.forEach { person ->
                                    val isSelected = state.selectedPersonIds.contains(person.id)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.togglePerson(person.id) },
                                        label = { Text("👩 ${person.name}") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = colors.secondaryContainer,
                                            selectedLabelColor = colors.onSecondaryContainer
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Associated Place
                    if (state.availablePlaces.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "Place:", style = MaterialTheme.typography.labelMedium, color = colors.textMuted)
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.availablePlaces.forEach { place ->
                                    val isSelected = state.selectedPlaceId == place.id
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.setPlace(if (isSelected) null else place.id) },
                                        label = { Text("📍 ${place.name}") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = colors.tertiaryContainer,
                                            selectedLabelColor = colors.text
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Associated Song
                    if (state.availableSongs.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "Song / Tune:", style = MaterialTheme.typography.labelMedium, color = colors.textMuted)
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.availableSongs.forEach { song ->
                                    val isSelected = state.selectedSongId == song.id
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.setSong(if (isSelected) null else song.id) },
                                        label = { Text("🎵 ${song.title}") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = colors.secondaryContainer,
                                            selectedLabelColor = colors.onSecondaryContainer
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Associated Memory
                    if (state.availableMemories.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "Connected Life Memory:", style = MaterialTheme.typography.labelMedium, color = colors.textMuted)
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.availableMemories.forEach { memory ->
                                    val isSelected = state.selectedMemoryId == memory.id
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.setMemory(if (isSelected) null else memory.id) },
                                        label = { Text("📖 ${memory.title}") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = colors.primaryContainer,
                                            selectedLabelColor = colors.onPrimaryContainer
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = state.errorMessage!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.error
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Save & Delete Actions
            RetroButton(
                text = "SAVE EVENT",
                icon = Icons.Filled.Save,
                onClick = viewModel::save,
                style = RetroButtonStyle.Primary,
                minHeight = 52.dp
            )

            if (state.isEditing) {
                Spacer(modifier = Modifier.height(12.dp))
                RetroButton(
                    text = "REMOVE THIS EVENT",
                    icon = Icons.Filled.DeleteForever,
                    onClick = viewModel::requestDelete,
                    style = RetroButtonStyle.Ghost,
                    minHeight = 48.dp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (state.showDeleteConfirm) {
        RetroConfirmDialog(
            title = "REMOVE THIS EVENT?",
            message = "Are you sure you want to remove this milestone from your life timeline? Associated people, places, and songs will remain safe in your app.",
            confirmLabel = "REMOVE",
            cancelLabel = "CANCEL",
            onConfirm = viewModel::confirmDelete,
            onCancel = viewModel::cancelDelete
        )
    }
}
