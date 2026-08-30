package com.memorymoments.app.ui.screens.portal

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.memorymoments.app.model.CognitiveAbilityProgress
import com.memorymoments.app.model.PatientActivityRecord
import com.memorymoments.app.model.PatientAssessmentRecord
import com.memorymoments.app.model.PatientProfile
import com.memorymoments.app.model.PatientReminder
import com.memorymoments.app.model.ReminderStatus
import com.memorymoments.app.model.ReminderType
import com.memorymoments.app.model.UserRole
import com.memorymoments.app.repository.AuthRepository
import com.memorymoments.app.repository.CaregiverRepository
import com.memorymoments.app.repository.PatientProgressRepository
import com.memorymoments.app.ui.components.ArcadeScreen
import com.memorymoments.app.ui.components.ArcadeTopBar
import com.memorymoments.app.ui.components.RetroButton
import com.memorymoments.app.ui.components.RetroButtonStyle
import com.memorymoments.app.ui.components.RetroPanel
import com.memorymoments.app.ui.components.RetroTextField
import com.memorymoments.app.ui.screens.home.HomeScreen
import com.memorymoments.app.ui.theme.MmTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PatientPortalScreen(
    onPlay: () -> Unit,
    onFamily: () -> Unit,
    onPlaces: () -> Unit,
    onMemories: () -> Unit,
    onMusic: () -> Unit,
    onSettings: () -> Unit,
    onCaregiver: () -> Unit = {},
    onTryDemo: () -> Unit,
    onNavigateRoute: (String) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val colors = MmTheme.colors
    val authRepo = remember { AuthRepository(context) }
    val progressRepo = remember { PatientProgressRepository(context) }
    val caregiverRepo = remember { CaregiverRepository(context) }

    val currentUser by authRepo.currentUser.collectAsStateWithLifecycle(initialValue = null)
    val reminders by caregiverRepo.reminders.collectAsStateWithLifecycle(initialValue = emptyList())
    val cognitiveProgress by progressRepo.cognitiveProgress.collectAsStateWithLifecycle(initialValue = emptyList())
    val completedActivities by progressRepo.completedActivities.collectAsStateWithLifecycle(initialValue = emptyList())
    val completedAssessments by progressRepo.completedAssessments.collectAsStateWithLifecycle(initialValue = emptyList())

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showAddReminderDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        caregiverRepo.fetchRemoteReminders()
        progressRepo.fetchRemoteProgress()
    }

    val patientName = currentUser?.patientProfile?.fullName?.ifBlank {
        currentUser?.identifier?.substringBefore("@")?.replace(".", " ")?.split(" ")
            ?.joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
    } ?: ""

    Scaffold(
        bottomBar = {
            PatientPortalBottomBar(
                selectedTab = selectedTab,
                onSelectTab = { selectedTab = it },
                onGames = onPlay,
                onMemories = onMemories,
                onFamily = onFamily
            )
        },
        containerColor = colors.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> {
                    // Home Section
                    HomeScreen(
                        isPreview = false,
                        userName = patientName,
                        onPlay = onPlay,
                        onFamily = onFamily,
                        onPlaces = onPlaces,
                        onMemories = onMemories,
                        onMusic = onMusic,
                        onSettings = onSettings,
                        onCaregiver = onCaregiver,
                        onTryDemo = onTryDemo,
                        onNavigateRoute = onNavigateRoute
                    )
                }
                1 -> {
                    // Progress Section
                    PatientProgressTab(
                        cognitiveProgress = cognitiveProgress,
                        activities = completedActivities,
                        assessments = completedAssessments
                    )
                }
                2 -> {
                    // Reminders Section
                    PatientRemindersTab(
                        reminders = reminders,
                        onCompleteReminder = { id ->
                            coroutineScope.launch {
                                caregiverRepo.toggleReminderStatus(id, ReminderStatus.COMPLETED)
                            }
                        },
                        onAddReminderClick = { showAddReminderDialog = true }
                    )
                }
                3 -> {
                    // Profile Section
                    val activeProfile = currentUser?.patientProfile ?: PatientProfile(
                        fullName = currentUser?.identifier?.substringBefore("@")?.replace(".", " ") ?: "Patient",
                        contactInfo = currentUser?.identifier ?: ""
                    )
                    PatientProfileTab(
                        profile = activeProfile,
                        userIdentifier = currentUser?.identifier ?: "",
                        onOpenChat = onCaregiver,
                        onLogout = onLogout,
                        onUpdateProfile = { updated ->
                            coroutineScope.launch {
                                authRepo.updatePatientProfile(updated)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showAddReminderDialog) {
        AddPatientReminderDialog(
            onDismiss = { showAddReminderDialog = false },
            onAdd = { title, date, time, repeat, type ->
                coroutineScope.launch {
                    caregiverRepo.addReminder(
                        PatientReminder(
                            id = java.util.UUID.randomUUID().toString(),
                            title = title,
                            type = type,
                            scheduledTime = time,
                            date = date,
                            repeatOption = repeat,
                            status = ReminderStatus.PENDING
                        )
                    )
                }
                showAddReminderDialog = false
            }
        )
    }
}

// -------------------------------------------------------------------------
// UNIFIED SINGLE BOTTOM NAVIGATION BAR (7 Main Destinations)
// -------------------------------------------------------------------------

@Composable
private fun PatientPortalBottomBar(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    onGames: () -> Unit,
    onMemories: () -> Unit,
    onFamily: () -> Unit
) {
    val colors = MmTheme.colors

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.panel,
        tonalElevation = 8.dp,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Home
            BottomNavItem(
                title = androidx.compose.ui.res.stringResource(com.memorymoments.app.R.string.nav_home),
                icon = Icons.Filled.Home,
                isSelected = selectedTab == 0,
                onClick = { onSelectTab(0) }
            )
            // 2. Games
            BottomNavItem(
                title = androidx.compose.ui.res.stringResource(com.memorymoments.app.R.string.nav_games),
                icon = Icons.Filled.SportsEsports,
                isSelected = false,
                onClick = onGames
            )
            // 3. Memories
            BottomNavItem(
                title = androidx.compose.ui.res.stringResource(com.memorymoments.app.R.string.nav_memories),
                icon = Icons.Filled.PhotoLibrary,
                isSelected = false,
                onClick = onMemories
            )
            // 4. Family
            BottomNavItem(
                title = androidx.compose.ui.res.stringResource(com.memorymoments.app.R.string.nav_family),
                icon = Icons.Filled.Groups,
                isSelected = false,
                onClick = onFamily
            )
            // 5. Progress
            BottomNavItem(
                title = androidx.compose.ui.res.stringResource(com.memorymoments.app.R.string.nav_progress),
                icon = Icons.Filled.AutoGraph,
                isSelected = selectedTab == 1,
                onClick = { onSelectTab(1) }
            )
            // 6. Reminders
            BottomNavItem(
                title = androidx.compose.ui.res.stringResource(com.memorymoments.app.R.string.nav_reminders),
                icon = Icons.Filled.Alarm,
                isSelected = selectedTab == 2,
                onClick = { onSelectTab(2) }
            )
            // 7. Profile
            BottomNavItem(
                title = androidx.compose.ui.res.stringResource(com.memorymoments.app.R.string.nav_profile),
                icon = Icons.Filled.Person,
                isSelected = selectedTab == 3,
                onClick = { onSelectTab(3) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = MmTheme.colors

    Column(
        modifier = Modifier
            .defaultMinSize(minWidth = 46.dp, minHeight = 50.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 3.dp, vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) colors.reward else Color.Transparent,
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) colors.panel else colors.textMuted,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 3.dp)
                    .size(20.dp)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp
            ),
            color = if (isSelected) colors.primary else colors.textMuted,
            maxLines = 1
        )
    }
}

// -------------------------------------------------------------------------
// TAB 1: PROGRESS SECTION
// -------------------------------------------------------------------------

@Composable
private fun PatientProgressTab(
    cognitiveProgress: List<CognitiveAbilityProgress>,
    activities: List<PatientActivityRecord>,
    assessments: List<PatientAssessmentRecord>
) {
    val colors = MmTheme.colors

    ArcadeScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "YOUR PROGRESS",
                style = MmTheme.arcade.titleSmall,
                color = colors.reward,
                modifier = Modifier.semantics { heading() }
            )

            Text(
                text = "Track your cognitive journey, completed activities, and assessments over time.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMuted
            )

            // Cognitive Performance & Improvement Percentages Cards
            Text(
                text = "COGNITIVE ABILITY PERFORMANCE",
                style = MmTheme.arcade.label,
                color = colors.secondary
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                cognitiveProgress.forEach { ability ->
                    CognitiveAbilityCard(ability = ability)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Completed Activities Section
            Text(
                text = "COMPLETED ACTIVITIES",
                style = MmTheme.arcade.label,
                color = colors.secondary
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.panel,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (activities.isEmpty()) {
                        Text(text = "No completed activities yet.", style = MaterialTheme.typography.bodyMedium, color = colors.textMuted)
                    } else {
                        activities.forEach { act ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null, tint = colors.reward, modifier = Modifier.size(20.dp))
                                    Column {
                                        Text(text = act.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colors.text)
                                        Text(text = "${act.dateCompleted} • ${act.category}", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                                    }
                                }
                                Surface(shape = RoundedCornerShape(6.dp), color = colors.panelInner) {
                                    Text(text = act.score, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = colors.primary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Completed Assessments Section
            Text(
                text = "COMPLETED ASSESSMENTS",
                style = MmTheme.arcade.label,
                color = colors.secondary
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.panel,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (assessments.isEmpty()) {
                        Text(text = "No assessments recorded yet.", style = MaterialTheme.typography.bodyMedium, color = colors.textMuted)
                    } else {
                        assessments.forEach { assess ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = assess.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colors.text)
                                    Text(text = "${assess.scorePercent}% Result", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = colors.reward)
                                }
                                Text(text = assess.resultSummary, style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                                Text(text = "Completed: ${assess.dateCompleted}", style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CognitiveAbilityCard(ability: CognitiveAbilityProgress) {
    val colors = MmTheme.colors

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = colors.panel,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ability.domain,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                    color = colors.text
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colors.panelInner,
                    border = BorderStroke(1.dp, colors.reward)
                ) {
                    Text(
                        text = ability.statusLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.reward,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${ability.currentPercent}% Score",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = colors.primary
                )
                Text(
                    text = "Baseline: ${ability.baselinePercent}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )
            }

            LinearProgressIndicator(
                progress = { ability.currentPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = colors.reward,
                trackColor = colors.panelInner,
            )
        }
    }
}

// -------------------------------------------------------------------------
// TAB 2: REMINDERS SECTION
// -------------------------------------------------------------------------

@Composable
private fun PatientRemindersTab(
    reminders: List<PatientReminder>,
    onCompleteReminder: (String) -> Unit,
    onAddReminderClick: () -> Unit
) {
    val colors = MmTheme.colors
    val pendingReminders = remember(reminders) { reminders.filter { it.status != ReminderStatus.COMPLETED } }
    val completedReminders = remember(reminders) { reminders.filter { it.status == ReminderStatus.COMPLETED } }

    ArcadeScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "REMINDERS",
                    style = MmTheme.arcade.titleSmall,
                    color = colors.reward,
                    modifier = Modifier.semantics { heading() }
                )

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onAddReminderClick),
                    color = colors.panelInner,
                    border = BorderStroke(1.dp, colors.reward)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null, tint = colors.reward, modifier = Modifier.size(16.dp))
                        Text(text = androidx.compose.ui.res.stringResource(com.memorymoments.app.R.string.reminders_add_button), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = colors.reward)
                    }
                }
            }

            Text(
                text = androidx.compose.ui.res.stringResource(com.memorymoments.app.R.string.reminders_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMuted
            )

            // Upcoming / Due Reminders
            Text(
                text = androidx.compose.ui.res.stringResource(com.memorymoments.app.R.string.reminders_upcoming_section),
                style = MmTheme.arcade.label,
                color = colors.secondary
            )

            if (pendingReminders.isEmpty()) {
                RetroPanel(borderColor = colors.reward) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null, tint = colors.reward, modifier = Modifier.size(28.dp))
                        Text(text = androidx.compose.ui.res.stringResource(com.memorymoments.app.R.string.reminders_all_done), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colors.text)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    pendingReminders.forEach { rem ->
                        PatientReminderCard(reminder = rem, onComplete = { onCompleteReminder(rem.id) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Completed History
            Text(
                text = androidx.compose.ui.res.stringResource(com.memorymoments.app.R.string.reminders_completed_section),
                style = MmTheme.arcade.label,
                color = colors.secondary
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.panel,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (completedReminders.isEmpty()) {
                        Text(text = "No completed reminders recorded yet today.", style = MaterialTheme.typography.bodyMedium, color = colors.textMuted)
                    } else {
                        completedReminders.forEach { rem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = colors.reward, modifier = Modifier.size(20.dp))
                                    Column {
                                        Text(text = rem.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colors.text)
                                        Text(text = "${rem.scheduledTime} • ${rem.date} (${rem.repeatOption})", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                                    }
                                }
                                Surface(shape = RoundedCornerShape(6.dp), color = colors.reward.copy(alpha = 0.15f)) {
                                    Text(text = "Completed", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = colors.reward, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PatientReminderCard(
    reminder: PatientReminder,
    onComplete: () -> Unit
) {
    val colors = MmTheme.colors

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = colors.panel,
        border = BorderStroke(1.5.dp, colors.primary)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = reminder.type.icon, fontSize = 22.sp)
                    Column {
                        Text(text = reminder.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = colors.text)
                        Text(text = "${reminder.scheduledTime} • ${reminder.date} • ${reminder.repeatOption}", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                    }
                }
            }

            RetroButton(
                text = "✓ MARK AS COMPLETED",
                onClick = onComplete,
                style = RetroButtonStyle.Primary,
                minHeight = 46.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// -------------------------------------------------------------------------
// TAB 3: PROFILE SECTION
// -------------------------------------------------------------------------

@Composable
private fun PatientProfileTab(
    profile: PatientProfile,
    userIdentifier: String,
    onOpenChat: () -> Unit,
    onLogout: () -> Unit,
    onUpdateProfile: (PatientProfile) -> Unit = {}
) {
    val colors = MmTheme.colors
    val context = LocalContext.current
    var showEditProfileDialog by remember { mutableStateOf(false) }

    ArcadeScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PATIENT PROFILE",
                    style = MmTheme.arcade.titleSmall,
                    color = colors.reward,
                    modifier = Modifier.semantics { heading() }
                )

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showEditProfileDialog = true },
                    color = colors.panelInner,
                    border = BorderStroke(1.dp, colors.reward)
                ) {
                    Text(
                        text = "✏️ EDIT PROFILE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.reward,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Personal Information Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.panel,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val displayName = profile.fullName.ifBlank { userIdentifier.substringBefore("@").ifBlank { "Patient" } }
                            Text(text = displayName, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.text)
                            Text(text = "Login Account: $userIdentifier", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                        }
                        Surface(shape = CircleShape, color = colors.panelInner) {
                            Icon(imageVector = Icons.Filled.Person, contentDescription = null, tint = colors.reward, modifier = Modifier.padding(8.dp).size(28.dp))
                        }
                    }

                    HorizontalDivider(color = colors.border, modifier = Modifier.padding(vertical = 4.dp))

                    ProfileFieldRow(label = "Age", value = if (profile.age.isNotBlank()) "${profile.age} years old" else "Not set")
                    ProfileFieldRow(label = "Date of Birth", value = profile.dateOfBirth.ifBlank { "Not set" })
                    ProfileFieldRow(label = "Gender", value = profile.gender.ifBlank { "Female" })
                    ProfileFieldRow(label = "Contact Info", value = profile.contactInfo.ifBlank { userIdentifier })
                    ProfileFieldRow(label = "Language Preference", value = profile.language.ifBlank { "English" })
                }
            }

            // Diagnosed Conditions Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.panel,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "DIAGNOSED CONDITIONS / DISEASES",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.secondary
                    )

                    val conditions = if (profile.diagnosedConditions.isNotEmpty()) {
                        profile.diagnosedConditions
                    } else {
                        listOf("None reported")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        conditions.forEach { condition ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = colors.panelInner,
                                border = BorderStroke(1.dp, colors.secondary)
                            ) {
                                Text(
                                    text = condition,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.secondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Emergency Contact Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.panel,
                border = BorderStroke(1.5.dp, colors.primary)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Filled.ContactPhone, contentDescription = null, tint = colors.primary)
                            Text(
                                text = "EMERGENCY CONTACT",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.primary
                            )
                        }
                    }

                    val contactName = profile.emergencyContactName.ifBlank { "Not set" }
                    val contactRel = profile.emergencyContactRelationship.ifBlank { "Not set" }
                    val contactPhone = profile.emergencyContactPhone.ifBlank { profile.emergencyContact.ifBlank { "Not set" } }

                    ProfileFieldRow(label = "Contact Name", value = contactName)
                    ProfileFieldRow(label = "Relationship", value = contactRel)
                    ProfileFieldRow(label = "Phone Number", value = contactPhone)

                    if (contactPhone != "Not set" && contactPhone.isNotBlank()) {
                        RetroButton(
                            text = androidx.compose.ui.res.stringResource(com.memorymoments.app.R.string.btn_call_emergency),
                            icon = Icons.Filled.Call,
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$contactPhone"))
                                runCatching { context.startActivity(intent) }
                            },
                            style = RetroButtonStyle.Primary,
                            minHeight = 48.dp
                        )
                    }
                }
            }

            // Caregiver Link & Chat Option
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.panel,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.memorymoments.app.R.string.profile_caregiver_info).uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.text
                    )
                    Text(
                        text = "Caregiver: ${profile.caregiverName.ifBlank { "CG-998811 (Active Link)" }}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted
                    )

                    RetroButton(
                        text = androidx.compose.ui.res.stringResource(com.memorymoments.app.R.string.btn_chat_caregiver),
                        icon = Icons.AutoMirrored.Filled.Chat,
                        onClick = onOpenChat,
                        style = RetroButtonStyle.Secondary,
                        minHeight = 44.dp
                    )
                }
            }

            // Log Out Button
            RetroButton(
                text = androidx.compose.ui.res.stringResource(com.memorymoments.app.R.string.btn_logout),
                icon = Icons.AutoMirrored.Filled.Logout,
                onClick = onLogout,
                style = RetroButtonStyle.Ghost,
                minHeight = 48.dp
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showEditProfileDialog) {
        EditPatientProfileDialog(
            currentProfile = profile,
            onDismiss = { showEditProfileDialog = false },
            onSave = { updated ->
                onUpdateProfile(updated)
                showEditProfileDialog = false
            }
        )
    }
}

