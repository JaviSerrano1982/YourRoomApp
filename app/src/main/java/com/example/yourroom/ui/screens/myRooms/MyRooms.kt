package com.example.yourroom.ui.screens.myRooms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.yourroom.ui.screens.publish.PublishRoutes
import com.example.yourroom.viewmodel.MyRoomsViewModel

@Composable
fun MyRoomsScreen(
    navController: NavController,
    vm: MyRoomsViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()

    when {
        ui.isLoading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        ui.error != null -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = ui.error ?: "Ha ocurrido un error")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { vm.loadMyRooms() }) { Text("Reintentar") }
                }
            }
        }
        ui.items.isEmpty() -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Todavía no has publicado ninguna sala.")
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ui.items) { item ->
                    MyRoomCard(
                        title = item.space.title ?: "(Sin título)",
                        location = item.space.location ?: "",
                        price = item.space.hourlyPrice?.toPlainString(),
                        capacity = item.space.capacity,
                        photoUrl = item.primaryPhotoUrl,
                        onClick = {
                            // Por ahora, al tocar vamos a la pantalla de edición de DETALLES
                            navController.navigate(PublishRoutes.details(item.space.id))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MyRoomCard(
    title: String,
    location: String,
    price: String?,
    capacity: Int?,
    photoUrl: String?,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Column {
            // Imagen principal
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Foto principal",
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Información básica
            Column(Modifier.padding(12.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                val chips = buildList {
                    if (location.isNotBlank()) add(location)
                    price?.let { add("${it} €/h") }
                    capacity?.let { add("$it personas") }
                }
                Text(
                    text = chips.joinToString(" • "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Pulsa para editar",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
