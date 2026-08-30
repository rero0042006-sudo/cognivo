package com.memorymoments.app.ui.screens.family

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.memorymoments.app.R
import com.memorymoments.app.model.FamilyMember
import com.memorymoments.app.ui.components.ArcadeScreen
import com.memorymoments.app.ui.components.ArcadeTopBar
import com.memorymoments.app.ui.components.MemberPortrait
import com.memorymoments.app.ui.components.RetroButton
import com.memorymoments.app.ui.components.RetroButtonStyle
import com.memorymoments.app.ui.components.RetroConfirmDialog
import com.memorymoments.app.ui.components.RetroPanel
import com.memorymoments.app.ui.theme.MmTheme
import com.memorymoments.app.utils.Constants
import java.io.File

@Composable
fun FamilySetupScreen(
    onBack: () -> Unit,
    onAddMember: () -> Unit,
    onEditMember: (String) -> Unit,
    viewModel: FamilyViewModel = viewModel()
) {
    val gallery by viewModel.gallery.collectAsStateWithLifecycle()
    val pendingDelete by viewModel.pendingDelete.collectAsStateWithLifecycle()
    FamilyGalleryContent(
        state = gallery,
        pendingDelete = pendingDelete,
        onBack = onBack,
        onAddMember = onAddMember,
        onEditMember = onEditMember,
        onRequestDelete = viewModel::requestDelete,
        onCancelDelete = viewModel::cancelDelete,
        onConfirmDelete = viewModel::confirmDelete
    )
}

@Composable
fun FamilyGalleryContent(
    state: FamilyGalleryState,
    pendingDelete: FamilyMember?,
    onBack: () -> Unit,
    onAddMember: () -> Unit,
    onEditMember: (String) -> Unit,
    onRequestDelete: (FamilyMember) -> Unit,
    onCancelDelete: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens

    ArcadeScreen {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.primary)
            }
            return@ArcadeScreen
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Top Bar
            item(span = { GridItemSpan(2) }) {
                ArcadeTopBar(title = stringResource(R.string.family_title), onBack = onBack)
            }

            // Title & Subtitle
            item(span = { GridItemSpan(2) }) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.family_title),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        ),
                        color = colors.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.family_subtitle),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
                        color = colors.textMuted
                    )
                }
            }

            // Empty State
            if (state.members.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    EmptyFamilyState(onAddMember = onAddMember)
                }
            } else {
                // Stitch Large 2-Column Photo Cards
                items(state.members, key = { it.id }) { member ->
                    StitchPersonCard(
                        member = member,
                        onEdit = { onEditMember(member.id) },
                        onDelete = { onRequestDelete(member) }
                    )
                }

                // Add Member Button Row
                item(span = { GridItemSpan(2) }) {
                    Spacer(modifier = Modifier.height(8.dp))
                    RetroButton(
                        text = stringResource(R.string.family_add_member),
                        icon = Icons.Filled.PersonAdd,
                        onClick = onAddMember,
                        style = RetroButtonStyle.Primary,
                        minHeight = 56.dp
                    )
                }
            }
        }
    }

    pendingDelete?.let { member ->
        RetroConfirmDialog(
            title = "REMOVE ${member.name.uppercase()}?",
            message = "This will remove ${member.name} from your family memories and games.",
            confirmLabel = "REMOVE",
            onConfirm = onConfirmDelete,
            onCancel = onCancelDelete
        )
    }
}

/**
 * Stitch 2-Column Person Card: Large Photo, Prominent Name, Minimal Metadata
 */
@Composable
private fun StitchPersonCard(
    member: FamilyMember,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = MmTheme.colors
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(20.dp),
        color = colors.panel,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Large Photo
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                shape = RoundedCornerShape(16.dp),
                color = colors.panelInner
            ) {
                val photoUri = member.displayPhotoUri
                if (!photoUri.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(File(photoUri))
                            .crossfade(true)
                            .build(),
                        contentDescription = member.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = member.name.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 36.sp
                            ),
                            color = colors.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Name
            Text(
                text = member.name,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = colors.text,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Relationship
            if (member.relationship.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = member.relationship,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = colors.secondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EmptyFamilyState(onAddMember: () -> Unit) {
    val colors = MmTheme.colors

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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.family_empty_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = colors.primary,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.family_empty_desc),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textMuted,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            RetroButton(
                text = stringResource(R.string.family_add_member),
                icon = Icons.Filled.PersonAdd,
                onClick = onAddMember,
                style = RetroButtonStyle.Primary,
                minHeight = 56.dp
            )
        }
    }
}
