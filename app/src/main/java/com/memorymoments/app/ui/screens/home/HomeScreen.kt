package com.memorymoments.app.ui.screens.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.memorymoments.app.R
import com.memorymoments.app.model.AppStats
import com.memorymoments.app.model.Memory
import com.memorymoments.app.repository.DailyCompanionData
import com.memorymoments.app.repository.DailyGameOption
import com.memorymoments.app.repository.GameSettingsRepository
import com.memorymoments.app.ui.components.ArcadeScreen
import com.memorymoments.app.ui.components.ComboBadge
import com.memorymoments.app.ui.components.LevelBadge
import com.memorymoments.app.ui.components.RetroButton
import com.memorymoments.app.ui.components.RetroButtonStyle
import com.memorymoments.app.ui.components.RetroPanel
import com.memorymoments.app.ui.components.StarCounter
import com.memorymoments.app.ui.components.XpBar
import com.memorymoments.app.ui.theme.MmTheme
import com.memorymoments.app.ui.theme.rememberReduceMotion
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val XP_PLACEHOLDER_MAX = 100

@Composable
fun HomeScreen(
    isPreview: Boolean = false,
    onExitPreview: () -> Unit = {},
    userName: String = "",
    onPlay: () -> Unit,
    onFamily: () -> Unit,
    onPlaces: () -> Unit,
    onMemories: () -> Unit,
    onMusic: () -> Unit,
    onSettings: () -> Unit,
    onCaregiver: () -> Unit = {},
    onTryDemo: () -> Unit,
    onNavigateRoute: (String) -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val dailyData by viewModel.dailyCompanion.collectAsStateWithLifecycle()
    val isPlayingSong by viewModel.isPlayingSong.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val settingsRepo = remember { GameSettingsRepository(context) }
    val showHeritageContent by settingsRepo.showHeritageContent.collectAsStateWithLifecycle(initialValue = true)

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopAudio()
        }
    }

    if (MmTheme.isEasyMode) {
        StitchHomeContent(
            isPreview = isPreview,
            onExitPreview = onExitPreview,
            userName = userName,
            dailyData = dailyData,
            isPlayingSong = isPlayingSong,
            showHeritageContent = showHeritageContent,
            onPlaySong = { viewModel.togglePlaySong(dailyData.todaySong) },
            onNextSong = viewModel::nextSong,
            onNextMemory = viewModel::nextMemory,
            onStartGame = { route, key ->
                viewModel.markActivityComplete(key)
                if (route.isNotEmpty()) onNavigateRoute(route) else onPlay()
            },
            onPlay = onPlay,
            onFamily = onFamily,
            onPlaces = onPlaces,
            onMemories = onMemories,
            onMusic = onMusic,
            onSettings = onSettings,
            onCaregiver = onCaregiver
        )
    } else {
        GameModeHomeScreenContent(
            isPreview = isPreview,
            onExitPreview = onExitPreview,
            stats = stats,
            dailyData = dailyData,
            isPlayingSong = isPlayingSong,
            showHeritageContent = showHeritageContent,
            onPlaySong = { viewModel.togglePlaySong(dailyData.todaySong) },
            onPlay = onPlay,
            onFamily = onFamily,
            onPlaces = onPlaces,
            onMemories = onMemories,
            onMusic = onMusic,
            onSettings = onSettings,
            onCaregiver = onCaregiver,
            onTryDemo = onTryDemo
        )
    }
}

/**
 * Stitch-Style Accessible Home Dashboard with Northeast India Heritage Elements
 */
