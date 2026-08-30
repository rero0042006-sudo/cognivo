package com.memorymoments.app.ui.screens.caregiver

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.memorymoments.app.model.AiConfidence
import com.memorymoments.app.model.AlertSeverity
import com.memorymoments.app.model.CaregiverAlert
import com.memorymoments.app.model.CaregiverNote
import com.memorymoments.app.model.CognitiveDomainJourney
import com.memorymoments.app.model.DistractorStyle
import com.memorymoments.app.model.PatientReminder
import com.memorymoments.app.model.ReminderStatus
import com.memorymoments.app.model.ReminderType
import com.memorymoments.app.model.RoutineSlot
import com.memorymoments.app.model.WeeklySummaryStats
import com.memorymoments.app.model.WhatChangedItem
import com.memorymoments.app.ui.components.ArcadeScreen
import com.memorymoments.app.ui.components.ArcadeTopBar
import com.memorymoments.app.ui.components.RetroButton
import com.memorymoments.app.ui.components.RetroButtonStyle
import com.memorymoments.app.ui.components.RetroPanel
import com.memorymoments.app.ui.components.RetroTextField
import com.memorymoments.app.ui.theme.MmTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CaregiverDashboardScreen(
    onBack: () -> Unit,
    onPeople: () -> Unit,
    onMemories: () -> Unit,
    onPlaces: () -> Unit,
    onMusic: () -> Unit,
    onTimeline: () -> Unit,
    onPreview: () -> Unit,
    onSettings: () -> Unit,
    onOpenChat: () -> Unit = {},
    viewModel: CaregiverDashboardViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens
    val context = LocalContext.current

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("TODAY", "ACTIVITIES", "JOURNEY", "ROUTINE", "NOTES", "FOUNDATION")

    var selectedAlertForWhy by remember { mutableStateOf<CaregiverAlert?>(null) }
    var selectedJourneyForWhy by remember { mutableStateOf<CognitiveDomainJourney?>(null) }
    var showAddReminderDialog by remember { mutableStateOf(false) }

    ArcadeScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            ArcadeTopBar(title = "CAREGIVER HUB", onBack = onBack)

            // Caregiver Code Banner & Quick Copy
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                color = colors.panelInner,
                border = BorderStroke(1.dp, colors.reward)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = colors.reward,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Caregiver Code (Share with patient):",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = colors.textMuted
                            )
                            Text(
                                text = state.caregiverCode,
                                style = MmTheme.arcade.titleSmall.copy(fontSize = 16.sp),
                                color = colors.reward
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Caregiver Code", state.caregiverCode)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Caregiver code copied!", Toast.LENGTH_SHORT).show()
                            },
                        color = colors.panel,
                        border = BorderStroke(1.dp, colors.reward)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "Copy code",
                                tint = colors.reward,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "COPY",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.reward
                            )
                        }
                    }
                }
            }

            // Tab Navigation Row
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = colors.primary,
                edgePadding = 0.dp,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = colors.reward,
                        height = 3.dp
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                style = if (selectedTabIndex == index) {
                                    MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                } else {
                                    MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp)
                                },
                                color = if (selectedTabIndex == index) colors.reward else colors.textMuted
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable Content depending on selected Tab
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> TodayOverviewTab(
                        state = state,
                        onOpenChat = onOpenChat,
                        onExplainAlert = { selectedAlertForWhy = it },
                        onToggleReminder = viewModel::toggleReminderStatus
                    )
                    1 -> ActivitiesAndRemindersTab(
                        state = state,
                        onToggleReminder = viewModel::toggleReminderStatus,
                        onAddReminderClick = { showAddReminderDialog = true }
                    )
                    2 -> CognitiveJourneyTab(
                        state = state,
                        onExplainJourney = { selectedJourneyForWhy = it },
                        onDifficultyChange = viewModel::setDistractorStyle
                    )
                    3 -> RoutineAndBehaviorTab(state = state)
                    4 -> CommunicationAndNotesTab(
                        state = state,
                        onOpenChat = onOpenChat,
                        onAddNote = viewModel::addNote,
                        onDeleteNote = viewModel::deleteNote
                    )
                    5 -> MemoryFoundationTab(
                        state = state,
                        onPeople = onPeople,
                        onMemories = onMemories,
                        onPlaces = onPlaces,
                        onMusic = onMusic,
                        onTimeline = onTimeline,
                        onPreview = onPreview,
                        onSettings = onSettings
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // "Why am I seeing this?" Dialog for Alerts
    selectedAlertForWhy?.let { alert ->
        AlertDialog(
            onDismissRequest = { selectedAlertForWhy = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Filled.Lightbulb, contentDescription = null, tint = colors.reward)
                    Text(text = "Why Am I Seeing This?", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.primary
                    )
                    Text(
                        text = alert.reason,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.text
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colors.panelInner,
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Text(
                            text = "AI Confidence: ${alert.confidence.name} • Non-medical supportive observation",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textMuted,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedAlertForWhy = null }) {
                    Text("GOT IT", fontWeight = FontWeight.Bold, color = colors.primary)
                }
            },
            containerColor = colors.panel
        )
    }

    // "Why am I seeing this?" Dialog for Cognitive Journey
    selectedJourneyForWhy?.let { journey ->
        AlertDialog(
            onDismissRequest = { selectedJourneyForWhy = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Filled.Psychology, contentDescription = null, tint = colors.secondary)
                    Text(text = "${journey.domain} Insight", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Baseline: ${journey.baselineComparison}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.reward
                    )
                    Text(
                        text = journey.explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.text
                    )
                    Text(
                        text = "Calculated using longitudinal response patterns, not single-session evaluation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedJourneyForWhy = null }) {
                    Text("CLOSE", fontWeight = FontWeight.Bold, color = colors.primary)
                }
            },
            containerColor = colors.panel
        )
    }

    // Add Reminder Dialog
    if (showAddReminderDialog) {
        AddReminderDialog(
            onDismiss = { showAddReminderDialog = false },
            onAdd = { title, type, time ->
                viewModel.addReminder(title, type, time)
                showAddReminderDialog = false
            }
        )
    }
}

