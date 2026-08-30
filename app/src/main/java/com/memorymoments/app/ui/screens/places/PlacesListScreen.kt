package com.memorymoments.app.ui.screens.places

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.memorymoments.app.model.Place
import com.memorymoments.app.ui.components.ArcadeScreen
import com.memorymoments.app.ui.components.ArcadeTopBar
import com.memorymoments.app.ui.components.RetroButton
import com.memorymoments.app.ui.components.RetroButtonStyle
import com.memorymoments.app.ui.components.RetroPanel
import com.memorymoments.app.ui.theme.MmTheme
import java.io.File

@Composable
fun PlacesListScreen(
    onBack: () -> Unit,
    onAddPlace: () -> Unit,
    onEditPlace: (String) -> Unit,
    viewModel: PlacesListViewModel = viewModel()
) {
    val places by viewModel.places.collectAsStateWithLifecycle()
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens

    ArcadeScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimens.screenHorizontal, vertical = dimens.screenVertical)
        ) {
            ArcadeTopBar(
                title = "My Places",
                onBack = onBack
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MY PLACES",
                    style = if (MmTheme.isEasyMode) MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold) else MmTheme.arcade.hud,
                    color = colors.reward
                )
                Text(
                    text = "${places.size} SAVED",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.secondary
                )
            }

            Spacer(modifier = Modifier.height(dimens.md))

            if (places.isEmpty()) {
                EmptyPlacesPanel(
                    onAddPlace = onAddPlace,
                    onLoadDemo = viewModel::loadDemoPlaces
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(dimens.md)
                ) {
                    items(places, key = { it.id }) { place ->
                        PlaceCardItem(
                            place = place,
                            onClick = { onEditPlace(place.id) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(dimens.lg))
                        RetroButton(
                            text = "+ ADD PLACE",
                            icon = Icons.Filled.Add,
                            onClick = onAddPlace,
                            style = RetroButtonStyle.Primary,
                            minHeight = dimens.playButtonMin
                        )
                        Spacer(modifier = Modifier.height(dimens.xl))
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceCardItem(
    place: Place,
    onClick: () -> Unit
) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = colors.panel,
        border = BorderStroke(2.dp, colors.border)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            val photoUri = place.displayPhotoUri
            if (!photoUri.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(File(photoUri))
                            .crossfade(true)
                            .build(),
                        contentDescription = "Photo of ${place.name}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimens.md)
            ) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = if (MmTheme.isEasyMode) 22.sp else 19.sp
                    ),
                    color = colors.reward
                )

                if (!place.location.isNullOrBlank() || !place.datePeriod.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(dimens.xs))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = colors.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = buildString {
                                if (!place.location.isNullOrBlank()) append(place.location)
                                if (!place.location.isNullOrBlank() && !place.datePeriod.isNullOrBlank()) append(" • ")
                                if (!place.datePeriod.isNullOrBlank()) append(place.datePeriod)
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.secondary
                        )
                    }
                }

                if (!place.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(dimens.xs))
                    Text(
                        text = place.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyPlacesPanel(
    onAddPlace: () -> Unit,
    onLoadDemo: () -> Unit
) {
    val colors = MmTheme.colors
    val dimens = MmTheme.dimens

    RetroPanel(borderColor = colors.primary.copy(alpha = 0.6f)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = dimens.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimens.md)
        ) {
            Icon(
                imageVector = Icons.Filled.Place,
                contentDescription = null,
                tint = colors.reward,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "NO PLACES YET",
                style = if (MmTheme.isEasyMode) MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold) else MmTheme.arcade.hud,
                color = colors.reward,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Add a meaningful place.",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = dimens.md)
            )
            Spacer(modifier = Modifier.height(dimens.sm))
            RetroButton(
                text = "+ ADD PLACE",
                icon = Icons.Filled.Add,
                onClick = onAddPlace,
                style = RetroButtonStyle.Primary,
                minHeight = dimens.playButtonMin
            )
            RetroButton(
                text = "LOAD DEMO PLACES",
                icon = Icons.Filled.AutoAwesome,
                onClick = onLoadDemo,
                style = RetroButtonStyle.Ghost
            )
        }
    }
}
