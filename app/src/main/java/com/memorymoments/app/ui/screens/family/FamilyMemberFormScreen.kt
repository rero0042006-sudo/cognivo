package com.memorymoments.app.ui.screens.family

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.memorymoments.app.model.RelationshipOptions
import com.memorymoments.app.ui.components.ArcadeScreen
import com.memorymoments.app.ui.components.ArcadeTopBar
import com.memorymoments.app.ui.components.MemberPortrait
import com.memorymoments.app.ui.components.RetroButton
import com.memorymoments.app.ui.components.RetroButtonStyle
import com.memorymoments.app.ui.components.RetroCard
import com.memorymoments.app.ui.components.RetroConfirmDialog
import com.memorymoments.app.ui.components.RetroTextField
import com.memorymoments.app.ui.theme.MmTheme

@Composable
fun FamilyMemberFormScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: FamilyMemberFormViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(state.shouldClose, state.memberMissing) {
        if (state.shouldClose || state.memberMissing) onSaved()
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.onPhotoPicked(uri)
    }

    fun launchPicker() {
        photoPicker.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    FamilyMemberFormContent(
        state = state,
        onBack = onBack,
        onNameChange = viewModel::onNameChange,
        onRelationshipSelected = viewModel::onRelationshipSelected,
        onCustomRelationshipChange = viewModel::onCustomRelationshipChange,
        onNicknameChange = viewModel::onNicknameChange,
        onMemoryContextChange = viewModel::onMemoryContextChange,
        onPickPhoto = ::launchPicker,
        onSave = viewModel::save,
        onRequestDelete = { showDeleteConfirm = true }
    )

    if (showDeleteConfirm) {
        RetroConfirmDialog(
            title = "REMOVE ${state.name.ifBlank { "THIS PERSON" }.uppercase()}?",
            message = "This will remove ${state.name.ifBlank { "this person" }} from your family game.",
            confirmLabel = "REMOVE",
            onConfirm = {
                showDeleteConfirm = false
                viewModel.deleteMember()
            },
            onCancel = { showDeleteConfirm = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyMemberFormContent(
    state: FamilyFormState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onRelationshipSelected: (String) -> Unit,
    onCustomRelationshipChange: (String) -> Unit,
    onNicknameChange: (String) -> Unit,
    onMemoryContextChange: (String) -> Unit,
    onPickPhoto: () -> Unit,
    onSave: () -> Unit,
    onRequestDelete: () -> Unit
) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens
    val title = if (state.isEditing) "Edit Family Member" else "Add Family Member"
    var relationshipExpanded by remember { mutableStateOf(false) }

    ArcadeScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = dimens.screenHorizontal, vertical = dimens.screenVertical)
        ) {
            ArcadeTopBar(title = title, onBack = onBack)
            Text(
                text = if (state.isEditing) "EDIT FAMILY MEMBER" else "ADD FAMILY MEMBER",
                style = MmTheme.arcade.hud,
                color = colors.reward
            )
            Spacer(modifier = Modifier.height(dimens.lg))

            PhotoPickerArea(
                photoPath = state.photoPath,
                isLoading = state.isLoadingPhoto,
                error = state.photoError,
                onPickPhoto = onPickPhoto
            )

            Spacer(modifier = Modifier.height(dimens.lg))
            RetroTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = "Name",
                required = true,
                error = state.nameError
            )
            Spacer(modifier = Modifier.height(dimens.md))

            ExposedDropdownMenuBox(
                expanded = relationshipExpanded,
                onExpandedChange = { relationshipExpanded = it }
            ) {
                OutlinedTextField(
                    value = state.relationshipSelection,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    label = { Text("Relationship *") },
                    isError = state.relationshipError != null,
                    supportingText = state.relationshipError?.let {
                        { Text(it, color = colors.error) }
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = relationshipExpanded) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.text),
                    colors = fieldColors()
                )
                ExposedDropdownMenu(
                    expanded = relationshipExpanded,
                    onDismissRequest = { relationshipExpanded = false }
                ) {
                    RelationshipOptions.all.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(option, style = MaterialTheme.typography.bodyLarge)
                            },
                            onClick = {
                                onRelationshipSelected(option)
                                relationshipExpanded = false
                            },
                            modifier = Modifier.defaultMinSize(minHeight = dimens.touch)
                        )
                    }
                }
            }

            if (state.relationshipSelection == RelationshipOptions.OTHER) {
                Spacer(modifier = Modifier.height(dimens.md))
                RetroTextField(
                    value = state.customRelationship,
                    onValueChange = onCustomRelationshipChange,
                    label = "Custom relationship",
                    required = true,
                    error = state.relationshipError
                )
            }

            Spacer(modifier = Modifier.height(dimens.md))
            RetroTextField(
                value = state.nickname,
                onValueChange = onNicknameChange,
                label = "Nickname"
            )
            Spacer(modifier = Modifier.height(dimens.md))
            RetroTextField(
                value = state.memoryContext,
                onValueChange = onMemoryContextChange,
                label = "Memory / context",
                singleLine = false,
                minLines = 3,
                imeAction = ImeAction.Done
            )

            Spacer(modifier = Modifier.height(dimens.lg))
            if (!state.isValid) {
                Text(
                    text = state.missingSummary.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.error
                )
                Spacer(modifier = Modifier.height(dimens.md))
            }

            RetroButton(
                text = if (state.isEditing) "SAVE CHANGES" else "SAVE FAMILY MEMBER",
                icon = Icons.Filled.Check,
                onClick = onSave,
                style = RetroButtonStyle.Primary,
                enabled = !state.isSaving,
                minHeight = dimens.playButtonMin
            )
            if (state.isEditing) {
                Spacer(modifier = Modifier.height(dimens.md))
                RetroButton(
                    text = "REMOVE",
                    onClick = onRequestDelete,
                    style = RetroButtonStyle.Ghost
                )
            }
            Spacer(modifier = Modifier.height(dimens.xl))
        }
    }
}

