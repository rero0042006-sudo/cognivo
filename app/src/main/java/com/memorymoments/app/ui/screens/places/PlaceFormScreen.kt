package com.memorymoments.app.ui.screens.places

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.memorymoments.app.ui.components.ArcadeScreen
import com.memorymoments.app.ui.components.ArcadeTopBar
import com.memorymoments.app.ui.components.RetroButton
import com.memorymoments.app.ui.components.RetroButtonStyle
import com.memorymoments.app.ui.components.RetroCard
import com.memorymoments.app.ui.components.RetroConfirmDialog
import com.memorymoments.app.ui.components.RetroTextField
import com.memorymoments.app.ui.theme.MmTheme
import java.io.File

@Composable
fun PlaceFormScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: PlaceFormViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(state.shouldClose, state.placeMissing) {
        if (state.shouldClose || state.placeMissing) onSaved()
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.onPhotoPicked(uri)
    }

    val colors = MmTheme.colors
    val dimens = MmTheme.dimens
    val context = LocalContext.current

    ArcadeScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = dimens.screenHorizontal, vertical = dimens.screenVertical)
        ) {
            ArcadeTopBar(
                title = if (state.isEditing) "Edit Place" else "Add Place",
                onBack = onBack
            )

            Text(
                text = if (state.isEditing) "EDIT PLACE" else "NEW PLACE",
                style = if (MmTheme.isEasyMode) MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold) else MmTheme.arcade.hud,
                color = colors.reward
            )

            Spacer(modifier = Modifier.height(dimens.md))

            // Photo Selector Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clickable {
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                shape = RoundedCornerShape(16.dp),
                color = colors.panel,
                border = BorderStroke(2.dp, if (state.photoError != null) colors.error else colors.secondary)
            ) {
                val currentPhoto = state.photoUris.firstOrNull()
                if (state.isLoadingPhoto) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.reward)
                    }
                } else if (!currentPhoto.isNullOrBlank()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(File(currentPhoto))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Place photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(dimens.sm),
                            shape = RoundedCornerShape(8.dp),
                            color = colors.background.copy(alpha = 0.8f)
                        ) {
                            Text(
                                text = "TAP TO CHANGE PHOTO",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.text,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(dimens.md),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddPhotoAlternate,
                            contentDescription = "Add place photo",
                            tint = colors.reward,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(dimens.sm))
                        Text(
                            text = "CHOOSE PLACE PHOTO",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.text
                        )
                        Text(
                            text = "Tap to choose a real photo from your device",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textMuted
                        )
                    }
                }
            }

            if (state.photoError != null) {
                Spacer(modifier = Modifier.height(dimens.xs))
                Text(
                    text = state.photoError.orEmpty(),
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
                        text = "PLACE DETAILS",
                        style = MmTheme.arcade.label,
                        color = colors.secondary
                    )

                    RetroTextField(
                        value = state.name,
                        onValueChange = viewModel::onNameChange,
                        label = "PLACE NAME",
                        required = true,
                        error = state.nameError,
                        imeAction = ImeAction.Next
                    )

                    RetroTextField(
                        value = state.location,
                        onValueChange = viewModel::onLocationChange,
                        label = "LOCATION (OPTIONAL)",
                        imeAction = ImeAction.Next
                    )

                    RetroTextField(
                        value = state.datePeriod,
                        onValueChange = viewModel::onDatePeriodChange,
                        label = "YEAR / ERA (OPTIONAL)",
                        imeAction = ImeAction.Next
                    )

                    RetroTextField(
                        value = state.description,
                        onValueChange = viewModel::onDescriptionChange,
                        label = "MEMORIES / NOTES (OPTIONAL)",
                        singleLine = false,
                        minLines = 3,
                        imeAction = ImeAction.Done
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimens.xl))

            // Action Buttons
            RetroButton(
                text = if (state.isSaving) "SAVING..." else "SAVE PLACE",
                icon = Icons.Filled.Check,
                onClick = viewModel::save,
                style = RetroButtonStyle.Primary,
                enabled = !state.isSaving,
                minHeight = dimens.playButtonMin
            )

            if (state.isEditing) {
                Spacer(modifier = Modifier.height(dimens.md))
                RetroButton(
                    text = "DELETE PLACE",
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
            title = "DELETE PLACE?",
            message = "This place and its photos will be removed from your game.",
            confirmLabel = "DELETE",
            onConfirm = {
                showDeleteConfirm = false
                viewModel.deletePlace()
            },
            onCancel = {
                showDeleteConfirm = false
            }
        )
    }
}
