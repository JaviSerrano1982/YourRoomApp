package com.example.yourroom.ui.screens.edit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.yourroom.ui.components.LocationAutocompleteField
import com.example.yourroom.ui.components.transparentTextFieldColors
import com.example.yourroom.viewmodel.EditRoomViewModel



@Composable
fun EditRoomScreen(
    navController: NavController,
    spaceId: Long,
    vm: EditRoomViewModel = hiltViewModel()
) {
    LaunchedEffect(spaceId) { vm.init(spaceId) }
    val ui by vm.ui.collectAsState()

    // === Cálculo local de "hay cambios sin guardar" (mismo criterio que isDirty del VM) ===
    val hasUnsavedChanges by remember(ui) {
        mutableStateOf(
            ui.space != null && (
                    ui.title != ui.space?.title.orEmpty() ||
                            ui.location != ui.space?.location.orEmpty() ||
                            ui.addressLine != ui.space?.addressLine.orEmpty() ||
                            ui.capacity != ui.space?.capacity?.toString().orEmpty() ||
                            ui.hourlyPrice != ui.space?.hourlyPrice?.toPlainString().orEmpty() ||
                            ui.sizeM2 != ui.space?.sizeM2?.toString().orEmpty() ||
                            ui.availability != ui.space?.availability.orEmpty() ||
                            ui.services != ui.space?.services.orEmpty() ||
                            ui.description != ui.space?.description.orEmpty()
                    )
        )
    }

    var showExitConfirm by remember { mutableStateOf(false) }

    // Interceptar el botón físico/gesto de atrás
    BackHandler(enabled = true) {
        if (hasUnsavedChanges) {
            showExitConfirm = true
        } else {
            navController.popBackStack()
        }
    }

    if (ui.loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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

    val topBarHeight = 60.dp

    Box(Modifier.fillMaxSize()) {

        // === Top Bar con el mismo estilo que MyRooms ===
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
        ) {
            IconButton(
                onClick = {
                    if (hasUnsavedChanges) {
                        showExitConfirm = true
                    } else {
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Atrás",
                    tint = Color.White
                )
            }
            Text(
                text = "Editar sala",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // === Contenido desplazado bajo la top bar ===
        Box(
            Modifier
                .fillMaxSize()
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(top = topBarHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextField(
                    value = ui.title,
                    onValueChange = { vm.onChange(title = it) },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = transparentTextFieldColors(),
                    trailingIcon = {
                        if (ui.title.isNotEmpty()) {
                            IconButton(
                                onClick = { vm.onChange(title = "") },
                                modifier = Modifier.size(20.dp)
                            )
                            {
                                Icon(Icons.Default.Close, contentDescription = "Borrar")
                            }
                        }
                    }
                )

                LocationAutocompleteField(
                    value = ui.location,
                    onValueChange = { vm.onChange(location = it) },
                    onSuggestionPicked = { vm.onChange(location = it) },
                    label = "Localidad / Ciudad",
                    colors = transparentTextFieldColors()
                )

                TextField(
                    value = ui.addressLine,
                    onValueChange = { vm.onChange(addressLine = it) },
                    label = { Text("Dirección") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = transparentTextFieldColors(),
                    trailingIcon = {
                        if (ui.addressLine.isNotEmpty()) {
                            IconButton(
                                onClick = { vm.onChange(addressLine = "") },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Borrar")
                            }
                        }
                    }
                )

                TextField(
                    value = ui.capacity,
                    onValueChange = { vm.onChange(capacity = it.filter { ch -> ch.isDigit() }) },
                    label = { Text("Capacidad") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = transparentTextFieldColors(),
                    trailingIcon = {
                        if (ui.capacity.isNotEmpty()) {
                            IconButton(
                                onClick = { vm.onChange(capacity = "") },
                                modifier = Modifier.size(20.dp)

                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Borrar")
                            }
                        }
                    }
                )

                TextField(
                    value = ui.hourlyPrice,
                    onValueChange = {
                        vm.onChange(
                            hourlyPrice = it
                                .filter { ch -> ch.isDigit() || ch == '.' || ch == ',' }
                                .replace(',', '.')
                        )
                    },
                    label = { Text("Precio €/hora") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = transparentTextFieldColors(),
                    trailingIcon = {
                        if (ui.hourlyPrice.isNotEmpty()) {
                            IconButton(
                                onClick = { vm.onChange(hourlyPrice = "") },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Borrar")
                            }
                        }
                    }
                )

                TextField(
                    value = ui.sizeM2,
                    onValueChange = { vm.onChange(sizeM2 = it.filter { ch -> ch.isDigit() }) },
                    label = { Text("Tamaño (m²)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = transparentTextFieldColors(),
                    trailingIcon = {
                        if (ui.sizeM2.isNotEmpty()) {
                            IconButton(
                                onClick = { vm.onChange(sizeM2 = "") },
                                modifier = Modifier.size(20.dp)

                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Borrar")
                            }
                        }
                    }
                )

                TextField(
                    value = ui.availability,
                    onValueChange = { vm.onChange(availability = it) },
                    label = { Text("Disponibilidad") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = transparentTextFieldColors(),
                    trailingIcon = {
                        if (ui.availability.isNotEmpty()) {
                            IconButton(
                                onClick = { vm.onChange(availability = "") },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Borrar")
                            }
                        }
                    }
                )

                TextField(
                    value = ui.services,
                    onValueChange = { vm.onChange(services = it) },
                    label = { Text("Servicios") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = transparentTextFieldColors(),
                    trailingIcon = {
                        if (ui.services.isNotEmpty()) {
                            IconButton(
                                onClick = { vm.onChange(services = "") },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Borrar")
                            }
                        }
                    }
                )

                TextField(
                    value = ui.description,
                    onValueChange = { vm.onChange(description = it) },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    colors = transparentTextFieldColors(),
                    trailingIcon = {
                        if (ui.description.isNotEmpty()) {
                            IconButton(
                                onClick = { vm.onChange(description = "") },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Borrar")
                            }
                        }
                    }
                )
            }

                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        vm.save {
                            // Marca para refrescar MyRooms y vuelve
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("needsRefreshMyRooms", true)
                            navController.popBackStack()
                        }
                    },
                    enabled = ui.isSaveEnabled && !ui.saving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .navigationBarsPadding(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor =  Color(0xFFA5C6E2) ,
                        contentColor = Color.White
                    )
                ) {
                    if (ui.saving) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (ui.saving) "Guardando..." else "Guardar cambios")
                }
            }
        }

        // === Diálogo de confirmación al salir con cambios ===
        if (showExitConfirm) {
            AlertDialog(
                onDismissRequest = { showExitConfirm = false },
                title = { Text("Salir sin guardar") },
                text = { Text("Tienes cambios sin guardar. Si vuelves atrás, se perderán. ¿Quieres salir igualmente?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showExitConfirm = false
                            navController.popBackStack()
                        }
                    ) { Text("Salir") }
                },
                dismissButton = {
                    TextButton(onClick = { showExitConfirm = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }

