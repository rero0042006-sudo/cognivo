package com.memorymoments.app.ui.screens.memories

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhotoCamera
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
import androidx.compose.ui.semantics.Role
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
import com.memorymoments.app.ui.components.RetroTextField
import com.memorymoments.app.ui.theme.MmTheme
import java.io.File

private val HERITAGE_OPTIONS = listOf(
    "Family Traditions",
    "Festivals & Celebrations",
    "Childhood Places",
    "Traditional Food",
    "Music & Songs",
    "Traditional Clothing",
    "Community Gatherings",
    "Important Journeys"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MemoryFormScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: MemoryFormViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = MmTheme.colors
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(state.shouldClose) {
        if (state.shouldClose) {
            onSaved()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onPhotoPicked(it) }
    }

    ArcadeScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            ArcadeTopBar(
                title = if (state.isEditMode) "Edit Memory" else "Add Memory",
                onBack = onBack
            )

            Text(
                text = if (state.isEditMode) "Edit Memory" else "Create a Memory",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                ),
                color = colors.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Connect photo, loved ones, place, song, and life details into one meaningful memory.",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                color = colors.textMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!state.errorMessage.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.errorMessage!!,
                        color = colors.error,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(14.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // STEP 1: PHOTO
            FormSectionCard(title = "STEP 1 • PHOTO") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val photo = state.photoUri
                    if (!photo.isNullOrBlank()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(role = Role.Button, onClick = { photoPickerLauncher.launch("image/*") }),
                            color = colors.panelInner,
                            border = BorderStroke(1.dp, colors.border)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(File(photo))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Memory Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(role = Role.Button, onClick = { photoPickerLauncher.launch("image/*") }),
                            color = colors.panelInner,
                            border = BorderStroke(1.dp, colors.border)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    modifier = Modifier.size(48.dp),
                                    shape = CircleShape,
                                    color = colors.primaryContainer
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.PhotoCamera,
                                            contentDescription = null,
                                            tint = colors.onPrimaryContainer,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "SELECT PHOTO",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.primary
                                )
                            }
                        }
                    }

                    RetroButton(
                        text = if (state.photoUri != null) "CHANGE PHOTO" else "CHOOSE PHOTO",
                        icon = Icons.Filled.PhotoCamera,
                        onClick = { photoPickerLauncher.launch("image/*") },
                        style = RetroButtonStyle.Ghost,
                        minHeight = 44.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // STEP 2 & 3: TITLE & STORY
            FormSectionCard(title = "STEP 2 & 3 • TITLE & STORY") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    RetroTextField(
                        value = state.title,
                        onValueChange = viewModel::onTitleChange,
                        label = "Memory Title (Required)",
                        required = true,
                        error = if (state.title.isBlank()) state.errorMessage else null
                    )

                    RetroTextField(
                        value = state.description,
                        onValueChange = viewModel::onDescriptionChange,
                        label = "Story or Description (Optional)",
                        singleLine = false,
                        minLines = 3
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // STEP 4: PEOPLE
            FormSectionCard(title = "STEP 4 • WHO WAS THERE?") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (state.availablePeople.isEmpty()) {
                        Text(
                            text = "No family members added yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textMuted
                        )
                    } else {
                        Text(
                            text = "Select family members in this memory:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textMuted
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (person in state.availablePeople) {
                                val isSelected = state.selectedPersonIds.contains(person.id)
                                SelectableChip(
                                    text = person.name,
                                    icon = Icons.Filled.Groups,
                                    isSelected = isSelected,
                                    onClick = { viewModel.togglePersonSelection(person.id) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // STEP 5: PLACE
            FormSectionCard(title = "STEP 5 • WHERE WAS IT?") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (state.availablePlaces.isEmpty()) {
                        Text(
                            text = "No saved places yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textMuted
                        )
                    } else {
                        Text(
                            text = "Select a saved place (or leave blank):",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textMuted
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (place in state.availablePlaces) {
                                val isSelected = state.selectedPlaceId == place.id
                                SelectableChip(
                                    text = place.name,
                                    icon = Icons.Filled.Landscape,
                                    isSelected = isSelected,
                                    onClick = { viewModel.onPlaceSelected(place.id) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // STEP 6: SONG
            FormSectionCard(title = "STEP 6 • ASSOCIATED SONG") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (state.availableSongs.isEmpty()) {
                        Text(
                            text = "No saved songs yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textMuted
                        )
                    } else {
                        Text(
                            text = "Select a special song (optional):",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textMuted
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (song in state.availableSongs) {
                                val isSelected = state.selectedSongId == song.id
                                SelectableChip(
                                    text = song.title,
                                    icon = Icons.Filled.MusicNote,
                                    isSelected = isSelected,
                                    onClick = { viewModel.onSongSelected(song.id) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // STEP 7: YEAR / ERA & HERITAGE
            FormSectionCard(title = "STEP 7 • YEAR & HERITAGE (OPTIONAL)") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    RetroTextField(
                        value = state.dateOrYear,
                        onValueChange = viewModel::onDateOrYearChange,
                        label = "Year or Era (e.g. 1985 or 1970s)"
                    )

                    Text(
                        text = "Cultural / Heritage Category:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (tag in HERITAGE_OPTIONS) {
                            val isSelected = state.heritageCategory == tag
                            SelectableChip(
                                text = tag,
                                icon = Icons.Filled.AutoAwesome,
                                isSelected = isSelected,
                                onClick = { viewModel.onHeritageCategoryChange(if (isSelected) null else tag) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SAVE / DELETE ACTIONS
            RetroButton(
                text = if (state.isSaving) "SAVING..." else if (state.isEditMode) "UPDATE MEMORY" else "SAVE MEMORY",
                icon = Icons.Default.Check,
                onClick = viewModel::save,
                style = RetroButtonStyle.Primary,
                enabled = !state.isSaving,
                minHeight = 56.dp
            )

            if (state.isEditMode) {
                Spacer(modifier = Modifier.height(12.dp))
                RetroButton(
                    text = "DELETE MEMORY",
                    icon = Icons.Filled.DeleteForever,
                    onClick = { showDeleteConfirm = true },
                    style = RetroButtonStyle.Ghost,
                    minHeight = 52.dp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showDeleteConfirm) {
        RetroConfirmDialog(
            title = "DELETE THIS MEMORY?",
            message = "This will remove \"${state.title.ifBlank { "this memory" }}\". Related family members, places, and songs will NOT be deleted.",
            confirmLabel = "DELETE",
            onConfirm = {
                showDeleteConfirm = false
                viewModel.delete()
            },
            onCancel = { showDeleteConfirm = false }
        )
    }
}

@Composable
private fun FormSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    val colors = MmTheme.colors

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = colors.panel,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = colors.primary
            )
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun SelectableChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = MmTheme.colors
    val bgColor = if (isSelected) colors.primary else colors.panelInner
    val fgColor = if (isSelected) colors.onPrimary else colors.text
    val borderColor = if (isSelected) colors.primary else colors.border

    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = fgColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = fgColor
            )
        }
    }
}