@Composable
private fun EditPatientProfileDialog(
    currentProfile: PatientProfile,
    onDismiss: () -> Unit,
    onSave: (PatientProfile) -> Unit
) {
    val colors = MmTheme.colors
    var fullName by remember { mutableStateOf(currentProfile.fullName) }
    var age by remember { mutableStateOf(currentProfile.age) }
    var dateOfBirth by remember { mutableStateOf(currentProfile.dateOfBirth) }
    var gender by remember { mutableStateOf(currentProfile.gender) }
    var emergencyName by remember { mutableStateOf(currentProfile.emergencyContactName) }
    var emergencyRel by remember { mutableStateOf(currentProfile.emergencyContactRelationship) }
    var emergencyPhone by remember { mutableStateOf(currentProfile.emergencyContactPhone) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Edit Profile Details", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RetroTextField(value = fullName, onValueChange = { fullName = it }, label = "Full Name")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    RetroTextField(value = age, onValueChange = { age = it }, label = "Age", modifier = Modifier.weight(1f))
                    RetroTextField(value = dateOfBirth, onValueChange = { dateOfBirth = it }, label = "DOB", modifier = Modifier.weight(1f))
                }
                RetroTextField(value = gender, onValueChange = { gender = it }, label = "Gender")
                Text(text = "Emergency Contact:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = colors.primary)
                RetroTextField(value = emergencyName, onValueChange = { emergencyName = it }, label = "Contact Name")
                RetroTextField(value = emergencyRel, onValueChange = { emergencyRel = it }, label = "Relationship")
                RetroTextField(value = emergencyPhone, onValueChange = { emergencyPhone = it }, label = "Phone Number")
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        currentProfile.copy(
                            fullName = fullName.trim(),
                            age = age.trim(),
                            dateOfBirth = dateOfBirth.trim(),
                            gender = gender.trim(),
                            emergencyContactName = emergencyName.trim(),
                            emergencyContactRelationship = emergencyRel.trim(),
                            emergencyContactPhone = emergencyPhone.trim(),
                            emergencyContact = emergencyPhone.trim()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) {
                Text("SAVE")
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

@Composable
private fun ProfileFieldRow(label: String, value: String) {
    val colors = MmTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = colors.textMuted)
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = colors.text)
    }
}

