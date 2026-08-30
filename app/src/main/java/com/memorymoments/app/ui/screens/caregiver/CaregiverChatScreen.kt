package com.memorymoments.app.ui.screens.caregiver

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.memorymoments.app.model.CaregiverChatMessage
import com.memorymoments.app.model.UserRole
import com.memorymoments.app.repository.CaregiverRepository
import com.memorymoments.app.ui.components.ArcadeScreen
import com.memorymoments.app.ui.components.ArcadeTopBar
import com.memorymoments.app.ui.components.RetroButton
import com.memorymoments.app.ui.components.RetroButtonStyle
import com.memorymoments.app.ui.components.RetroTextField
import com.memorymoments.app.ui.theme.MmTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CaregiverChatScreen(
    currentViewerRole: UserRole = UserRole.CAREGIVER,
    onBack: () -> Unit,
    onCall: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { CaregiverRepository(context) }
    val messages by repository.chatMessages.collectAsStateWithLifecycle(initialValue = emptyList())
    val linkedPatient by repository.linkedPatient.collectAsStateWithLifecycle(initialValue = null)
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var messageText by remember { mutableStateOf("") }
    var activeSenderRole by remember { mutableStateOf(currentViewerRole) }

    val quickReplies = remember(activeSenderRole) {
        if (activeSenderRole == UserRole.CAREGIVER) {
            listOf(
                "Good morning! How are you feeling?",
                "Don't forget your water! 💧",
                "Great job on the memory game! 🌟",
                "I'll visit you at 4 PM today."
            )
        } else {
            listOf(
                "I'm feeling good today!",
                "Just finished my morning walk.",
                "Thank you for checking in ❤️",
                "Can you call me later?"
            )
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
        repository.markChatAsRead()
    }

    ArcadeScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            ArcadeTopBar(
                title = if (activeSenderRole == UserRole.CAREGIVER) "Chat with ${linkedPatient?.name ?: "Patient"}" else "Chat with Caregiver",
                onBack = onBack
            )

            // Header Banner with Call Action
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(14.dp),
                color = colors.panel,
                border = BorderStroke(1.dp, colors.border)
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = colors.primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (activeSenderRole == UserRole.CAREGIVER) Icons.Filled.Person else Icons.Filled.Favorite,
                                    contentDescription = null,
                                    tint = colors.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = if (activeSenderRole == UserRole.CAREGIVER) (linkedPatient?.name ?: "Eleanor") else "Caregiver Support",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = colors.text
                            )
                            Text(
                                text = "Active • Two-way linked channel",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.reward
                            )
                        }
                    }

                    // Sender Role Switcher (allows demoing patient and caregiver side)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colors.panelInner,
                        border = BorderStroke(1.dp, colors.secondary),
                        modifier = Modifier.clickable {
                            activeSenderRole = if (activeSenderRole == UserRole.CAREGIVER) UserRole.PATIENT else UserRole.CAREGIVER
                        }
                    ) {
                        Text(
                            text = "Send as: ${if (activeSenderRole == UserRole.CAREGIVER) "Caregiver" else "Patient"}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.secondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Chat Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    val isMe = msg.senderRole == activeSenderRole
                    ChatBubble(message = msg, isMe = isMe)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Replies Row
            Text(
                text = "QUICK MESSAGES:",
                style = MmTheme.arcade.label,
                color = colors.textMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickReplies.take(2).forEach { reply ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                coroutineScope.launch {
                                    repository.sendChatMessage(activeSenderRole, reply)
                                }
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = colors.panelInner,
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Text(
                            text = reply,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = colors.text,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RetroTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    label = "Type message...",
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                Surface(
                    modifier = Modifier
                        .size(52.dp)
                        .clickable {
                            if (messageText.isNotBlank()) {
                                val textToSend = messageText
                                messageText = ""
                                coroutineScope.launch {
                                    repository.sendChatMessage(activeSenderRole, textToSend)
                                }
                            }
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = colors.primary,
                    border = BorderStroke(1.dp, colors.primary)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = colors.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: CaregiverChatMessage,
    isMe: Boolean
) {
    val colors = MmTheme.colors
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }

    val bubbleColor = if (isMe) colors.primary else colors.panelInner
    val textColor = if (isMe) colors.onPrimary else colors.text
    val alignment = if (isMe) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Text(
            text = if (message.senderRole == UserRole.CAREGIVER) "Caregiver" else "Patient",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = colors.textMuted,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )

        Surface(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isMe) 14.dp else 2.dp,
                bottomEnd = if (isMe) 2.dp else 14.dp
            ),
            color = bubbleColor,
            border = BorderStroke(1.dp, if (isMe) colors.primary else colors.border),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = textColor.copy(alpha = 0.75f)
                    )
                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.DoneAll,
                            contentDescription = "Read",
                            tint = textColor.copy(alpha = 0.75f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}