// -------------------------------------------------------------------------
// TAB 0: TODAY OVERVIEW
// -------------------------------------------------------------------------

@Composable
private fun TodayOverviewTab(
    state: CaregiverDashboardUiState,
    onOpenChat: () -> Unit,
    onExplainAlert: (CaregiverAlert) -> Unit,
    onToggleReminder: (String, ReminderStatus) -> Unit
) {
    val colors = MmTheme.colors

    // Patient status card answering "How is my patient doing today?"
    RetroPanel(borderColor = colors.reward.copy(alpha = 0.85f)) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PATIENT TODAY",
                        style = MmTheme.arcade.label,
                        color = colors.secondary
                    )
                    Text(
                        text = "${state.linkedPatient.name}, Age ${state.linkedPatient.age}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = colors.text
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.panelInner,
                    border = BorderStroke(1.dp, colors.reward)
                ) {
                    Text(
                        text = "Active Today 🌟",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.reward,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = "“Doing well today! Morning memory recognition completed with high engagement.”",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                color = colors.text
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = colors.border)

            // Quick Message & Call Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RetroButton(
                    text = "💬 CHAT WITH PATIENT",
                    icon = Icons.AutoMirrored.Filled.Chat,
                    onClick = onOpenChat,
                    style = RetroButtonStyle.Primary,
                    modifier = Modifier.weight(1f),
                    minHeight = 44.dp
                )
            }
        }
    }

    // "What Changed Since Yesterday?"
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.panel,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = colors.primary)
                Text(
                    text = "WHAT CHANGED SINCE YESTERDAY?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.primary
                )
            }

            state.whatChanged.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = colors.text
                    )
                }
            }
        }
    }

    // Intelligent Alerts Section
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "INTELLIGENT INSIGHTS & ALERTS",
            style = MmTheme.arcade.label,
            color = colors.secondary
        )

        state.alerts.forEach { alert ->
            AlertCard(alert = alert, onWhyClick = { onExplainAlert(alert) })
        }
    }
}

