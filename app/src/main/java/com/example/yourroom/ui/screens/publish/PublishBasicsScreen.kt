package com.example.yourroom.ui.screens.publish



import androidx.compose.ui.text.font.FontStyle
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import coil.compose.AsyncImage
import com.example.yourroom.R
import com.example.yourroom.ui.components.LocationAutocompleteField
import com.example.yourroom.ui.components.transparentTextFieldColors
import com.example.yourroom.viewmodel.PublishSpaceViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishBasicsScreen(
    navController: NavController,
    vm: PublishSpaceViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()
    var showCancelDialog by remember { mutableStateOf(false) }

    // Photo picker (guardamos la Uri en el VM)
    val pickPhoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) vm.onMainPhotoSelected(uri)
    }


    // Validación mínima para habilitar "Siguiente"
    val isNextEnabled = remember(ui) {
        ui.title.isNotBlank() &&
                ui.location.isNotBlank() &&
                ui.address.isNotBlank() &&
                (ui.capacity.toIntOrNull()?.let { it > 0 } == true) &&
                (ui.price.replace(',', '.').toDoubleOrNull()?.let { it > 0.0 } == true) &&
                ui.photoUri != null
    }

    val addressIS = remember { MutableInteractionSource() }
    val addressFocused by addressIS.collectIsFocusedAsState()
    val capacityIS = remember { MutableInteractionSource() }
    val capacityFocused by capacityIS.collectIsFocusedAsState()
    val priceIS = remember { MutableInteractionSource() }
    val priceFocused by priceIS.collectIsFocusedAsState()
    val titleIS = remember { MutableInteractionSource() }
    val titleFocused by titleIS.collectIsFocusedAsState()


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "¡Publica tu sala!",
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
                    .height(70.dp)
                    .padding(16.dp),

                horizontalArrangement = Arrangement.End
            ) {


                Button(
                    onClick = {
                        vm.submitBasics { id ->
                            // Si guarda OK, navega a Detalles con el id resultante
                            navController.navigate(PublishRoutes.details(id))
                        }
                    },
                    enabled = isNextEnabled && !ui.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isNextEnabled && !ui.isLoading) Color(0xFFA5C6E2) else Color.Gray,
                        contentColor = Color.White
                    )

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
                .padding(padding) // respeta el espacio del TopBar y BottomBar
        ) {
            // Zona del icono (arriba, centrado)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icono_basicos),
                    contentDescription = "Ilustración de publicación",
                    modifier = Modifier.size(120.dp), // 56–64dp funciona muy bien
                    contentScale = ContentScale.Fit


                )
            }



            // Formulario ocupa el resto de la pantalla; si falta espacio, hace scroll
            Column(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),

            ) {
                ui.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                // Título
                TextField(
                    value = ui.title,
                    onValueChange = vm::onTitleChange,
                    label = {
                        if (!titleFocused && ui.title.isEmpty()) {
                            Column {
                                Text(
                                    "Título de la sala",
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Ej.: Sala polivalente con espejos",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        } else {
                            Text("Título de la sala")
                        }
                    },
                    interactionSource = titleIS,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    colors = transparentTextFieldColors(),
                    trailingIcon = {
                        if (ui.title.isNotEmpty()) {
                            IconButton(
                                onClick = { vm.onTitleChange("") },
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

                // Ubicación
                LocationAutocompleteField(
                    value = ui.location,
                    label = "Ubicación",
                    example = "Ciudad, población, etc",
                    onValueChange = { vm.onLocationTyping(it) },
                    onSuggestionPicked = { picked -> vm.onLocationPicked(picked) },
                    isSaving = ui.isLoading,
                    isError = ui.error?.contains("ubicación", ignoreCase = true) == true && ui.location.isBlank(),
                    errorMessage = if (ui.location.isBlank()) "Campo obligatorio" else null,
                    colors = transparentTextFieldColors(),
                    enabled = true                )

                // Dirección
                TextField(
                    value = ui.address,
                    onValueChange = vm::onAddressChange,
                    label = {
                        if (!addressFocused && ui.address.isEmpty()) {
                            Column {
                                Text(
                                    "Dirección",
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Ej.: Calle Mayor 123, Madrid",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        } else {
                            Text("Dirección")
                        }
                    },
                    interactionSource = addressIS,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = transparentTextFieldColors(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    trailingIcon = {
                        if (ui.address.isNotEmpty()) {
                            IconButton(
                                onClick = { vm.onAddressChange("") },
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

                // Capacidad
                TextField(
                    value = ui.capacity,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.all { it.isDigit() }) {
                            vm.onCapacityChange(input)
                        }
                    },
                    label = {
                        if (!capacityFocused && ui.capacity.isEmpty()) {
                            Column {
                                Text(
                                    "Capacidad",
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Nº de personas",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        } else {
                            Text("Capacidad ")
                        }
                    },
                    interactionSource = capacityIS,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = transparentTextFieldColors(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    trailingIcon = {
                        if (ui.capacity.isNotEmpty()) {
                            IconButton(
                                onClick = { vm.onCapacityChange("") },
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

                // Precio


                TextField(
                    value = ui.price,
                    onValueChange = { input ->
                        val sanitized = input.replace(',', '.')
                        if (sanitized.isEmpty() || sanitized.matches(Regex("""\d*([.]\d{0,2})?"""))) {
                            vm.onPriceChange(sanitized)
                        }
                    },
                    label = {
                        if (!priceFocused && ui.price.isEmpty()) {
                            Column {
                                Text(
                                    "Precio alquiler",
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "€ / hora, dia, mes, etc",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        } else {
                            Text("Precio alquiler")
                        }
                    },
                    interactionSource = priceIS,
                    singleLine = true,
                    prefix = { Text("€ ") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = transparentTextFieldColors(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    trailingIcon = {
                        if (ui.price.isNotEmpty()) {
                            IconButton(
                                onClick = { vm.onPriceChange("") },
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
                Spacer(Modifier.height(18.dp))


                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp), // padding interno de la Card
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // IZQUIERDA: texto + botón
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Foto principal", style = MaterialTheme.typography.titleMedium)
                            Text(
                                if (ui.photoUri != null) "1 foto seleccionada" else "Ninguna foto seleccionada",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            OutlinedButton(
                                onClick = {
                                    vm.onAddMainPhotoClicked {
                                        pickPhoto.launch("image/*")
                                    }
                                }
                            ) {
                                Text(if (ui.photoUri != null) "Cambiar foto" else "Seleccionar foto")
                            }

                        }

                        Spacer(modifier = Modifier.width(12.dp)) // separación entre texto y foto

                        // DERECHA: imagen en miniatura con padding y bordes redondeados
                        if (ui.photoUri != null) {
                            AsyncImage(
                                model = ui.photoUri,
                                contentDescription = "Foto seleccionada",
                                modifier = Modifier
                                    .size(90.dp) // tamaño fijo
                                    .clip(RoundedCornerShape(12.dp)) // respeta esquinas redondeadas
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }




            }
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
                        // Limpia toda la pila y navega a Home (misma lógica que Details)
                        navController.navigate(PublishRoutes.home()) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                            launchSingleTop = true
                            restoreState = false
                        }
                    }
                }) { Text("Cancelar") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Seguir editando")
                }
            }
        )
    }
}