@Composable
private fun StitchHomeContent(
    isPreview: Boolean,
    onExitPreview: () -> Unit,
    userName: String,
    dailyData: DailyCompanionData,
    isPlayingSong: Boolean,
    showHeritageContent: Boolean,
    onPlaySong: () -> Unit,
    onNextSong: () -> Unit,
    onNextMemory: () -> Unit,
    onStartGame: (String, String) -> Unit,
    onPlay: () -> Unit,
    onFamily: () -> Unit,
    onPlaces: () -> Unit,
    onMemories: () -> Unit,
    onMusic: () -> Unit,
    onSettings: () -> Unit,
    onCaregiver: () -> Unit
) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens

    val formattedDate = remember {
        SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
    }

    val greetingText = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    val fullGreeting = remember(userName, greetingText) {
        if (userName.isNotBlank()) {
            "$greetingText, $userName!"
        } else {
            "$greetingText!"
        }
    }

    ArcadeScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Top-Left: "MORE" Action Button
                Surface(
                    modifier = Modifier
                        .height(42.dp)
                        .clickable(role = Role.Button, onClick = onSettings),
                    shape = RoundedCornerShape(14.dp),
                    color = colors.panel,
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "More Options",
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "MORE",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.primary
                        )
                    }
                }

                // Center: App Logo & Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(38.dp),
                        shape = CircleShape,
                        color = colors.panel,
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Eco,
                                contentDescription = "App Logo",
                                tint = colors.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Text(
                        text = "Cogniva",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = colors.primary
                    )
                }

                // Top-Right: Caregiver Chat Button
                Surface(
                    modifier = Modifier
                        .height(42.dp)
                        .clickable(role = Role.Button, onClick = onCaregiver),
                    shape = RoundedCornerShape(14.dp),
                    color = colors.primaryContainer,
                    border = BorderStroke(1.dp, colors.primary)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Chat with Caregiver",
                            tint = colors.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "CARE CHAT",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.onPrimaryContainer
                        )
                    }
                }
            }

            // Preview Mode Banner
            if (isPreview) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = colors.secondaryContainer,
                    border = BorderStroke(1.dp, colors.secondary)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👁️ PREVIEWING ELDER VIEW",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.onSecondaryContainer
                        )
                        RetroButton(
                            text = "EXIT PREVIEW",
                            onClick = onExitPreview,
                            style = RetroButtonStyle.Primary,
                            minHeight = 36.dp
                        )
                    }
                }
            }

            // Greeting & Date / Weather Section (Stitch Style)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = fullGreeting,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp
                    ),
                    color = colors.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
                        color = colors.textMuted
                    )
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .padding(0.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WbSunny,
                            contentDescription = "Weather",
                            tint = colors.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "72°F Pleasant",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = colors.textMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cards Column
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. TODAY'S MEMORY CARD (Stitch Dashboard Spec)
                if (dailyData.todayMemory != null) {
                    StitchMemoryCard(
                        memory = dailyData.todayMemory,
                        isCompleted = dailyData.completedActivities.contains("memory"),
                        onView = onMemories,
                        onNext = onNextMemory
                    )
                }

                // 2. TODAY'S ACTIVITY CARD (Stitch Daily Exercise)
                if (dailyData.todayGame != null) {
                    StitchActivityCard(
                        gameOption = dailyData.todayGame,
                        isCompleted = dailyData.completedActivities.contains("game"),
                        onStart = { onStartGame(dailyData.todayGame.route, "game") }
                    )
                }

                // 3. TODAY'S JOURNEY (Path Progress Metaphor)
                StitchJourneyCard(
                    completedActivities = dailyData.completedActivities
                )

                // 4. OUR ROOTS — NORTHEAST INDIA HERITAGE CARD (Optional & Configurable)
                if (showHeritageContent) {
                    StitchRootsCard(
                        onExplorePlaces = onPlaces,
                        onExploreMemories = onMemories
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

/**
 * Stitch Today's Memory Card
 */
@Composable
private fun StitchMemoryCard(
    memory: Memory,
    isCompleted: Boolean,
    onView: () -> Unit,
    onNext: () -> Unit
) {
    val colors = MmTheme.colors
    val context = LocalContext.current
    val photoUri = memory.photoUris.firstOrNull()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = colors.panel,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Memory Photo
            if (!photoUri.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(File(photoUri))
                        .crossfade(true)
                        .build(),
                    contentDescription = memory.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TODAY'S MEMORY",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.04.sp
                        ),
                        color = colors.primary
                    )

                    if (isCompleted) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Done",
                                tint = colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "DONE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "\"${memory.title}\"",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = colors.text
                )

                if (!memory.date.isNullOrBlank() || !memory.place.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val locationTag = listOfNotNull(memory.place, memory.state, memory.date).joinToString(" • ")
                    Text(
                        text = locationTag,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colors.secondary
                    )
                }

                if (!memory.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = memory.description,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                        color = colors.textMuted
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RetroButton(
                        text = "VIEW MEMORY",
                        icon = Icons.Filled.Book,
                        onClick = onView,
                        style = RetroButtonStyle.Primary,
                        modifier = Modifier.weight(1.6f),
                        minHeight = 52.dp
                    )

                    RetroButton(
                        text = "NEXT",
                        icon = Icons.Filled.SkipNext,
                        onClick = onNext,
                        style = RetroButtonStyle.Ghost,
                        modifier = Modifier.weight(1f),
                        minHeight = 52.dp
                    )
                }
            }
        }
    }
}

/**
 * Stitch Today's Activity Card (Daily Exercise)
 */
@Composable
private fun StitchActivityCard(
    gameOption: DailyGameOption,
    isCompleted: Boolean,
    onStart: () -> Unit
) {
    val colors = MmTheme.colors

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = colors.panel,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = colors.secondaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Extension,
                                contentDescription = null,
                                tint = colors.onSecondaryContainer,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "TODAY'S ACTIVITY",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.04.sp
                            ),
                            color = colors.secondary
                        )
                        Text(
                            text = gameOption.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 21.sp
                            ),
                            color = colors.text
                        )
                    }
                }

                if (isCompleted) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Done",
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "DONE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${gameOption.description} • 5 mins",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                color = colors.textMuted
            )

            Spacer(modifier = Modifier.height(18.dp))

            RetroButton(
                text = "START",
                icon = Icons.Filled.PlayArrow,
                onClick = onStart,
                style = RetroButtonStyle.Primary,
                minHeight = 52.dp
            )
        }
    }
}