// -------------------------------------------------------------------------
// TAB 1: ACTIVITIES & REMINDERS
// -------------------------------------------------------------------------

@Composable
private fun ActivitiesAndRemindersTab(
    state: CaregiverDashboardUiState,
    onToggleReminder: (String, ReminderStatus) -> Unit,
    onAddReminderClick: () -> Unit
) {
    val colors = MmTheme.colors

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "DAILY REMINDERS & ACTIVITIES",
            style = MmTheme.arcade.label,
            color = colors.secondary
        )

        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onAddReminderClick),
            color = colors.panelInner,
            border = BorderStroke(1.dp, colors.primary)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                Text(text = "ADD REMINDER", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = colors.primary)
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        state.reminders.forEach { reminder ->
            ReminderCard(reminder = reminder, onToggleStatus = { newStatus -> onToggleReminder(reminder.id, newStatus) })
        }
    }
}

// -------------------------------------------------------------------------
// TAB 2: COGNITIVE JOURNEY
// -------------------------------------------------------------------------

@Composable
private fun CognitiveJourneyTab(
    state: CaregiverDashboardUiState,
    onExplainJourney: (CognitiveDomainJourney) -> Unit,
    onDifficultyChange: (DistractorStyle) -> Unit
) {
    val colors = MmTheme.colors

    Text(
        text = "PERSONAL BASELINE & COGNITIVE JOURNEY",
        style = MmTheme.arcade.label,
        color = colors.secondary
    )

    Text(
        text = "Evaluated against patient's own history over time, not compared to others.",
        style = MaterialTheme.typography.bodySmall,
        color = colors.textMuted
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        state.cognitiveJourney.forEach { journey ->
            CognitiveDomainCard(journey = journey, onWhyClick = { onExplainJourney(journey) })
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Adaptive Activity Support / Recommended Difficulty
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.panel,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "ADAPTIVE DIFFICULTY SUPPORT",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.primary
            )
            Text(
                text = "Cogniva recommends 'NORMAL' level based on steady recognition accuracy.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.text
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(DistractorStyle.EASY, DistractorStyle.NORMAL, DistractorStyle.CHALLENGE).forEach { style ->
                    val isSelected = state.distractorStyle == style
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onDifficultyChange(style) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) colors.primary else colors.panelInner,
                        border = BorderStroke(1.dp, if (isSelected) colors.primary else colors.border)
                    ) {
                        Text(
                            text = style.name,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) colors.onPrimary else colors.textMuted,
                            modifier = Modifier.padding(vertical = 10.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// TAB 3: ROUTINE & BEHAVIOR
// -------------------------------------------------------------------------

@Composable
private fun RoutineAndBehaviorTab(state: CaregiverDashboardUiState) {
    val colors = MmTheme.colors

    Text(
        text = "ROUTINE FINGERPRINT & BEST ENGAGEMENT TIME",
        style = MmTheme.arcade.label,
        color = colors.secondary
    )

    // Best Time Recommendation Banner
    RetroPanel(borderColor = colors.reward) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.Filled.Star, contentDescription = null, tint = colors.reward)
                Text(
                    text = "Suggested Best Activity Time: 11:00 AM",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.reward
                )
            }
            Text(
                text = "Patient demonstrates highest responsiveness and focus between 10:30 AM and 12:00 PM.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.text
            )
        }
    }

    // Routine slots list
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        state.routineSlots.forEach { slot ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = if (slot.isRecommendedBestTime) colors.panelInner else colors.panel,
                border = BorderStroke(if (slot.isRecommendedBestTime) 2.dp else 1.dp, if (slot.isRecommendedBestTime) colors.reward else colors.border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = slot.time,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (slot.isRecommendedBestTime) colors.reward else colors.primary
                        )
                        Text(
                            text = slot.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.text
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colors.panel,
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Text(
                            text = "Engagement: ${slot.engagementLevel}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.textMuted,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// TAB 4: COMMUNICATION & NOTES
// -------------------------------------------------------------------------

@Composable
private fun CommunicationAndNotesTab(
    state: CaregiverDashboardUiState,
    onOpenChat: () -> Unit,
    onAddNote: (String) -> Unit,
    onDeleteNote: (String) -> Unit
) {
    val colors = MmTheme.colors
    var newNoteText by remember { mutableStateOf("") }
    val dateFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }

    Text(
        text = "COMMUNICATION & PRIVATE CAREGIVER NOTES",
        style = MmTheme.arcade.label,
        color = colors.secondary
    )

    RetroButton(
        text = "OPEN 2-WAY CHAT WITH PATIENT",
        icon = Icons.AutoMirrored.Filled.Chat,
        onClick = onOpenChat,
        style = RetroButtonStyle.Primary,
        minHeight = 50.dp
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Private Caregiver Notes Section
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.panel,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "PRIVATE CAREGIVER NOTES",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.primary
            )
            Text(
                text = "Jot down observations, appointments, or mood notes. Visible only to you.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RetroTextField(
                    value = newNoteText,
                    onValueChange = { newNoteText = it },
                    label = "Write a quick note...",
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    modifier = Modifier
                        .size(52.dp)
                        .clickable {
                            if (newNoteText.isNotBlank()) {
                                onAddNote(newNoteText)
                                newNoteText = ""
                            }
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = colors.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Note", tint = colors.onPrimary)
                    }
                }
            }

            HorizontalDivider(color = colors.border, modifier = Modifier.padding(vertical = 4.dp))

            if (state.notes.isEmpty()) {
                Text(text = "No private notes yet.", style = MaterialTheme.typography.bodyMedium, color = colors.textMuted)
            } else {
                state.notes.forEach { note ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = colors.panelInner,
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = note.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.text
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = dateFormat.format(Date(note.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textMuted
                                )
                            }
                            IconButton(onClick = { onDeleteNote(note.id) }, modifier = Modifier.size(28.dp)) {
                                Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete", tint = colors.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// TAB 5: MEMORY FOUNDATION HUBS
// -------------------------------------------------------------------------

@Composable
private fun MemoryFoundationTab(
    state: CaregiverDashboardUiState,
    onPeople: () -> Unit,
    onMemories: () -> Unit,
    onPlaces: () -> Unit,
    onMusic: () -> Unit,
    onTimeline: () -> Unit,
    onPreview: () -> Unit,
    onSettings: () -> Unit
) {
    val colors = MmTheme.colors

    Text(
        text = "MEMORY FOUNDATION HUBS",
        style = MmTheme.arcade.label,
        color = colors.secondary
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        CaregiverHubCard(title = "People & Family", countLabel = "${state.peopleCount} people", subtitle = "Faces and relationships", icon = Icons.Filled.Groups, onClick = onPeople)
        CaregiverHubCard(title = "Memories & Stories", countLabel = "${state.memoriesCount} memories", subtitle = "Photos and reminiscence", icon = Icons.AutoMirrored.Filled.EventNote, onClick = onMemories)
        CaregiverHubCard(title = "Places & Hometowns", countLabel = "${state.placesCount} places", subtitle = "Special locations", icon = Icons.Filled.Landscape, onClick = onPlaces)
        CaregiverHubCard(title = "Favorite Music", countLabel = "${state.songsCount} songs", subtitle = "Melodies and audio tracks", icon = Icons.Filled.MusicNote, onClick = onMusic)
        CaregiverHubCard(title = "Life Timeline", countLabel = "${state.eventsCount} events", subtitle = "Chronological milestones", icon = Icons.Filled.Timeline, onClick = onTimeline)
    }

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = "CONTROLS & PREVIEW",
        style = MmTheme.arcade.label,
        color = colors.secondary
    )

    RetroButton(
        text = "PREVIEW ELDER EXPERIENCE",
        icon = Icons.Filled.Visibility,
        onClick = onPreview,
        style = RetroButtonStyle.Primary,
        minHeight = 52.dp
    )

    RetroButton(
        text = "APP SETTINGS",
        icon = Icons.Filled.Settings,
        onClick = onSettings,
        style = RetroButtonStyle.Secondary,
        minHeight = 50.dp
    )
}

// -------------------------------------------------------------------------
// REUSABLE CARDS & DIALOGS
// -------------------------------------------------------------------------

@Composable
private fun AlertCard(
    alert: CaregiverAlert,
    onWhyClick: () -> Unit
) {
    val colors = MmTheme.colors
    val borderColor = when (alert.severity) {
        AlertSeverity.POSITIVE -> colors.reward
        AlertSeverity.INFO -> colors.secondary
        AlertSeverity.WARNING -> colors.error
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = colors.panel,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                    color = borderColor
                )
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onWhyClick),
                    color = colors.panelInner,
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(12.dp))
                        Text(text = "Why?", style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
                    }
                }
            }

            Text(
                text = alert.description,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.text
            )
        }
    }
}

