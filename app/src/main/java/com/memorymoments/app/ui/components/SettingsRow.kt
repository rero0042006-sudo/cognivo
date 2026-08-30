package com.memorymoments.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.memorymoments.app.ui.theme.MmTheme

@Composable
fun SettingsRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.md)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.secondary,
            modifier = Modifier.size(dimens.xxl)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = colors.text)
            Spacer(modifier = Modifier.height(dimens.xs))
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.textMuted)
        }
    }
}