@Composable
private fun PhotoPickerArea(
    photoPath: String?,
    isLoading: Boolean,
    error: String?,
    onPickPhoto: () -> Unit
) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens
    if (photoPath.isNullOrBlank()) {
        RetroCard(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 220.dp)
                .clickable(role = Role.Button, onClick = onPickPhoto),
            borderColor = if (error != null) colors.error else colors.secondary.copy(alpha = 0.7f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimens.xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = colors.secondary)
                } else {
                    Icon(
                        imageVector = Icons.Filled.PhotoCamera,
                        contentDescription = "Add photo",
                        tint = colors.reward,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(dimens.md))
                    Text(
                        text = "ADD PHOTO",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.text,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    } else {
        MemberPortrait(
            imageUri = photoPath,
            contentDescription = "Selected family photo",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .defaultMinSize(minHeight = 220.dp)
        )
        Spacer(modifier = Modifier.height(dimens.md))
        RetroButton(
            text = "CHANGE PHOTO",
            onClick = onPickPhoto,
            style = RetroButtonStyle.Secondary
        )
    }
    if (error != null) {
        Spacer(modifier = Modifier.height(dimens.sm))
        Text(text = error, style = MaterialTheme.typography.bodyMedium, color = colors.error)
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MmTheme.colors.text,
    unfocusedTextColor = MmTheme.colors.text,
    cursorColor = MmTheme.colors.reward,
    focusedBorderColor = MmTheme.colors.secondary,
    unfocusedBorderColor = MmTheme.colors.border,
    errorBorderColor = MmTheme.colors.error,
    errorLabelColor = MmTheme.colors.error,
    focusedLabelColor = MmTheme.colors.secondary,
    unfocusedLabelColor = MmTheme.colors.textMuted,
    focusedContainerColor = MmTheme.colors.panelInner,
    unfocusedContainerColor = MmTheme.colors.panelInner,
    errorContainerColor = MmTheme.colors.panelInner
)
