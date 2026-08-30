package com.memorymoments.app.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.memorymoments.app.model.DistractorStyle
import com.memorymoments.app.model.TextSizeOption
import com.memorymoments.app.model.UiMode
import com.memorymoments.app.ui.components.ArcadeScreen
import com.memorymoments.app.ui.components.ArcadeTopBar
import com.memorymoments.app.ui.components.RetroButton
import com.memorymoments.app.ui.components.RetroButtonStyle
import com.memorymoments.app.ui.components.RetroConfirmDialog
import com.memorymoments.app.ui.theme.MmTheme

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onFamily: () -> Unit,
    onCaregiver: () -> Unit = {},
    onDistractors: () -> Unit,
    onLogout: (() -> Unit)? = null,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val distractorStyle by viewModel.distractorStyle.collectAsStateWithLifecycle()
    val textSize by viewModel.textSize.collectAsStateWithLifecycle()
    val reduceAnimations by viewModel.reduceAnimations.collectAsStateWithLifecycle()
    val soundEffects by viewModel.soundEffects.collectAsStateWithLifecycle()
    val haptics by viewModel.haptics.collectAsStateWithLifecycle()
    val ttsEnabled by viewModel.ttsEnabled.collectAsStateWithLifecycle()
    val ttsSlowRate by viewModel.ttsSlowRate.collectAsStateWithLifecycle()
    val stats by viewModel.caregiverStats.collectAsStateWithLifecycle()
    val uiMode by viewModel.uiMode.collectAsStateWithLifecycle()
    val dailyCompanionEnabled by viewModel.dailyCompanionEnabled.collectAsStateWithLifecycle()
    val dailyMusicEnabled by viewModel.dailyMusicEnabled.collectAsStateWithLifecycle()
    val dailyMemoriesEnabled by viewModel.dailyMemoriesEnabled.collectAsStateWithLifecycle()
    val dailyGamesEnabled by viewModel.dailyGamesEnabled.collectAsStateWithLifecycle()
    val dailyTalkEnabled by viewModel.dailyTalkEnabled.collectAsStateWithLifecycle()
    val showHeritageContent by viewModel.showHeritageContent.collectAsStateWithLifecycle()

    val colors = MmTheme.colors
    val dimens = MmTheme.dimens
    val isEasy = MmTheme.isEasyMode

    ArcadeScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            ArcadeTopBar(title = "Settings", onBack = onBack)

            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = colors.primary
            )

            val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
            val context = androidx.compose.ui.platform.LocalContext.current

            // 0. LANGUAGE PREFERENCE
            SettingsCard(title = "LANGUAGE PREFERENCE / ভাষা পছন্দ") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    com.memorymoments.app.utils.AppLanguageManager.SUPPORTED_LANGUAGES.forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(role = Role.RadioButton) {
                                    viewModel.setAppLanguage(lang.code)
                                    com.memorymoments.app.utils.AppLanguageManager.applyLocale(context, lang.code)
                                    (context as? android.app.Activity)?.recreate()
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = appLanguage.equals(lang.code, ignoreCase = true),
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = colors.primary,
                                    unselectedColor = colors.textMuted
                                )
                            )
                            Column(modifier = Modifier.padding(start = 10.dp)) {
                                Text(
                                    text = lang.nativeName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.text
                                )
                                Text(
                                    text = lang.englishName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textMuted
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. CULTURAL CUSTOMIZATION
            SettingsCard(title = "CULTURAL CUSTOMIZATION") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingToggleRow(
                        title = "Show Heritage Content",
                        subtitle = "Display Northeast India cultural stories, places, and regional reminiscence cards",
                        checked = showHeritageContent,
                        onCheckedChange = viewModel::setShowHeritageContent
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))


            // 1. DAILY COMPANION (PHASE 13)
            SettingsCard(title = "DAILY COMPANION") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingToggleRow(
                        title = "Enable Daily Companion",
                        subtitle = "Show suggested daily music, memory, and game on Home",
                        checked = dailyCompanionEnabled,
                        onCheckedChange = viewModel::setDailyCompanionEnabled
                    )

                    if (dailyCompanionEnabled) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "DAILY ACTIVITIES",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textMuted
                        )

                        SettingToggleRow(
                            title = "Music activities",
                            subtitle = "Suggest a daily song from your library",
                            checked = dailyMusicEnabled,
                            onCheckedChange = viewModel::setDailyMusicEnabled
                        )

                        SettingToggleRow(
                            title = "Memory activities",
                            subtitle = "Suggest a daily personal photo or life story",
                            checked = dailyMemoriesEnabled,
                            onCheckedChange = viewModel::setDailyMemoriesEnabled
                        )

                        SettingToggleRow(
                            title = "Game activities",
                            subtitle = "Suggest a daily memory or recognition game",
                            checked = dailyGamesEnabled,
                            onCheckedChange = viewModel::setDailyGamesEnabled
                        )

                        SettingToggleRow(
                            title = "Memory Talk activities",
                            subtitle = "Suggest voice chat reminiscence on memories",
                            checked = dailyTalkEnabled,
                            onCheckedChange = viewModel::setDailyTalkEnabled
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. APPEARANCE (UI MODE)
            SettingsCard(title = "APPEARANCE") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "INTERFACE STYLE",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.primary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RetroButton(
                            text = "❤️ EASY MODE",
                            onClick = { viewModel.setUiMode(UiMode.EASY) },
                            style = if (uiMode == UiMode.EASY) RetroButtonStyle.Primary else RetroButtonStyle.Ghost,
                            modifier = Modifier.weight(1f),
                            minHeight = 50.dp
                        )
                        RetroButton(
                            text = "🎮 GAME MODE",
                            onClick = { viewModel.setUiMode(UiMode.GAME) },
                            style = if (uiMode == UiMode.GAME) RetroButtonStyle.Primary else RetroButtonStyle.Ghost,
                            modifier = Modifier.weight(1f),
                            minHeight = 50.dp
                        )
                    }
                    Text(
                        text = if (uiMode == UiMode.EASY) {
                            "Senior Easy Mode: Calm, warm, clear, high contrast, and simplified navigation."
                        } else {
                            "Retro Arcade Mode: Classic arcade panels, XP bars, combo meters, and energetic visual feedback."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. VOICE & SPEECH (PHASE 11)
            SettingsCard(title = "VOICE & SPEECH") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingToggleRow(
                        title = "Read responses aloud",
                        subtitle = "Use voice text-to-speech during Memory Talk",
                        checked = ttsEnabled,
                        onCheckedChange = viewModel::setTtsEnabled
                    )

                    SettingToggleRow(
                        title = "Slow speech rate",
                        subtitle = "Speak companion responses at a calm, gentle pace",
                        checked = ttsSlowRate,
                        onCheckedChange = viewModel::setTtsSlowRate
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. ACCESSIBILITY & DISPLAY
            SettingsCard(title = "ACCESSIBILITY & DISPLAY") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "TEXT SIZE",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.primary
                    )
                    TextSizeOption.entries.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(role = Role.RadioButton) { viewModel.setTextSize(option) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = textSize == option,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = colors.primary,
                                    unselectedColor = colors.textMuted
                                )
                            )
                            Column(modifier = Modifier.padding(start = 10.dp)) {
                                Text(
                                    text = option.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.text
                                )
                                Text(
                                    text = "${(option.scaleFactor * 100).toInt()}% text size",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    SettingToggleRow(
                        title = "Sound Effects",
                        subtitle = "Play subtle audio chimes during gameplay",
                        checked = soundEffects,
                        onCheckedChange = viewModel::setSoundEffects
                    )

                    SettingToggleRow(
                        title = "Haptic Feedback",
                        subtitle = "Gentle tap vibrations on answer selection",
                        checked = haptics,
                        onCheckedChange = viewModel::setHaptics
                    )

                    SettingToggleRow(
                        title = "Reduce Animations",
                        subtitle = "Simplify screen transitions and motion",
                        checked = reduceAnimations,
                        onCheckedChange = viewModel::setReduceAnimations
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. CAREGIVER MANAGEMENT
            SettingsCard(title = "CAREGIVER MANAGEMENT") {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "GAMES PLAYED", style = MaterialTheme.typography.labelMedium, color = colors.textMuted)
                            Text(text = "${stats.gamesPlayed}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.primary)
                        }
                        Column {
                            Text(text = "BEST SCORE", style = MaterialTheme.typography.labelMedium, color = colors.textMuted)
                            Text(text = "${stats.bestScore} / 10", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.primary)
                        }
                        Column {
                            Text(text = "BEST STREAK", style = MaterialTheme.typography.labelMedium, color = colors.textMuted)
                            Text(text = "x${stats.bestStreak}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.secondary)
                        }
                    }

                    Text(
                        text = "Game scores are for enjoyment and engagement only, not a medical or diagnostic assessment.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    RetroButton(
                        text = "CAREGIVER DASHBOARD",
                        icon = Icons.Filled.Groups,
                        onClick = onCaregiver,
                        style = RetroButtonStyle.Primary
                    )

                    RetroButton(
                        text = "MANAGE FAMILY MEMBERS",
                        icon = Icons.Filled.Groups,
                        onClick = onFamily,
                        style = RetroButtonStyle.Secondary
                    )

                    RetroButton(
                        text = "DISTRACTOR LAB",
                        icon = Icons.Filled.Science,
                        onClick = onDistractors,
                        style = RetroButtonStyle.Secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. ABOUT & PRIVACY
            SettingsCard(title = "ABOUT & PRIVACY") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Memory Moments v0.1.0\nA personalized memory, reminiscence and engagement tool designed for older adults.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.text
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Family photos stay securely on your device. Microphones are active only during spoken memory sessions and audio is never stored permanently without caregiver consent.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // RESET DATA
            RetroButton(
                text = "RESET GAME DATA",
                icon = Icons.Filled.DeleteForever,
                onClick = viewModel::requestReset,
                style = RetroButtonStyle.Ghost
            )

            if (onLogout != null) {
                Spacer(modifier = Modifier.height(12.dp))
                RetroButton(
                    text = "LOG OUT / SWITCH ACCOUNT",
                    icon = Icons.AutoMirrored.Filled.Logout,
                    onClick = onLogout,
                    style = RetroButtonStyle.Secondary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (uiState.showResetConfirm) {
        RetroConfirmDialog(
            title = "RESET GAME DATA?",
            message = "This will reset all stats, combo records, and settings. Family photos will be preserved.",
            confirmLabel = "RESET",
            cancelLabel = "CANCEL",
            onConfirm = viewModel::confirmReset,
            onCancel = viewModel::cancelReset
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    val colors = MmTheme.colors

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
                .padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = colors.text
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = MmTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Switch) { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.text
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMuted
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.panel,
                checkedTrackColor = colors.primary,
                uncheckedThumbColor = colors.panelInner,
                uncheckedTrackColor = colors.border
            )
        )
    }
}