/**
 * Stitch Today's Journey (Path Metaphor)
 */
@Composable
private fun StitchJourneyCard(
    completedActivities: Set<String>
) {
    val colors = MmTheme.colors
    val currentTimeStr = remember {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = colors.panelInner,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.home_todays_journey),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = colors.primary
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colors.panel,
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Text(
                        text = "⏰ $currentTimeStr",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.secondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Morning Node
                JourneyNode(
                    label = "Morning",
                    time = "8:00 AM",
                    active = true,
                    completed = completedActivities.isNotEmpty(),
                    colors = colors
                )

                // Connecting Line 1
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                ) {
                    Surface(
                        color = if (completedActivities.isNotEmpty()) colors.primary else colors.border,
                        modifier = Modifier.fillMaxSize()
                    ) {}
                }

                // Noon Node
                JourneyNode(
                    label = "Noon",
                    time = "12:00 PM",
                    active = completedActivities.isNotEmpty(),
                    completed = completedActivities.size >= 2,
                    colors = colors
                )

                // Connecting Line 2
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                ) {
                    Surface(
                        color = if (completedActivities.size >= 2) colors.primary else colors.border,
                        modifier = Modifier.fillMaxSize()
                    ) {}
                }

                // Evening Node
                JourneyNode(
                    label = "Evening",
                    time = "6:00 PM",
                    active = completedActivities.size >= 2,
                    completed = completedActivities.size >= 3,
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun JourneyNode(
    label: String,
    time: String,
    active: Boolean,
    completed: Boolean,
    colors: com.memorymoments.app.ui.theme.MmColors
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            color = when {
                completed -> colors.primary
                active -> colors.panel
                else -> colors.panelInner
            },
            border = BorderStroke(
                2.dp,
                if (active || completed) colors.primary else colors.border
            )
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (completed) Icons.Filled.CheckCircle else Icons.Filled.Eco,
                    contentDescription = null,
                    tint = if (completed) colors.onPrimary else colors.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            ),
            color = if (active) colors.text else colors.textMuted
        )

        Text(
            text = time,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal
            ),
            color = colors.textMuted
        )
    }
}

/**
 * Our Roots Card — Northeast India Heritage Section
 */
@Composable
private fun StitchRootsCard(
    onExplorePlaces: () -> Unit,
    onExploreMemories: () -> Unit
) {
    val colors = MmTheme.colors

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = colors.panel,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = colors.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Landscape,
                            contentDescription = null,
                            tint = colors.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = stringResource(R.string.heritage_our_roots),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.04.sp
                        ),
                        color = colors.primary
                    )
                    Text(
                        text = stringResource(R.string.heritage_places_stories),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        color = colors.text
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.heritage_explore_subtitle),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                color = colors.textMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RetroButton(
                    text = "PLACES",
                    onClick = onExplorePlaces,
                    style = RetroButtonStyle.Secondary,
                    modifier = Modifier.weight(1f),
                    minHeight = 48.dp
                )
                RetroButton(
                    text = "STORIES",
                    onClick = onExploreMemories,
                    style = RetroButtonStyle.Ghost,
                    modifier = Modifier.weight(1f),
                    minHeight = 48.dp
                )
            }
        }
    }
}

/**
 * Stitch Docked Bottom Navigation Bar (5 destinations, min 56dp touch targets)
 */
@Composable
private fun StitchBottomNavBar(
    activeTab: String,
    onHome: () -> Unit,
    onGames: () -> Unit,
    onMemories: () -> Unit,
    onFamily: () -> Unit,
    onMore: () -> Unit
) {
    val colors = MmTheme.colors

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.panel,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StitchNavItem(
                label = stringResource(R.string.nav_home),
                icon = Icons.Filled.Home,
                selected = activeTab == "Home",
                onClick = onHome
            )
            StitchNavItem(
                label = stringResource(R.string.nav_games),
                icon = Icons.Filled.SportsEsports,
                selected = activeTab == "Games",
                onClick = onGames
            )
            StitchNavItem(
                label = stringResource(R.string.nav_memories),
                icon = Icons.AutoMirrored.Filled.EventNote,
                selected = activeTab == "Memories",
                onClick = onMemories
            )
            StitchNavItem(
                label = stringResource(R.string.nav_family),
                icon = Icons.Filled.Groups,
                selected = activeTab == "Family",
                onClick = onFamily
            )
            StitchNavItem(
                label = stringResource(R.string.nav_more),
                icon = Icons.Filled.Menu,
                selected = activeTab == "More",
                onClick = onMore
            )
        }
    }
}

