package com.memorymoments.app.ui.screens.family

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.memorymoments.app.model.FamilyMember
import com.memorymoments.app.repository.FamilyRepository
import com.memorymoments.app.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FamilyGalleryState(
    val members: List<FamilyMember> = emptyList(),
    val isLoading: Boolean = true
) {
    val canAdd: Boolean get() = members.size < Constants.MAX_FAMILY_MEMBERS
    val isFamilyComplete: Boolean get() = members.size >= Constants.MAX_FAMILY_MEMBERS
    val isReadyToPlay: Boolean get() = members.size >= Constants.MIN_FAMILY_FOR_GAME
    val membersNeeded: Int get() = (Constants.MIN_FAMILY_FOR_GAME - members.size).coerceAtLeast(0)
}

class FamilyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FamilyRepository(application)

    val gallery: StateFlow<FamilyGalleryState> = repository.family
        .map { members -> FamilyGalleryState(members = members, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FamilyGalleryState()
        )

    private val _pendingDelete = MutableStateFlow<FamilyMember?>(null)
    val pendingDelete: StateFlow<FamilyMember?> = _pendingDelete.asStateFlow()

    fun requestDelete(member: FamilyMember) {
        _pendingDelete.value = member
    }

    fun cancelDelete() {
        _pendingDelete.value = null
    }

    fun confirmDelete() {
        val member = _pendingDelete.value ?: return
        viewModelScope.launch {
            repository.delete(member.id)
            _pendingDelete.value = null
        }
    }
}
