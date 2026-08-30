package com.memorymoments.app.ui.screens.gamesetup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memorymoments.app.ui.components.ArcadeScreen
import com.memorymoments.app.ui.components.ArcadeTopBar
import com.memorymoments.app.ui.theme.MmTheme

@Composable
fun GameSelectionScreen(
    onBack: () -> Unit,
    onSelectWhosWho: () -> Unit,
    onSelectWhereWasIt: () -> Unit,
    onSelectNameThatTune: () -> Unit
) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens

    ArcadeScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            ArcadeTopBar(title = "Choose Activity", onBack = onBack)

            Text(
                text = "Memory Activities",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = colors.text
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Select an activity to practice recognizing today:",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                color = colors.textMuted
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Game Choice Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StitchGameChoiceCard(
                    title = "Who's Who?",
                    subtitle = "Practice recognizing your family members and loved ones.",
                    icon = Icons.Filled.PhotoLibrary,
                    iconBg = colors.primaryContainer,
                    iconTint = colors.onPrimaryContainer,
                    onClick = onSelectWhosWho
                )

                StitchGameChoiceCard(
                    title = "Where Was It?",
                    subtitle = "Recognize familiar places from your hometown & travels.",
                    icon = Icons.Filled.Place,
                    iconBg = colors.tertiaryContainer,
                    iconTint = colors.text,
                    onClick = onSelectWhereWasIt
                )

                StitchGameChoiceCard(
                    title = "Name That Tune",
                    subtitle = "Listen to familiar melodies and identify your favorite songs.",
                    icon = Icons.Filled.MusicNote,
                    iconBg = colors.secondaryContainer,
                    iconTint = colors.onSecondaryContainer,
                    onClick = onSelectNameThatTune
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StitchGameChoiceCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    val colors = MmTheme.colors

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = colors.panel,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Surface(
                modifier = Modifier.size(60.dp),
                shape = CircleShape,
                color = iconBg
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = colors.text
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp
                    ),
                    color = colors.textMuted
                )
            }
        }
    }
}
