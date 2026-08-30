package com.memorymoments.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.memorymoments.app.ui.theme.MmTheme

@Composable
fun FamilyMemberCard(
    name: String,
    relationship: String,
    imageUri: String?,
    modifier: Modifier = Modifier,
    onEdit: (() -> Unit)? = null,
    onRemove: (() -> Unit)? = null
) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens
    RetroCard(modifier = modifier, borderColor = colors.secondary.copy(alpha = 0.45f)) {
        MemberPortrait(
            imageUri = imageUri,
            contentDescription = "Photo of $name",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )
        Spacer(modifier = Modifier.height(dimens.md))
        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium,
            color = colors.text,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(dimens.xs))
        Text(
            text = relationship.uppercase(),
            style = MaterialTheme.typography.titleMedium,
            color = colors.textMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (onEdit != null) {
            Spacer(modifier = Modifier.height(dimens.md))
            RetroButton(
                text = "EDIT",
                onClick = onEdit,
                style = RetroButtonStyle.Secondary
            )
        }
        if (onRemove != null) {
            Spacer(modifier = Modifier.height(dimens.sm))
            RetroButton(
                text = "REMOVE",
                onClick = onRemove,
                style = RetroButtonStyle.Ghost
            )
        }
    }
}
