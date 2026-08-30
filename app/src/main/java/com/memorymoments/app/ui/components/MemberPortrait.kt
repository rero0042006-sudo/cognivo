package com.memorymoments.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil.compose.SubcomposeAsyncImage
import com.memorymoments.app.ui.theme.MmTheme
import com.memorymoments.app.utils.ImageStorage

@Composable
fun MemberPortrait(
    imageUri: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = MmTheme.dimens.portraitSlot
) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens
    val surfaceModifier = if (modifier == Modifier) {
        Modifier.size(size)
    } else {
        modifier
    }
    Surface(
        modifier = surfaceModifier,
        shape = MmTheme.shapes.card,
        color = colors.backgroundElevated,
        border = BorderStroke(dimens.borderThick, colors.secondary.copy(alpha = 0.7f))
    ) {
        val model = ImageStorage.loadModel(imageUri)
        if (model == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = contentDescription,
                    tint = colors.textMuted,
                    modifier = Modifier.fillMaxSize(0.42f)
                )
            }
        } else {
            SubcomposeAsyncImage(
                model = model,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.secondary)
                    }
                },
                error = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.BrokenImage,
                            contentDescription = "Photo could not be loaded",
                            tint = colors.textMuted,
                            modifier = Modifier.fillMaxSize(0.4f)
                        )
                    }
                }
            )
        }
    }
}
