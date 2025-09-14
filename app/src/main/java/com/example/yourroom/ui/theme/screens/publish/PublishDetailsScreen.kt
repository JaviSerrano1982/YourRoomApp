package com.example.yourroom.ui.theme.screens.publish

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.yourroom.viewmodel.PublishDetailsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishDetailsScreen(
    navController: NavController,
    spaceId: Long,
    viewModel: PublishDetailsViewModel = hiltViewModel() // <- tu VM inyectado con Hilt
) {
    val ui = viewModel.uiState
    val scope = rememberCoroutineScope()
    var showCancelDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles") },
                actions = {
                    IconButton(onClick = { showCancelDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancelar y borrar borrador"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    enabled = !ui.isSaving
                ) {
                    Text("Atrás")
                }
                Button(
                    onClick = {
                        // Guardar detalles y avanzar SOLO si se guarda OK
                        scope.launch {
                            val ok = viewModel.saveDetailsAwait()
                            if (ok) {
                                navController.navigate(PublishRoutes.photos(spaceId))
                            }
                            // Si no ok, ui.error ya queda seteado y NO navega
                        }
                    },
                    enabled = !ui.isSaving
                ) {
                    if (ui.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Siguiente")
                    }
                }

            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mensaje de error si lo hubiera
            ui.error?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // sizeM2
            OutlinedTextField(
                value = ui.sizeM2Text,
                onValueChange = viewModel::onSize,
                label = { Text("Superficie (m²)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !ui.isSaving,
                modifier = Modifier.fillMaxWidth()
            )

            // availability
            OutlinedTextField(
                value = ui.availability,
                onValueChange = viewModel::onAvailability,
                label = { Text("Disponibilidad (ej.: L–V 8:00–22:00)") },
                minLines = 1,
                maxLines = 3,
                enabled = !ui.isSaving,
                modifier = Modifier.fillMaxWidth()
            )

            // services
            OutlinedTextField(
                value = ui.services,
                onValueChange = viewModel::onServices,
                label = { Text("Servicios (ej.: duchas, vestuarios, wifi)") },
                minLines = 2,
                maxLines = 5,
                enabled = !ui.isSaving,
                modifier = Modifier.fillMaxWidth()
            )

            // description
            OutlinedTextField(
                value = ui.description,
                onValueChange = viewModel::onDescription,
                label = { Text("Descripción") },
                minLines = 4,
                maxLines = 8,
                enabled = !ui.isSaving,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancelar publicación") },
            text = { Text("Se eliminará el borrador de esta sala. ¿Seguro que quieres cancelar y borrar?") },
            confirmButton = {
                TextButton(onClick = {
                    showCancelDialog = false
                    // Borrado en backend y salida del flujo
                    viewModel.cancelAndDelete {
                        // Después de borrar, sal del flujo. Aquí usamos un back simple.
                        // Si prefieres ir a Home con popUpTo, cámbialo por tu ruta:
                        // navController.navigate(PublishRoutes.home()) {
                        //     popUpTo(PublishRoutes.home()) { inclusive = true }
                        // }
                        navController.popBackStack()
                    }
                }) {
                    Text("Sí, borrar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Seguir editando")
                }
            }
        )
    }
}
