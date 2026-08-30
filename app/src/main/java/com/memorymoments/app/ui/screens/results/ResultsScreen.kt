package com.memorymoments.app.ui.screens.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.memorymoments.app.repository.GameSettingsRepository
import com.memorymoments.app.repository.PatientProgressRepository
import com.memorymoments.app.ui.components.ArcadeScreen
import com.memorymoments.app.ui.components.ArcadeTopBar
import com.memorymoments.app.ui.components.ComboBadge
import com.memorymoments.app.ui.components.RetroButton
import com.memorymoments.app.ui.components.RetroButtonStyle
import com.memorymoments.app.ui.components.RetroPanel
import com.memorymoments.app.ui.components.StarCounter
import com.memorymoments.app.ui.theme.MmTheme

@Composable
fun ResultsScreen(
    stars: Int = 0,
    xp: Int = 0,
    bestCombo: Int = 0,
    totalRounds: Int = 10,
    correctAnswers: Int = 0,
    gameTitle: String = "Who's Who? (Face Recognition)",
    category: String = "Recognition",
    onHome: () -> Unit,
    onPlayAgain: () -> Unit,
    onNextActivity: () -> Unit = onHome,
    onViewFamily: () -> Unit = {}
) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens
    val context = LocalContext.current
    val settingsRepo = remember { GameSettingsRepository(context) }
    val progressRepo = remember { PatientProgressRepository(context) }

    val percentage = if (totalRounds > 0) ((correctAnswers.toFloat() / totalRounds) * 100).toInt() else 0
    var isRecorded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!isRecorded) {
            isRecorded = true
            settingsRepo.recordGameCompletion(
                correct = correctAnswers,
                total = totalRounds,
                bestComboInGame = bestCombo
            )
            progressRepo.recordActivity(
                title = gameTitle,
                score = "$percentage% Score",
                category = category
            )
        }
    }

    ArcadeScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = dimens.screenHorizontal, vertical = dimens.screenVertical)
        ) {
            ArcadeTopBar(title = "Results", onBack = onHome)

            RetroPanel(borderColor = colors.reward.copy(alpha = 0.85f)) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(dimens.md)
                ) {
                    Text(
                        text = if (percentage >= 70) "GREAT JOB!" else "WELL PLAYED!",
                        style = MmTheme.arcade.titleSmall,
                        color = colors.reward,
                        textAlign = TextAlign.Center
                    )

                    StarCounter(stars = stars, padded = false, label = "STARS EARNED")

                    Text(
                        text = "$correctAnswers / $totalRounds CORRECT",
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.text,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "$percentage% ACCURACY",
                        style = MmTheme.arcade.hud,
                        color = colors.success,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "+$xp XP",
                            style = MmTheme.arcade.hud,
                            color = colors.secondary
                        )
                        ComboBadge(combo = bestCombo, label = "x$bestCombo BEST STREAK")
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimens.md))

            RetroPanel(borderColor = colors.border) {
                Text(
                    text = "Activity complete! Your result has been saved to your progress history to keep your mind active and healthy.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(dimens.xl))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(dimens.md)
            ) {
                RetroButton(
                    text = "NEXT RECOMMENDED ACTIVITY",
                    icon = Icons.Filled.AutoAwesome,
                    onClick = onNextActivity,
                    style = RetroButtonStyle.Primary,
                    minHeight = dimens.playButtonMin
                )

                RetroButton(
                    text = "PLAY AGAIN",
                    icon = Icons.Filled.PlayArrow,
                    onClick = onPlayAgain,
                    style = RetroButtonStyle.Secondary,
                    minHeight = dimens.buttonMin
                )

                RetroButton(
                    text = "VIEW FAMILY",
                    icon = Icons.Filled.Groups,
                    onClick = onViewFamily,
                    style = RetroButtonStyle.Ghost,
                    minHeight = dimens.buttonMin
                )

                RetroButton(
                    text = "HOME",
                    icon = Icons.Filled.Home,
                    onClick = onHome,
                    style = RetroButtonStyle.Ghost
                )
            }
        }
    }
}
