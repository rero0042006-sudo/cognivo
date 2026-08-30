package com.memorymoments.app.ui.screens.memorytalk

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.memorymoments.app.R
import com.memorymoments.app.speech.SpeechState
import com.memorymoments.app.ui.components.ArcadeScreen
import com.memorymoments.app.ui.components.ArcadeTopBar
import com.memorymoments.app.ui.components.RetroButton
import com.memorymoments.app.ui.components.RetroButtonStyle
import com.memorymoments.app.ui.components.RetroConfirmDialog
import com.memorymoments.app.ui.theme.MmTheme
import java.io.File

@Composable
fun MemoryTalkScreen(
    onBack: () -> Unit,
    onAddMemory: () -> Unit,
    viewModel: MemoryTalkViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showPermissionDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        } else {
            showPermissionDialog = true
        }
    }

    fun handleMicTap() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            if (state.speechState is SpeechState.Listening) {
                viewModel.stopListening()
            } else {
                viewModel.startListening()
            }
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopSpeaking()
            viewModel.speechManager.reset()
        }
    }

    val colors = MmTheme.colors
    val dimens = MmTheme.dimens

    ArcadeScreen {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.primary)
            }
            return@ArcadeScreen
        }

        if (state.memories.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                ArcadeTopBar(title = stringResource(R.string.memory_talk_title), onBack = onBack)
                Spacer(modifier = Modifier.height(24.dp))
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
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Book,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = "NO MEMORIES YET",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = colors.text
                        )
                        Text(
                            text = "Add some personal memories or photos to start chatting with Memory Talk.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.textMuted,
                            textAlign = TextAlign.Center
                        )
                        RetroButton(
                            text = "+ ADD MEMORY",
                            icon = Icons.Filled.Book,
                            onClick = onAddMemory,
                            style = RetroButtonStyle.Primary,
                            minHeight = 56.dp
                        )
                    }
                }
            }
            return@ArcadeScreen
        }

        val memory = state.currentMemory
        if (memory == null) return@ArcadeScreen

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ArcadeTopBar(title = stringResource(R.string.memory_talk_title), onBack = onBack)

            Text(
                text = stringResource(R.string.memory_talk_title),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                ),
                color = colors.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Let's look back on this memory together.",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
                color = colors.textMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Memory Photo Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = colors.panel,
                border = BorderStroke(1.dp, colors.border),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    val photoUri = memory.photoUris.firstOrNull()
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
                                .height(220.dp)
                                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "\"${memory.title}\"",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 21.sp
                            ),
                            color = colors.text
                        )

                        if (!memory.date.isNullOrBlank() || !memory.place.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            val tag = listOfNotNull(memory.place, memory.state, memory.date).joinToString(" • ")
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.secondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Conversational Prompt & History
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
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (state.conversation.isEmpty()) {
                        Text(
                            text = "💭 \"Who was with you for this memory?\"",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = colors.primary,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            state.conversation.forEach { msg ->
                                val isUser = msg.speaker == "user"
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isUser) colors.panelInner else colors.primaryContainer.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, if (isUser) colors.border else colors.primaryContainer)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = if (isUser) "YOU:" else "COMPANION:",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isUser) colors.textMuted else colors.primary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = msg.text,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontSize = 17.sp,
                                                fontWeight = if (isUser) FontWeight.Normal else FontWeight.Medium
                                            ),
                                            color = colors.text
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (state.isGeneratingResponse) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colors.primary, strokeWidth = 2.dp)
                            Text(
                                text = "Thinking warmly...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textMuted
                            )
                        }
                    }

                    // Read aloud button if response is present
                    if (state.currentAiResponse != null && !state.isGeneratingResponse) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            modifier = Modifier.clickable {
                                if (state.isTtsSpeaking) viewModel.stopSpeaking() else viewModel.speakCurrentResponse()
                            },
                            shape = RoundedCornerShape(24.dp),
                            color = colors.panelInner,
                            border = BorderStroke(1.dp, colors.border)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (state.isTtsSpeaking) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = if (state.isTtsSpeaking) "Stop voice" else "Read aloud",
                                    tint = colors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = if (state.isTtsSpeaking) stringResource(R.string.memory_talk_stop_voice) else stringResource(R.string.memory_talk_read_aloud),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.primary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons: [ 🎤 TAP TO SPEAK ] & [ I DON'T REMEMBER ]
            val isListening = state.speechState is SpeechState.Listening
            val isProcessing = state.speechState is SpeechState.Processing

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Main Speak Button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isProcessing, onClick = ::handleMicTap),
                    shape = RoundedCornerShape(20.dp),
                    color = if (isListening) Color(0xFFFFEAEA) else colors.primary,
                    border = BorderStroke(if (isListening) 2.dp else 0.dp, if (isListening) Color(0xFFBA1A1A) else Color.Transparent),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp, horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Filled.Stop else Icons.Filled.Mic,
                            contentDescription = null,
                            tint = if (isListening) Color(0xFFBA1A1A) else colors.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = when {
                                isListening -> stringResource(R.string.memory_talk_listening)
                                isProcessing -> "THINKING..."
                                else -> stringResource(R.string.memory_talk_speak)
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = if (isListening) Color(0xFFBA1A1A) else colors.onPrimary
                        )
                    }
                }

                // "I DON'T REMEMBER" Option Button (Crucial zero-pressure feature for older adults)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isProcessing, onClick = viewModel::onDontRemember),
                    shape = RoundedCornerShape(20.dp),
                    color = colors.panel,
                    border = BorderStroke(1.5.dp, colors.border),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = null,
                            tint = colors.textMuted,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.memory_talk_dont_remember),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 17.sp
                            ),
                            color = colors.textMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Next Memory Action
            RetroButton(
                text = "NEXT MEMORY",
                icon = Icons.Filled.SkipNext,
                onClick = viewModel::nextMemory,
                style = RetroButtonStyle.Ghost,
                minHeight = 50.dp
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showPermissionDialog) {
        RetroConfirmDialog(
            title = "MICROPHONE ACCESS",
            message = "Memory Moments uses your microphone so you can talk comfortably about your memories.",
            confirmLabel = "ALLOW",
            cancelLabel = "NOT NOW",
            onConfirm = {
                showPermissionDialog = false
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            },
            onCancel = {
                showPermissionDialog = false
            }
        )
    }
}
