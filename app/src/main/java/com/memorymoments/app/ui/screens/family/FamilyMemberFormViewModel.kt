package com.memorymoments.app.ui.screens.family

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.memorymoments.app.model.FamilyMember
import com.memorymoments.app.model.RelationshipOptions
import com.memorymoments.app.repository.DistractorRepository
import com.memorymoments.app.repository.FamilyRepository
import com.memorymoments.app.repository.VisualProfileRepository
import com.memorymoments.app.utils.ImageStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class FamilyFormState(
    val id: String,
    val isEditing: Boolean,
    val name: String = "",
    val relationshipSelection: String = "",
    val customRelationship: String = "",
    val nickname: String = "",
    val memoryContext: String = "",
    val photoPath: String? = null,
    val nameError: String? = null,
    val relationshipError: String? = null,
    val photoError: String? = null,
    val isSaving: Boolean = false,
    val isLoadingPhoto: Boolean = false,
    val saveSucceeded: Boolean = false,
    val shouldClose: Boolean = false,
    val memberMissing: Boolean = false
) {
    val resolvedRelationship: String
        get() = if (relationshipSelection == RelationshipOptions.OTHER) {
            customRelationship.trim()
        } else {
            relationshipSelection.trim()
        }

    val isValid: Boolean
        get() = name.trim().isNotEmpty() &&
            resolvedRelationship.isNotEmpty() &&
            !photoPath.isNullOrBlank()

    val missingSummary: String?
        get() {
            if (isValid) return null
            val missing = buildList {
                if (name.trim().isEmpty()) add("name")
                if (resolvedRelationship.isEmpty()) add("relationship")
                if (photoPath.isNullOrBlank()) add("photo")
            }
            return "Add a ${missing.joinToString(" and ")} to save."
        }
}

class FamilyMemberFormViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val existingId: String? = savedStateHandle.get<String>(ID_KEY)
    private val repository = FamilyRepository(application)
    private val imageStorage = ImageStorage(application)
    private val distractorRepo = DistractorRepository(application)
    private val visualProfileRepo = VisualProfileRepository(application)
    private var originalPhotoPath: String? = null

    private val _state = MutableStateFlow(
        FamilyFormState(
            id = existingId ?: UUID.randomUUID().toString(),
            isEditing = existingId != null
        )
    )
    val state: StateFlow<FamilyFormState> = _state.asStateFlow()

    init {
        if (existingId != null) {
            viewModelScope.launch { loadExisting(existingId) }
        }
    }

    fun onNameChange(value: String) {
        _state.update { it.copy(name = value, nameError = null) }
    }

    fun onRelationshipSelected(value: String) {
        _state.update {
            it.copy(
                relationshipSelection = value,
                relationshipError = null,
                customRelationship = if (value == RelationshipOptions.OTHER) it.customRelationship else ""
            )
        }
    }

    fun onCustomRelationshipChange(value: String) {
        _state.update { it.copy(customRelationship = value, relationshipError = null) }
    }

    fun onNicknameChange(value: String) {
        _state.update { it.copy(nickname = value) }
    }

    fun onMemoryContextChange(value: String) {
        _state.update { it.copy(memoryContext = value) }
    }

    fun onPhotoPicked(uri: Uri) {
        val memberId = _state.value.id
        viewModelScope.launch {
            _state.update { it.copy(isLoadingPhoto = true, photoError = null) }
            val result = withContext(Dispatchers.IO) {
                imageStorage.copyFromPicker(uri, memberId)
            }
            _state.update { current ->
                result.fold(
                    onSuccess = { path ->
                        // Photo changed: invalidate Hard mode visual profile and distractors
                        if (current.isEditing && current.photoPath != null && current.photoPath != path) {
                            viewModelScope.launch(Dispatchers.IO) {
                                visualProfileRepo.invalidateForMember(memberId)
                                distractorRepo.invalidateHardForMember(memberId)
                            }
                        }
                        current.copy(photoPath = path, isLoadingPhoto = false, photoError = null)
                    },
                    onFailure = {
                        current.copy(
                            isLoadingPhoto = false,
                            photoError = "Couldn't save that photo. Please try another image."
                        )
                    }
                )
            }
        }
    }

    fun save() {
        val current = _state.value
        val nameError = if (current.name.trim().isEmpty()) "Please enter a name." else null
        val relationshipError = if (current.resolvedRelationship.isEmpty()) {
            "Please choose a relationship."
        } else {
            null
        }
        val photoError = if (current.photoPath.isNullOrBlank()) {
            "Please add a photo."
        } else {
            current.photoError
        }
        if (nameError != null || relationshipError != null || current.photoPath.isNullOrBlank()) {
            _state.update {
                it.copy(
                    nameError = nameError,
                    relationshipError = relationshipError,
                    photoError = photoError
                )
            }
            return
        }
        val member = FamilyMember(
            id = current.id,
            name = current.name.trim(),
            relationship = current.resolvedRelationship,
            nickname = current.nickname.trim(),
            memoryContext = current.memoryContext.trim(),
            originalPhotoUri = current.photoPath
        )
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val result = if (current.isEditing) repository.update(member) else repository.add(member)
            _state.update {
                it.copy(
                    isSaving = false,
                    saveSucceeded = result.isSuccess,
                    shouldClose = result.isSuccess
                )
            }
        }
    }

    fun deleteMember() {
        val id = _state.value.id
        viewModelScope.launch {
            repository.delete(id)
            // Clean up Hard mode data for this member
            withContext(Dispatchers.IO) {
                visualProfileRepo.invalidateForMember(id)
                distractorRepo.invalidateHardForMember(id)
            }
            _state.update { it.copy(shouldClose = true, isSaving = false) }
        }
    }

    private suspend fun loadExisting(id: String) {
        val member = repository.family.first().find { it.id == id }
        if (member == null) {
            _state.update { it.copy(memberMissing = true) }
            return
        }
        val selection = RelationshipOptions.menuSelection(member.relationship)
        _state.update {
            it.copy(
                id = member.id,
                isEditing = true,
                name = member.name,
                relationshipSelection = selection,
                customRelationship = if (selection == RelationshipOptions.OTHER) member.relationship else "",
                nickname = member.nickname,
                memoryContext = member.memoryContext,
                photoPath = member.originalPhotoUri
            )
        }
        originalPhotoPath = member.originalPhotoUri
    }

    companion object {
        const val ID_KEY = "id"
    }
}