// -------------------------------------------------------------------------
// ADD REMINDER DIALOG (Title, Date, Time, Repeat Option)
// -------------------------------------------------------------------------

@Composable
private fun AddPatientReminderDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, ReminderType) -> Unit
) {
    val colors = MmTheme.colors
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("Today") }
    var time by remember { mutableStateOf("2:00 PM") }
    var repeatOption by remember { mutableStateOf("Daily") }
    var selectedType by remember { mutableStateOf(ReminderType.MEDICATION) }

    val repeatOptions = listOf("None", "Daily", "Weekly", "Weekdays")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Add Reminder", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                RetroTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Reminder Title (e.g. Heart Medication)"
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    RetroTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = "Date (e.g. Today)",
                        modifier = Modifier.weight(1f)
                    )
                    RetroTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = "Time (e.g. 2:00 PM)",
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(text = "Repeat Option:", style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeatOptions.forEach { opt ->
                        val isSelected = repeatOption == opt
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { repeatOption = opt },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) colors.primary else colors.panelInner,
                            border = BorderStroke(1.dp, if (isSelected) colors.primary else colors.border)
                        ) {
                            Text(
                                text = opt,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) colors.onPrimary else colors.textMuted,
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Text(text = "Category:", style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(ReminderType.MEDICATION, ReminderType.HYDRATION, ReminderType.ACTIVITY).forEach { type ->
                        val isSelected = selectedType == type
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedType = type },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) colors.reward else colors.panelInner,
                            border = BorderStroke(1.dp, if (isSelected) colors.reward else colors.border)
                        ) {
                            Text(
                                text = type.label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) colors.panel else colors.textMuted,
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(title.trim(), date.trim(), time.trim(), repeatOption, selectedType)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) {
                Text("ADD REMINDER")
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