@Composable
private fun ReminderCard(
    reminder: PatientReminder,
    onToggleStatus: (ReminderStatus) -> Unit
) {
    val colors = MmTheme.colors
    val isCompleted = reminder.status == ReminderStatus.COMPLETED

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isCompleted) colors.panelInner.copy(alpha = 0.6f) else colors.panel,
        border = BorderStroke(1.dp, if (isCompleted) colors.border else colors.primary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(text = reminder.type.icon, fontSize = 22.sp)
                Column {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = colors.text
                    )
                    Text(
                        text = "${reminder.scheduledTime} • ${reminder.type.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        val nextStatus = if (isCompleted) ReminderStatus.PENDING else ReminderStatus.COMPLETED
                        onToggleStatus(nextStatus)
                    },
                color = if (isCompleted) colors.reward.copy(alpha = 0.2f) else colors.panelInner,
                border = BorderStroke(1.dp, if (isCompleted) colors.reward else colors.border)
            ) {
                Text(
                    text = if (isCompleted) "✓ DONE" else "PENDING",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isCompleted) colors.reward else colors.textMuted,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun CognitiveDomainCard(
    journey: CognitiveDomainJourney,
    onWhyClick: () -> Unit
) {
    val colors = MmTheme.colors

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = colors.panel,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = journey.domain,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.text
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colors.panelInner,
                    border = BorderStroke(1.dp, colors.reward)
                ) {
                    Text(
                        text = journey.baselineComparison,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.reward,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = journey.explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMuted
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Confidence: ${journey.confidence.name}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted
                )

                Text(
                    text = "Why am I seeing this? →",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.primary,
                    modifier = Modifier.clickable(onClick = onWhyClick)
                )
            }
        }
    }
}

