package com.example.yourroom.ui.theme.screens.publish

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.yourroom.viewmodel.PublishSpaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishBasicsScreen(
    navController: NavController,
    vm: PublishSpaceViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()

    // Photo picker (igual que el tuyo, pero guardando en el VM)
    val pickPhoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> vm.setPhotoUri(uri) }

    // Misma validación que ya hacías tú
    val isNextEnabled = remember(ui) {
        ui.title.isNotBlank() &&
                ui.location.isNotBlank() &&
                ui.address.isNotBlank() &&
                (ui.capacity.toIntOrNull()?.let { it > 0 } == true) &&
                (ui.price.replace(',', '.').toDoubleOrNull()?.let { it > 0.0 } == true) &&
                ui.photoUri != null
    }

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = { navController.popBackStack() }) { Text("Atrás") }
                Button(
                    onClick = {
                        vm.submitBasics { id ->
                            // Navegamos pasando el id creado/actualizado
                            navController.navigate(PublishRoutes.details(id))
                        }
                    },
                    enabled = isNextEnabled && !ui.isLoading
                ) {
                    if (ui.isLoading) {
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (ui.error != null) {
                Text(ui.error!!, color = MaterialTheme.colorScheme.error)
            }

            // Título
            OutlinedTextField(
                value = ui.title,
                onValueChange = vm::onTitleChange,
                label = { Text("Título de la sala") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            // Ubicación
            OutlinedTextField(
                value = ui.location,
                onValueChange = vm::onLocationChange,
                label = { Text("Ubicación (ciudad o zona)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            // Dirección
            OutlinedTextField(
                value = ui.address,
                onValueChange = vm::onAddressChange,
                label = { Text("Dirección") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            // Capacidad
            OutlinedTextField(
                value = ui.capacity,
                onValueChange = { input ->
                    if (input.isEmpty() || input.all { it.isDigit() }) vm.onCapacityChange(input)
                },
                label = { Text("Capacidad (personas)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )

            // Precio
            OutlinedTextField(
                value = ui.price,
                onValueChange = { input ->
                    val sanitized = input.replace(',', '.')
                    if (sanitized.isEmpty() || sanitized.matches(Regex("""\d*([.]\d{0,2})?"""))) {
                        vm.onPriceChange(sanitized)
                    }
                },
                label = { Text("Precio alquiler (€ / hora)") },
                singleLine = true,
                prefix = { Text("€ ") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                )
            )

            // Foto (sin subir aún; solo guardamos la Uri para el flujo)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Foto principal", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (ui.photoUri != null) "1 foto seleccionada" else "Ninguna foto seleccionada",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedButton(
                        onClick = {
                            pickPhoto.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Text(if (ui.photoUri != null) "Cambiar foto" else "Seleccionar foto")
                    }
                }
            }
        }
    }
}
