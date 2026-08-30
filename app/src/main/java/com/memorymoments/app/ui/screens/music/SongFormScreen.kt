package com.memorymoments.app.ui.screens.music

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.memorymoments.app.ui.components.ArcadeScreen
import com.memorymoments.app.ui.components.ArcadeTopBar
import com.memorymoments.app.ui.components.RetroButton
import com.memorymoments.app.ui.components.RetroButtonStyle
import com.memorymoments.app.ui.components.RetroCard
import com.memorymoments.app.ui.components.RetroConfirmDialog
import com.memorymoments.app.ui.components.RetroTextField
import com.memorymoments.app.ui.theme.MmTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongFormScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: SongFormViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val memories by viewModel.memories.collectAsStateWithLifecycle()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var memoryDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.shouldClose, state.songMissing) {
        if (state.shouldClose || state.songMissing) onSaved()
    }

    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) viewModel.onAudioPicked(uri)
    }

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
                title = if (state.isEditing) "Edit Song" else "Add Song",
                onBack = onBack
            )

            Text(
                text = if (state.isEditing) "EDIT SONG" else "NEW SONG",
                style = if (MmTheme.isEasyMode) MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold) else MmTheme.arcade.hud,
                color = colors.reward
            )

            Spacer(modifier = Modifier.height(dimens.md))

            // Audio File Selector Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { audioPicker.launch("audio/*") },
                shape = RoundedCornerShape(16.dp),
                color = colors.panel,
                border = BorderStroke(2.dp, if (state.audioError != null) colors.error else colors.secondary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimens.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (state.isLoadingAudio) {
                        CircularProgressIndicator(color = colors.reward)
                    } else {
                        Icon(
                            imageVector = if (state.localAudioUri != null) Icons.Filled.LibraryMusic else Icons.Filled.AudioFile,
                            contentDescription = null,
                            tint = if (state.localAudioUri != null) colors.success else colors.reward,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(dimens.sm))
                        Text(
                            text = if (state.localAudioUri != null) "AUDIO FILE READY" else "SELECT AUDIO FILE",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.text
                        )
                        Text(
                            text = if (state.localAudioUri != null) "Tap to choose a different audio file" else "Choose an audio file from your device",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textMuted
                        )
                    }
                }
            }

            if (state.audioError != null) {
                Spacer(modifier = Modifier.height(dimens.xs))
                Text(
                    text = state.audioError.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.error
                )
            }

            Spacer(modifier = Modifier.height(dimens.lg))

            // Form Inputs
            RetroCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(dimens.md)
                ) {
                    Text(
                        text = "SONG DETAILS",
                        style = MmTheme.arcade.label,
                        color = colors.secondary
                    )

                    RetroTextField(
                        value = state.title,
                        onValueChange = viewModel::onTitleChange,
                        label = "SONG TITLE",
                        required = true,
                        error = state.titleError,
                        imeAction = ImeAction.Next
                    )

                    RetroTextField(
                        value = state.artist,
                        onValueChange = viewModel::onArtistChange,
                        label = "ARTIST (OPTIONAL)",
                        imeAction = ImeAction.Done
                    )

                    if (memories.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(dimens.xs))
                        Text(
                            text = "CONNECT TO MEMORY (OPTIONAL)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.secondary
                        )

                        val selectedMemory = memories.find { it.id == state.selectedMemoryId }

                        ExposedDropdownMenuBox(
                            expanded = memoryDropdownExpanded,
                            onExpandedChange = { memoryDropdownExpanded = !memoryDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedMemory?.title ?: "None (No Memory Link)",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = memoryDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = colors.text,
                                    unfocusedTextColor = colors.text,
                                    focusedBorderColor = colors.secondary,
                                    unfocusedBorderColor = colors.border,
                                    focusedContainerColor = colors.panelInner,
                                    unfocusedContainerColor = colors.panelInner
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = memoryDropdownExpanded,
                                onDismissRequest = { memoryDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("None (No Memory Link)") },
                                    onClick = {
                                        viewModel.onMemorySelected(null)
                                        memoryDropdownExpanded = false
                                    }
                                )
                                memories.forEach { mem ->
                                    DropdownMenuItem(
                                        text = { Text(mem.title) },
                                        onClick = {
                                            viewModel.onMemorySelected(mem.id)
                                            memoryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimens.xl))

            // Action Buttons
            RetroButton(
                text = if (state.isSaving) "SAVING..." else "SAVE SONG",
                icon = Icons.Filled.Check,
                onClick = viewModel::save,
                style = RetroButtonStyle.Primary,
                enabled = !state.isSaving,
                minHeight = dimens.playButtonMin
            )

            if (state.isEditing) {
                Spacer(modifier = Modifier.height(dimens.md))
                RetroButton(
                    text = "DELETE SONG",
                    icon = Icons.Filled.Delete,
                    onClick = { showDeleteConfirm = true },
                    style = RetroButtonStyle.Ghost
                )
            }

            Spacer(modifier = Modifier.height(dimens.xxl))
        }
    }

    if (showDeleteConfirm) {
        RetroConfirmDialog(
            title = "DELETE SONG?",
            message = "This song will be removed from your music library.",
            confirmLabel = "DELETE",
            cancelLabel = "CANCEL",
            onConfirm = {
                showDeleteConfirm = false
                viewModel.deleteSong()
            },
            onCancel = {
                showDeleteConfirm = false
            }
        )
    }
}
