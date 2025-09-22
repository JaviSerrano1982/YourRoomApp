package com.example.yourroom.ui.theme.screens.publish

import android.R.attr.contentDescription
import android.R.attr.maxLines
import android.R.attr.minLines
import android.net.http.SslCertificate.restoreState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.yourroom.viewmodel.PublishDetailsViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yourroom.R
import com.example.yourroom.ui.theme.components.transparentTextFieldColors


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishDetailsScreen(
    navController: NavController,
    spaceId: Long,
    vm: PublishDetailsViewModel = hiltViewModel()
) {
    val ui = vm.uiState
    val scope = rememberCoroutineScope()
    var showCancelDialog by remember { mutableStateOf(false) }
    val isNextEnabled = remember(ui) {
        ui.sizeM2Text.toIntOrNull()?.let { it > 0 } == true &&
                ui.availability.isNotBlank() &&
                ui.services.isNotBlank() &&
                ui.description.isNotBlank()
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Detalles",
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
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
                        scope.launch {
                            val ok = vm.saveDetailsAwait()
                            if (ok) {
                                navController.navigate(PublishRoutes.photos(spaceId))
                            }
                        }
                    },
                    enabled = isNextEnabled && !ui.isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isNextEnabled && !ui.isSaving) Color(0xFF4CAF50) else Color.Gray,
                        contentColor = Color.White
                    )
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icono_detalles),
                    contentDescription = "Ilustración de detalles",
                    modifier = Modifier.size(130.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(Modifier.height(8.dp))


            // Mensaje de error
            ui.error?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            TextField(
                value = ui.sizeM2Text,
                onValueChange = vm::onSize,
                label = { Text("Superficie (m²)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !ui.isSaving,
                modifier = Modifier.fillMaxWidth(),
                colors = transparentTextFieldColors(),
                trailingIcon = {
                    if (ui.sizeM2Text.isNotEmpty()) {
                        IconButton(
                            onClick = { vm.onSize("") },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Borrar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )

            TextField(
                value = ui.availability,
                onValueChange = vm::onAvailability,
                label = { Text("Disponibilidad (ej.: L–V 8:00–22:00)") },
                minLines = 1,
                maxLines = 3,
                enabled = !ui.isSaving,
                modifier = Modifier.fillMaxWidth(),
                colors = transparentTextFieldColors(),
                trailingIcon = {
                    if (ui.availability.isNotEmpty()) {
                        IconButton(
                            onClick = { vm.onAvailability("") },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Borrar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )

            TextField(
                value = ui.services,
                onValueChange = vm::onServices,
                label = { Text("Servicios (ej.: duchas, vestuarios, wifi)") },
                minLines = 1,
                maxLines = 5,
                enabled = !ui.isSaving,
                modifier = Modifier.fillMaxWidth(),
                colors = transparentTextFieldColors(),
                trailingIcon = {
                    if (ui.services.isNotEmpty()) {
                        IconButton(
                            onClick = { vm.onServices("") },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Borrar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = ui.description,
                onValueChange = vm::onDescription,
                label = { Text("Descripción") },
                minLines = 4,
                maxLines = 8,
                enabled = !ui.isSaving,
                modifier = Modifier.fillMaxWidth(),
                colors = transparentTextFieldColors(),
                trailingIcon = {
                    if (ui.description.isNotEmpty()) {
                        IconButton(
                            onClick = { vm.onDescription("") },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Borrar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
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
                    vm.cancelAndDelete {
                        // Limpia toda la pila y navega a Home
                        navController.navigate(PublishRoutes.home()) {
                            // Sube hasta el startDestination y lo borra borra también
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                            launchSingleTop = true
                            restoreState = false
                        }
                    }
                }) { Text("Sí, borrar") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Seguir editando")
                }
            }
        )
    }

}