@Composable
private fun StitchNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = MmTheme.colors

    Column(
        modifier = Modifier
            .defaultMinSize(minWidth = 56.dp, minHeight = 56.dp)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(width = 46.dp, height = 30.dp),
            shape = RoundedCornerShape(15.dp),
            color = if (selected) colors.primaryContainer else Color.Transparent
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (selected) colors.onPrimaryContainer else colors.textMuted,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp
            ),
            color = if (selected) colors.primary else colors.textMuted
        )
    }
}

/**
 * Game Mode Home Content (Calm Forest Arcade)
 */
@Composable
private fun GameModeHomeScreenContent(
    isPreview: Boolean,
    onExitPreview: () -> Unit,
    stats: AppStats,
    dailyData: DailyCompanionData,
    isPlayingSong: Boolean,
    showHeritageContent: Boolean,
    onPlaySong: () -> Unit,
    onPlay: () -> Unit,
    onFamily: () -> Unit,
    onPlaces: () -> Unit,
    onMemories: () -> Unit,
    onMusic: () -> Unit,
    onSettings: () -> Unit,
    onCaregiver: () -> Unit,
    onTryDemo: () -> Unit
) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens
    val reduceMotion = rememberReduceMotion()

    var animatedLevel by remember { mutableStateOf(stats.level) }
    var animatedCombo by remember { mutableStateOf(stats.bestCombo) }

    LaunchedEffect(stats.level) {
        if (!reduceMotion) animatedLevel = stats.level
    }

    LaunchedEffect(stats.bestCombo) {
        if (!reduceMotion) animatedCombo = stats.bestCombo
    }

    ArcadeScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Retro Top Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MEMORY MOMENTS",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = colors.primary,
                    modifier = Modifier.semantics { heading() }
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onCaregiver) {
                        Icon(
                            imageVector = Icons.Filled.Groups,
                            contentDescription = "Caregiver Dashboard",
                            tint = colors.primary
                        )
                    }
                    IconButton(onClick = onSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
                            tint = colors.textMuted
                        )
                    }
                }
            }

            if (isPreview) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.secondaryContainer,
                    border = BorderStroke(1.dp, colors.secondary)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👁️ PREVIEWING ELDER VIEW",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.onSecondaryContainer
                        )
                        RetroButton(
                            text = "EXIT PREVIEW",
                            onClick = onExitPreview,
                            style = RetroButtonStyle.Primary,
                            minHeight = 36.dp
                        )
                    }
                }
            }

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LevelBadge(levelLabel = animatedLevel.toString())
                StarCounter(stars = stats.stars)
                ComboBadge(combo = animatedCombo)
            }

            XpBar(current = stats.xp % XP_PLACEHOLDER_MAX, max = XP_PLACEHOLDER_MAX)

            // Daily Companion Card
            if (dailyData.isEnabled && dailyData.todaySong != null) {
                RetroPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "TODAY'S SONG",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.primary
                        )
                        Text(
                            text = "🎵 ${dailyData.todaySong.title}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.text
                        )
                        RetroButton(
                            text = if (isPlayingSong) "STOP MUSIC" else "PLAY SONG",
                            onClick = onPlaySong,
                            style = RetroButtonStyle.Primary,
                            minHeight = 46.dp
                        )
                    }
                }
            }

            // Game Actions
            RetroButton(
                text = "START GAME",
                icon = Icons.Filled.SportsEsports,
                onClick = onPlay,
                style = RetroButtonStyle.Primary,
                minHeight = 56.dp
            )

            RetroButton(
                text = "FAMILIES & PEOPLE",
                icon = Icons.Filled.Groups,
                onClick = onFamily,
                style = RetroButtonStyle.Secondary,
                minHeight = 50.dp
            )

            RetroButton(
                text = "MY PLACES",
                icon = Icons.Filled.Landscape,
                onClick = onPlaces,
                style = RetroButtonStyle.Secondary,
                minHeight = 50.dp
            )

            RetroButton(
                text = "LIFE STORIES",
                icon = Icons.Filled.Book,
                onClick = onMemories,
                style = RetroButtonStyle.Secondary,
                minHeight = 50.dp
            )

            RetroButton(
                text = "MUSIC LIBRARY",
                icon = Icons.Filled.MusicNote,
                onClick = onMusic,
                style = RetroButtonStyle.Secondary,
                minHeight = 50.dp
            )

            RetroButton(
                text = "TRY DEMO MODE",
                icon = Icons.Filled.AutoAwesome,
                onClick = onTryDemo,
                style = RetroButtonStyle.Ghost,
                minHeight = 48.dp
            )
        }
    }
}