@Composable
private fun CaregiverHubCard(
    title: String,
    countLabel: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val colors = MmTheme.colors

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = colors.panel,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = colors.panelInner
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(22.dp))
                    }
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp), color = colors.text)
                        Surface(shape = RoundedCornerShape(6.dp), color = colors.panelInner) {
                            Text(text = countLabel, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = colors.secondary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                }
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = colors.textMuted)
        }
    }
}

@Composable
private fun AddReminderDialog(
    onDismiss: () -> Unit,
    onAdd: (String, ReminderType, String) -> Unit
) {
    val colors = MmTheme.colors
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ReminderType.MEDICATION) }
    var time by remember { mutableStateOf("12:00 PM") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Add Patient Reminder", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                RetroTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Reminder Title (e.g. Vitamin D)"
                )
                RetroTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = "Scheduled Time (e.g. 2:00 PM)"
                )
                Text(text = "Category:", style = MaterialTheme.typography.labelMedium, color = colors.textMuted)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(ReminderType.MEDICATION, ReminderType.HYDRATION, ReminderType.ACTIVITY).forEach { type ->
                        val isSelected = selectedType == type
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedType = type },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) colors.primary else colors.panelInner,
                            border = BorderStroke(1.dp, if (isSelected) colors.primary else colors.border)
                        ) {
                            Text(
                                text = type.label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) colors.onPrimary else colors.textMuted,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onAdd(title, selectedType, time) },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) {
                Text("ADD")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = colors.textMuted)
            }
        },
        containerColor = colors.panel
    )
}
