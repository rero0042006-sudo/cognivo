package com.memorymoments.app.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.memorymoments.app.ui.components.ArcadeScreen
import com.memorymoments.app.ui.components.RetroButton
import com.memorymoments.app.ui.components.RetroButtonStyle
import com.memorymoments.app.ui.components.RetroPanel
import com.memorymoments.app.ui.theme.MmTheme

data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val stepLabel: String
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens

    val pages = remember {
        listOf(
            OnboardingPage(
                title = "WELCOME TO MEMORY MOMENTS",
                subtitle = "A simple, friendly family recognition game designed to celebrate memories with the people you love.",
                icon = Icons.Filled.Star,
                stepLabel = "STEP 1 OF 3"
            ),
            OnboardingPage(
                title = "ADD YOUR FAMILY",
                subtitle = "Caregivers can add photos and names of family members. Your family photos stay strictly safe on this device.",
                icon = Icons.Filled.Groups,
                stepLabel = "STEP 2 OF 3"
            ),
            OnboardingPage(
                title = "LET'S PLAY!",
                subtitle = "Look at the prompt, choose the matching family portrait, earn stars, and practice at your own pace.",
                icon = Icons.Filled.PlayArrow,
                stepLabel = "STEP 3 OF 3"
            )
        )
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    val currentPage = pages[currentIndex]

    ArcadeScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = dimens.screenHorizontal, vertical = dimens.screenVertical),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(dimens.lg))

            AnimatedContent(
                targetState = currentPage,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "onboardingSlide"
            ) { page ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = page.stepLabel,
                        style = MmTheme.arcade.label,
                        color = colors.secondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(dimens.lg))

                    RetroPanel(borderColor = colors.reward.copy(alpha = 0.85f)) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = page.icon,
                                contentDescription = null,
                                tint = colors.reward,
                                modifier = Modifier.height(64.dp)
                            )

                            Spacer(modifier = Modifier.height(dimens.lg))

                            Text(
                                text = page.title,
                                style = MmTheme.arcade.titleSmall,
                                color = colors.reward,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(dimens.md))

                            Text(
                                text = page.subtitle,
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.text,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimens.xxl))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(dimens.md)
            ) {
                if (currentIndex < pages.size - 1) {
                    RetroButton(
                        text = "NEXT",
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        onClick = { currentIndex += 1 },
                        style = RetroButtonStyle.Primary,
                        minHeight = dimens.playButtonMin
                    )
                    RetroButton(
                        text = "SKIP",
                        onClick = onFinish,
                        style = RetroButtonStyle.Ghost
                    )
                } else {
                    RetroButton(
                        text = "GET STARTED",
                        icon = Icons.Filled.PlayArrow,
                        onClick = onFinish,
                        style = RetroButtonStyle.Primary,
                        minHeight = dimens.playButtonMin
                    )
                }
            }
        }
    }
}
