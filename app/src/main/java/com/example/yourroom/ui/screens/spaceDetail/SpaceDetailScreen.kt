package com.example.yourroom.ui.screens.spaceDetail

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.yourroom.model.PhotoResponse
import com.example.yourroom.model.SpaceResponse
import com.example.yourroom.viewmodel.SpaceDetailViewModel

@Composable
fun SpaceDetailScreen(
    navController: NavController,
    spaceId: Long,
    vm: SpaceDetailViewModel = hiltViewModel()
) {
    val state by vm.ui.collectAsState()

    LaunchedEffect(spaceId) {
        vm.load(spaceId)
    }

    when {
        state.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        state.errorMessage != null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.errorMessage ?: "Error",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        state.space != null -> {
            SpaceDetailContent(
                navController = navController,
                space = state.space!!,
                photos = state.photos,
                ownerEmail = state.ownerEmail
            )
        }
    }
}

@Composable
private fun SpaceDetailContent(
    navController: NavController,
    space: SpaceResponse,
    photos: List<PhotoResponse>,
    ownerEmail: String?
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Box {
                PhotosCarousel(
                    title = space.title,
                    photos = photos,
                    fallbackUrl = space.primaryPhotoUrl
                )

                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(12.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
            }
        }

        item {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = space.title ?: "Sala sin título",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = buildLocationText(space),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )

                Text(
                    text = formatPrice(space),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Divider()

                DetailSection(
                    title = "Descripción",
                    value = space.description.orEmpty().ifBlank { "Sin descripción disponible." }
                )

                DetailSection(
                    title = "Servicios",
                    value = space.services.orEmpty().ifBlank { "No especificados." }
                )

                DetailSection(
                    title = "Disponibilidad",
                    value = space.availability.orEmpty().ifBlank { "No especificada." }
                )

                InfoGrid(
                    capacity = space.capacity,
                    sizeM2 = space.sizeM2,
                    status = space.status
                )

                ContactCard(ownerEmail = ownerEmail)
            }
        }
    }
}

@Composable
private fun PhotosCarousel(
    title: String?,
    photos: List<PhotoResponse>,
    fallbackUrl: String?
) {
    val urls = when {
        photos.isNotEmpty() -> {
            val primary = photos.firstOrNull { it.primary }
            val rest = photos.filterNot { it.id == primary?.id }
            listOfNotNull(primary?.url) + rest.map { it.url }
        }
        !fallbackUrl.isNullOrBlank() -> listOf(fallbackUrl)
        else -> emptyList()
    }

    if (urls.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.25f)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text("Sin fotos disponibles")
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { urls.size })

    Box {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.25f)
        ) { page ->
            AsyncImage(
                model = urls[page],
                contentDescription = title ?: "Foto de la sala",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        if (urls.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(urls.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index) Color.White
                                else Color.White.copy(alpha = 0.45f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    value: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun InfoGrid(
    capacity: Int?,
    sizeM2: Int?,
    status: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        InfoChip(
            label = "Capacidad",
            value = capacity?.let { "$it personas" } ?: "No indicada",
            modifier = Modifier.weight(1f)
        )
        InfoChip(
            label = "Tamaño",
            value = sizeM2?.let { "$it m²" } ?: "No indicado",
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    InfoChip(
        label = "Estado",
        value = status,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun InfoChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ContactCard(ownerEmail: String?) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Email,
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Contacto del propietario",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = ownerEmail ?: "Email no disponible",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

private fun buildLocationText(space: SpaceResponse): String {
    return listOfNotNull(
        space.location?.takeIf { it.isNotBlank() },
        space.addressLine?.takeIf { it.isNotBlank() }
    ).joinToString(" · ").ifBlank { "Ubicación no disponible" }
}

private fun formatPrice(space: SpaceResponse): String {
    return space.hourlyPrice?.let { "$it €/hora" } ?: "Precio no disponible"
}

