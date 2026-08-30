package com.memorymoments.app.ui.screens.caregiver

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.memorymoments.app.model.CaregiverAlert
import com.memorymoments.app.model.CaregiverNote
import com.memorymoments.app.model.CognitiveDomainJourney
import com.memorymoments.app.model.DistractorStyle
import com.memorymoments.app.model.LinkedPatientDetails
import com.memorymoments.app.model.PatientReminder
import com.memorymoments.app.model.ReminderStatus
import com.memorymoments.app.model.ReminderType
import com.memorymoments.app.model.RoutineSlot
import com.memorymoments.app.model.WeeklySummaryStats
import com.memorymoments.app.model.WhatChangedItem
import com.memorymoments.app.repository.CaregiverRepository
import com.memorymoments.app.repository.DailyCompanionRepository
import com.memorymoments.app.repository.GameSettingsRepository
import com.memorymoments.app.repository.LifeEventRepository
import com.memorymoments.app.repository.MemoryRepository
import com.memorymoments.app.repository.PersonRepository
import com.memorymoments.app.repository.PlaceRepository
import com.memorymoments.app.repository.SongRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class CaregiverDashboardUiState(
    val caregiverCode: String = "CG-998811",
    val linkedPatient: LinkedPatientDetails = LinkedPatientDetails(),
    val reminders: List<PatientReminder> = emptyList(),
    val alerts: List<CaregiverAlert> = emptyList(),
    val cognitiveJourney: List<CognitiveDomainJourney> = emptyList(),
    val routineSlots: List<RoutineSlot> = emptyList(),
    val whatChanged: List<WhatChangedItem> = emptyList(),
    val weeklySummary: WeeklySummaryStats = WeeklySummaryStats(),
    val notes: List<CaregiverNote> = emptyList(),
    val peopleCount: Int = 0,
    val memoriesCount: Int = 0,
    val placesCount: Int = 0,
    val songsCount: Int = 0,
    val eventsCount: Int = 0,
    val distractorStyle: DistractorStyle = DistractorStyle.NORMAL,
    val showHeritageContent: Boolean = true,
    val dailyActivityEnabled: Boolean = true,
    val isLoading: Boolean = false
)

class CaregiverDashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val personRepo = PersonRepository(application)
    private val memoryRepo = MemoryRepository(application)
    private val placeRepo = PlaceRepository(application)
    private val songRepo = SongRepository(application)
    private val eventRepo = LifeEventRepository(application)
    private val settingsRepo = GameSettingsRepository(application)
    private val dailyRepo = DailyCompanionRepository(application)
    private val caregiverRepo = CaregiverRepository(application)

    private val contentCountsFlow = combine(
        personRepo.people,
        memoryRepo.memories,
        placeRepo.places,
        songRepo.songs,
        eventRepo.lifeEvents
    ) { people, memories, places, songs, events ->
        listOf(people.size, memories.size, places.size, songs.size, events.size)
    }

    private val baseCaregiverDataFlow = combine(
        caregiverRepo.caregiverCode,
        caregiverRepo.linkedPatient,
        caregiverRepo.reminders,
        caregiverRepo.alerts,
        caregiverRepo.cognitiveJourney
    ) { code, patient, reminders, alerts, journey ->
        CaregiverBaseData(code, patient, reminders, alerts, journey)
    }

    private val extraCaregiverDataFlow = combine(
        caregiverRepo.routineSlots,
        caregiverRepo.whatChangedItems,
        caregiverRepo.weeklySummary,
        caregiverRepo.notes
    ) { slots, whatChanged, summary, notes ->
        CaregiverExtraData(slots, whatChanged, summary, notes)
    }

    private val settingsFlow = combine(
        settingsRepo.distractorStyle,
        settingsRepo.showHeritageContent,
        dailyRepo.isCompanionEnabled
    ) { style, heritage, dailyOn ->
        CaregiverSettings(style, heritage, dailyOn)
    }

    val uiState: StateFlow<CaregiverDashboardUiState> = combine(
        baseCaregiverDataFlow,
        extraCaregiverDataFlow,
        contentCountsFlow,
        settingsFlow
    ) { base, extra, counts, settings ->
        CaregiverDashboardUiState(
            caregiverCode = base.code,
            linkedPatient = base.patient,
            reminders = base.reminders,
            alerts = base.alerts,
            cognitiveJourney = base.journey,
            routineSlots = extra.slots,
            whatChanged = extra.whatChanged,
            weeklySummary = extra.summary,
            notes = extra.notes,
            peopleCount = counts[0],
            memoriesCount = counts[1],
            placesCount = counts[2],
            songsCount = counts[3],
            eventsCount = counts[4],
            distractorStyle = settings.style,
            showHeritageContent = settings.heritage,
            dailyActivityEnabled = settings.dailyOn,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CaregiverDashboardUiState()
    )

    fun toggleReminderStatus(id: String, newStatus: ReminderStatus) {
        viewModelScope.launch {
            caregiverRepo.toggleReminderStatus(id, newStatus)
        }
    }

    fun addReminder(title: String, type: ReminderType, time: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            caregiverRepo.addReminder(
                PatientReminder(
                    id = UUID.randomUUID().toString(),
                    title = title.trim(),
                    type = type,
                    scheduledTime = time.trim(),
                    status = ReminderStatus.PENDING
                )
            )
        }
    }

    fun addNote(text: String) {
        viewModelScope.launch {
            caregiverRepo.addNote(text)
        }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            caregiverRepo.deleteNote(id)
        }
    }

    fun setDistractorStyle(style: DistractorStyle) {
        viewModelScope.launch {
            settingsRepo.setDistractorStyle(style)
        }
    }

    fun setShowHeritageContent(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepo.setShowHeritageContent(enabled)
        }
    }

    fun setDailyActivityEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dailyRepo.setCompanionEnabled(enabled)
        }
    }

    private data class CaregiverBaseData(
        val code: String,
        val patient: LinkedPatientDetails,
        val reminders: List<PatientReminder>,
        val alerts: List<CaregiverAlert>,
        val journey: List<CognitiveDomainJourney>
    )

    private data class CaregiverExtraData(
        val slots: List<RoutineSlot>,
        val whatChanged: List<WhatChangedItem>,
        val summary: WeeklySummaryStats,
        val notes: List<CaregiverNote>
    )

    private data class CaregiverSettings(
        val style: DistractorStyle,
        val heritage: Boolean,
        val dailyOn: Boolean
    )
}
