package com.example.yourroom.ui.screens.myRooms

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.yourroom.ui.screens.edit.EditRoomRoutes

import com.example.yourroom.viewmodel.MyRoomsViewModel


// ---------------------------------------------------------------------
// PANTALLA "MIS SALAS"
// ---------------------------------------------------------------------

/**
 * Pantalla que muestra las salas creadas por el usuario:
 *
 * Flujo principal:
 * 1) Recoge el estado expuesto por [MyRoomsViewModel].
 * 2) Observa un flag en el back stack para refrescar la lista al volver de edición.
 * 3) Renderiza uno de los 4 estados: cargando, error, vacío o lista.
 * 4) Cada tarjeta permite editar o borrar la sala.
 *
 * El borrado usa un diálogo de confirmación local y luego llama al ViewModel.
 */
@Composable
fun MyRoomsScreen(
    navController: NavController,
    vm: MyRoomsViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()

    // Observa un flag enviado desde EditRoomScreen para refrescar la lista
    val needsRefreshFlow = remember(navController) {
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.getStateFlow("needsRefreshMyRooms", false)
    }
    val needsRefresh by needsRefreshFlow?.collectAsState() ?: remember { mutableStateOf(false) }

    // Trigger de recarga cuando se solicita desde otra pantalla
    LaunchedEffect(needsRefresh) {
        if (needsRefresh) {
            vm.loadMyRooms()
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("needsRefreshMyRooms", false)
        }
    }

    val topBarHeight = 60.dp

    Box(Modifier.fillMaxSize()) {

        // TopBar personalizada
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(topBarHeight)
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFF7F00FF), Color(0xFF00BFFF))
                    )
                )
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .align(Alignment.TopStart)
                .zIndex(2f)
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Atrás",
                    tint = Color.White
                )
            }
            Text(
                text = "Mis salas",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }



        // CUERPO PRINCIPAL (según estado del ViewModel)
        when {
            ui.isLoading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(top = topBarHeight)
                    , contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            ui.error != null -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(top = topBarHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = ui.error ?: "Ha ocurrido un error")
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { vm.loadMyRooms() }) { Text("Reintentar") }
                    }
                }
            }
            ui.items.isEmpty() -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(top = topBarHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Todavía no has publicado ninguna sala.")
                }
            }

            else -> {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = topBarHeight) // evita solape con la barra
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    items(ui.items) { item ->
                        MyRoomCard(
                            title = item.space.title ?: "(Sin título)",
                            location = item.space.location ?: "",
                            price = item.space.hourlyPrice?.toPlainString(),
                            capacity = item.space.capacity,
                            photoUrl = item.primaryPhotoUrl,
                            onEditClick = { navController.navigate(EditRoomRoutes.edit(item.space.id)) },
                            onDeleteConfirmed = { vm.deleteRoom(item.space.id) }
                        )
                    }

                }
            }
        }
    }
}


// ---------------------------------------------------------------------
// TARJETA DE SALA (UI CARD)
// ---------------------------------------------------------------------

/**
 * Tarjeta que renderiza la información de una sala:
 *
 * - Imagen principal
 * - Título, ubicación, precio, capacidad
 * - Botones flotantes (editar / borrar)
 *
 * Contiene un estado local para el diálogo de confirmación de borrado.
 */
@SuppressLint("DefaultLocale")
@Composable
private fun MyRoomCard(
    title: String,
    location: String,
    price: String?,
    capacity: Int?,
    photoUrl: String?,
    onEditClick: () -> Unit,
    onDeleteConfirmed: () -> Unit
) {
    // Formateo seguro del precio
    val formattedPrice = remember(price) {
        price?.toDoubleOrNull()?.let { value ->
            if (value % 1.0 == 0.0) value.toInt().toString() else String.format("%.2f", value)
        }
    }

    // Control del diálogo de confirmación
    var showConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        // Contenido principal: imagen + info
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen IZQUIERDA
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Foto principal",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.width(12.dp))

            // Info DERECHA
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 40.dp) // deja espacio para la columna de iconos
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))

                if (location.isNotBlank()) {
                    Text(
                        text = location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                }

                formattedPrice?.let {
                    Text(
                        text = "$it €/h",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                }

                capacity?.let {
                    Text(
                        text = "$it personas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Columna de botones (Editar arriba, Borrar abajo) en esquina superior derecha
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
            horizontalAlignment = Alignment.End
        ) {
            // EDITAR
            Box(
                modifier = Modifier
                    .size(36.dp) // 🔹 tamaño del círculo
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { onEditClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Editar sala",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp) // 🔹 tamaño del icono
                )
            }

            // BORRAR
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error)
                    .clickable { showConfirm = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Eliminar sala",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    //Diálogo de confirmación
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Eliminar sala") },
            text = { Text("¿Estás seguro de que quieres eliminar esta sala? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirm = false
                        onDeleteConfirmed()
                    }
                ) { Text("Sí, borrar") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("Cancelar") }
            }
        )
    }
}
