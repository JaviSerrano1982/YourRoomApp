package com.example.yourroom.ui.screens.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.yourroom.ui.components.LocationAutocompleteField
import com.example.yourroom.viewmodel.EditRoomViewModel

@Composable
fun EditRoomScreen(
    navController: NavController,
    spaceId: Long,
    vm: EditRoomViewModel = hiltViewModel()
) {
    LaunchedEffect(spaceId) { vm.init(spaceId) }
    val ui by vm.ui.collectAsState()

    if (ui.loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    ui.error?.let { err ->
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text("Error: $err", color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
            Button(onClick = { vm.init(spaceId) }) { Text("Reintentar") }
        }
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = ui.title,
            onValueChange = { vm.onChange(title = it) },
            label = { Text("Título") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        LocationAutocompleteField(
            value = ui.location,
            onValueChange = { vm.onChange(location = it) },
            onSuggestionPicked = { vm.onChange(location = it) },
            label = "Localidad / Ciudad"
        )

        OutlinedTextField(
            value = ui.addressLine,
            onValueChange = { vm.onChange(addressLine = it) },
            label = { Text("Dirección") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = ui.capacity,
            onValueChange = { vm.onChange(capacity = it.filter { ch -> ch.isDigit() }) },
            label = { Text("Capacidad") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = ui.hourlyPrice,
            onValueChange = { vm.onChange(hourlyPrice = it.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' }.replace(',', '.')) },
            label = { Text("Precio €/hora") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = ui.sizeM2,
            onValueChange = { vm.onChange(sizeM2 = it.filter { ch -> ch.isDigit() }) },
            label = { Text("Tamaño (m²)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = ui.availability,
            onValueChange = { vm.onChange(availability = it) },
            label = { Text("Disponibilidad (texto)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        OutlinedTextField(
            value = ui.services,
            onValueChange = { vm.onChange(services = it) },
            label = { Text("Servicios (lista/comas)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        OutlinedTextField(
            value = ui.description,
            onValueChange = { vm.onChange(description = it) },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4
        )

        // Si quieres incluir fotos aquí: añade una fila con preview y un botón "Editar fotos"
        // Button(onClick = { navController.navigate(PublishRoutes.photos(spaceId)) }) { Text("Editar fotos") }

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                vm.save {
                    // Marca para que la pantalla anterior se refresque
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("needsRefreshMyRooms", true)

                    navController.popBackStack()
                }
            },
            enabled = ui.isSaveEnabled && !ui.saving,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (ui.saving) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text(if (ui.saving) "Guardando..." else "Guardar cambios")
        }
    }
}


