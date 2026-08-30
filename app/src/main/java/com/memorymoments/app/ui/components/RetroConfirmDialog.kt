package com.memorymoments.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import com.memorymoments.app.ui.theme.MmTheme

@Composable
fun RetroConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    cancelLabel: String = "CANCEL",
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens
    val isEasy = MmTheme.isEasyMode

    Dialog(onDismissRequest = onCancel) {
        RetroPanel(borderColor = colors.reward) {
            Text(
                text = title,
                style = if (isEasy) MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold) else MmTheme.arcade.hud,
                color = colors.reward
            )
            Spacer(modifier = Modifier.height(dimens.md))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.text
            )
            Spacer(modifier = Modifier.height(dimens.xl))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimens.sm)
            ) {
                RetroButton(
                    text = cancelLabel,
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    style = RetroButtonStyle.Secondary
                )
                RetroButton(
                    text = confirmLabel,
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    style = RetroButtonStyle.Primary
                )
            }
        }
    }
}
