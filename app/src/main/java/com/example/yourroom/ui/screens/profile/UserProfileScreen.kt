package com.example.yourroom.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.OutlinedButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.yourroom.R
import com.example.yourroom.viewmodel.UserProfileViewModel
import androidx.compose.ui.zIndex
import com.example.yourroom.model.UserProfileDto
import com.example.yourroom.viewmodel.FieldErrors
import kotlinx.coroutines.launch
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.TextField
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yourroom.ui.components.LocationAutocompleteField
import com.example.yourroom.ui.components.transparentTextFieldColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Pantalla de edición de perfil de usuario.
 *
 * Responsabilidades:
 * - Mostrar información del perfil y permitir su edición campo a campo.
 * - Gestionar cambios no guardados (hasChanges) y confirmación de salida.
 * - Lanzar el selector de imágenes y mostrar progreso al subir foto.
 * - Validar campos mostrando errores por campo y mensajes globales.
 *
 * Estados clave leídos del ViewModel:
 *  - profile, isSaving, hasChanges, errorMessage, emailErrorMessage,
 *    fieldErrors, isImageChanged, isUploadingPhoto y saveSuccess.
 *
 * Navegación:
 *  - Al guardar con éxito, ofrece acción para ir a "home".
 *  - Confirma la salida si hay cambios sin guardar.
 */
@Composable
fun UserProfileScreen(
    navController: NavHostController,
    viewModel: UserProfileViewModel
) {
    // --- StateFlows del VieModel a estados Compose ---
    val profile by viewModel.profile.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val hasChanges by viewModel.hasChanges.collectAsState()
    val userId by viewModel.userId.collectAsState()
    val localImageUri by viewModel.localImageUri.collectAsState()
    val isImageChanged by viewModel.isImageChanged.collectAsState()
    val fieldErrors by viewModel.fieldErrors.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val emailErrorMessage by viewModel.emailErrorMessage.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val isUploadingPhoto by viewModel.isUploadingPhoto.collectAsState()

    // --- UI helpers ---
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showLeaveDialog by remember { mutableStateOf(false) }
    val isEditingLocation = rememberSaveable { mutableStateOf(false) }
    val isEditingBirthDate = rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    // Lanzador del picker de imágenes (galería)
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // Subida directa a Firebase + actualización de estado
        viewModel.onPickLocalImage(uri)
    }

    // Feedback al guardar con éxito (snackbar)
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            coroutineScope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "Datos guardados correctamente",
                    actionLabel = "Inicio",
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            }
            viewModel.clearSaveSuccess()
        }
    }

    // Carga inicial de perfil desde DataStore + backend
    LaunchedEffect(Unit) {
        viewModel.initProfile(context)
    }

    // Intercepta el botón "atrás" para confirmar salida si hay cambios
    BackHandler {
        if (hasChanges && !isSaving) {
            showLeaveDialog = true
        } else {
            navController.popBackStack()
        }
    }

    // Diálogo de confirmación de salida
    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Cambios sin guardar") },
            text = { Text("Si sales ahora, los cambios no guardados se perderán.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLeaveDialog = false
                        viewModel.discardEdits()  //  restaura al snapshot + limpia localImageUri
                        navController.popBackStack()
                    }
                ) { Text("Salir") }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // --- Scaffold con Snackbar personalizado ---
    //Aparece en la en la parte inferior de la pantalla cuando se guardan los datos con éxito
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    containerColor = Color(0xFF4CAF50), // Verde positivo
                    contentColor = Color.White,
                    action = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Acción principal: "Inicio"
                            IconButton(
                                onClick = { data.performAction() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Home,
                                    contentDescription = "Ir a inicio",
                                    tint = Color.White
                                )
                            }
                            // Botón de cierre: "X"
                            IconButton(onClick = { data.dismiss() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                ) {
                    Text(data.visuals.message)
                }
            }
        }
    ) { padding ->
        UserProfileContent(
            navController = navController,
            profile = profile,
            localImageUri = localImageUri,
            onImageClick = { imageLauncher.launch("image/*") },
            onUpdateField = { viewModel.updateField(it) },
            onSaveClick = {
                if (userId > 0) {
                    viewModel.updateProfile(userId)
                    viewModel.clearImageChange()
                }
            },
            isImageChanged = isImageChanged,
            isSaving = isSaving,
            hasChanges = hasChanges,
            fieldErrors = fieldErrors,
            errorMessage = errorMessage,
            onRequestLeave = { showLeaveDialog = true },
            onDismissError = { viewModel.clearError() },
            emailErrorMessage = emailErrorMessage,
            isEditingLocation = isEditingLocation,
            isUploadingPhoto = isUploadingPhoto,
            onRemoveImage = { viewModel.removeSelectedImage() },
            modifier = Modifier.padding(padding) // evita que se solape con snackbar
        )
    }
}

/**
 * Contenido principal de la pantalla de perfil:
 * - Cabecera con foto + botón para cambiar imagen.
 * - Secciones con campos editables (nombre, apellidos, fecha, género, email, teléfono, ubicación).
 * - Botón fijo “Guardar cambios”.
 */
@Composable
fun UserProfileContent(
    profile: UserProfileDto,
    localImageUri: Uri?,
    onImageClick: () -> Unit,
    onUpdateField: (UserProfileDto.() -> UserProfileDto) -> Unit,
    onSaveClick: () -> Unit,
    navController: NavController,
    isImageChanged: Boolean,
    isSaving: Boolean,
    onRequestLeave: () -> Unit,
    hasChanges: Boolean,
    fieldErrors: FieldErrors,
    errorMessage: String?,
    onDismissError: () -> Unit,
    emailErrorMessage: String?,
    isUploadingPhoto: Boolean,
    onRemoveImage: () -> Unit,
    isEditingLocation: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    // Cache-buster estable: solo cambia cuando cambia la foto
    val cacheBust = remember(profile.photoUrl, localImageUri) { System.currentTimeMillis() }

    val imageModel: Any? = when {
        localImageUri != null -> localImageUri
        !profile.photoUrl.isNullOrBlank() -> {
            val url = profile.photoUrl
            val hasQueryParams = url.contains("?")
            url + if (hasQueryParams) "&ts=$cacheBust" else "?ts=$cacheBust"
        }
        else -> null
    }
    val imagePainter = rememberAsyncImagePainter(
        model = imageModel,
        placeholder = painterResource(R.drawable.avatar_default),
        error = painterResource(R.drawable.avatar_default),
        fallback = painterResource(R.drawable.avatar_default)
    )

    // Flags de edición por campo (persisten en recomposiciones)
    val isEditingFirstName = rememberSaveable { mutableStateOf(false) }
    val isEditingLastName = rememberSaveable { mutableStateOf(false) }
    val isEditingBirthDate = rememberSaveable { mutableStateOf(false) }
    val isEditingGender = rememberSaveable { mutableStateOf(false) }
    val isEditingEmail = rememberSaveable { mutableStateOf(false) }
    val isEditingPhone = rememberSaveable { mutableStateOf(false) }

    // Diálogo de error global (validación completa)
    if (!isSaving && hasChanges && errorMessage != null) {
        AlertDialog(
            onDismissRequest = onDismissError,
            title = { Text("Datos incompletos") },
            text  = { Text(errorMessage) },
            confirmButton = { TextButton(onClick = onDismissError) { Text("OK") } }
        )
    }

    // Helper para cerrar todos los modos edición (útil antes de guardar)
    fun resetEditingStates() {
        isEditingFirstName.value = false
        isEditingLastName.value = false
        isEditingBirthDate.value = false
        isEditingGender.value = false
        isEditingEmail.value = false
        isEditingPhone.value = false
        isEditingLocation.value = false
        isEditingBirthDate.value = false
    }

    Box(modifier = Modifier.fillMaxSize().then(modifier)) {

        // --- TopBar fija con degradado y botón "atrás" ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(60.dp)
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
                onClick = {
                    if (hasChanges && !isSaving) onRequestLeave() else navController.popBackStack()
                },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }
            Text(
                text = "Mi perfil",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // --- Contenido scrollable ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .background(Color.White)
                .padding(top = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cabecera con avatar + botón cámara + overlay de progreso
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 250.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF7F00FF), Color(0xFF00BFFF))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(150.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Image(
                            painter = imagePainter,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(150.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.White, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { onImageClick() },
                            modifier = Modifier
                                .offset(x = (-12).dp, y = (-12).dp)
                                .size(24.dp)
                                .background(Color(0xFF2196F3), shape = CircleShape)
                                .border(2.dp, Color.White, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = "Cambiar o añadir foto",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        // Mostrar botón de eliminar solo si hay una imagen seleccionada o ya guardada
                        if (localImageUri != null || !profile.photoUrl.isNullOrBlank()) {
                            IconButton(
                                onClick = onRemoveImage,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-12).dp, y = (25).dp)
                                    .size(24.dp)
                                    .background(Color.Transparent)



                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar foto",
                                    tint = Color.Red,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        if (isUploadingPhoto) {
                            // Overlay de progreso para feedback durante la subida
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color.White)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "${profile.firstName.orEmpty()} ${profile.lastName.orEmpty()}",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- Sección: Información básica ---
            SectionTitle("Información básica")

            EditableTextField(
                value = profile.firstName.orEmpty(),
                label = "Nombre",
                onValueChange = { onUpdateField { copy(firstName = it) } },
                isEditing = isEditingFirstName,
                isSaving = isSaving,
                isError = fieldErrors.firstName,
                errorMessage = "Campo obligatorio"
            )

            EditableTextField(
                value = profile.lastName.orEmpty(),
                label = "Apellidos",
                onValueChange = { onUpdateField { copy(lastName = it) } },
                isEditing = isEditingLastName,
                isSaving = isSaving,
                isError = fieldErrors.lastName,
                errorMessage = "Campo obligatorio"
            )

            BirthDateField(
                value = profile.birthDate.orEmpty(),
                onDateSelected = { newDate -> onUpdateField { copy(birthDate = newDate) } },
                isSaving = isSaving,
                isError = fieldErrors.birthDate,
                errorMessage = if (fieldErrors.birthDate) "Campo obligatorio" else null,
                isEditing = isEditingBirthDate
            )

            Spacer(modifier = Modifier.height(20.dp))

            // --- Sección: Género ---
            SectionTitle("Género")

            GenderSelector(
                selectedGender = profile.gender.orEmpty(),
                onGenderSelected = { gender -> onUpdateField { copy(gender = gender) } },
                isEnabled = !isSaving,
                isError = fieldErrors.gender
            )

            Spacer(modifier = Modifier.height(20.dp))

            // --- Sección: Información privada ---
            SectionTitle("Información privada")

            EditableTextField(
                value = profile.email.orEmpty(),
                label = "Email",
                onValueChange = { onUpdateField { copy(email = it) } },
                isEditing = isEditingEmail,
                isSaving = isSaving,
                isError = fieldErrors.email,
                errorMessage = emailErrorMessage ?: "Campo obligatorio"
            )

            EditableTextField(
                value = profile.phone.orEmpty(),
                label = "Teléfono",
                onValueChange = { onUpdateField { copy(phone = it) } },
                isEditing = isEditingPhone,
                isSaving = isSaving,
                isError = fieldErrors.phone,
                errorMessage = if (fieldErrors.phone) {
                    if (profile.phone.isNullOrBlank()) "Campo obligatorio" else "Debe tener 9 dígitos"
                } else null
            )

            Surface(
                color = Color.Transparent,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp)
                    .clickable(enabled = !isSaving && !isEditingLocation.value) {
                        isEditingLocation.value = true
                    }
            ) {
                LocationAutocompleteField(
                    value = profile.location.orEmpty(),
                    label = "Ubicación",
                    onValueChange = { onUpdateField { copy(location = it) } },
                    onSuggestionPicked = { picked -> onUpdateField { copy(location = picked) } },
                    isSaving = isSaving,
                    isError = fieldErrors.location,
                    errorMessage = "Campo obligatorio",
                    colors = transparentTextFieldColors(),
                    enabled = isEditingLocation.value, // ahora sí se activará
                    modifier = Modifier.fillMaxWidth()
                )
            }


            Spacer(modifier = Modifier.height(70.dp))
        }

        // --- Botón fijo inferior: Guardar cambios ---
        Button(
            onClick = {
                resetEditingStates()
                onSaveClick()
            },
            enabled = hasChanges && !isSaving,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (hasChanges) Color(0xFF673AB7) else Color.Gray,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        ) {
            if (isSaving) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Guardando…")
            } else {
                Text("Guardar cambios")
            }
        }
    }
}

/** Título de sección reutilizable para no repetir estilos. */
@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp)
    )
}

/**
 * Campo de texto “editable por toque”:
 * - Modo visual: al tocar el contenedor, entra en modo edición.
 * - Modo edición: habilita el TextField y hace focus automático.
 * - Muestra error y mensaje de apoyo cuando `isError = true`.
 */
@Composable
fun EditableTextField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    isEditing: MutableState<Boolean>,
    isSaving: Boolean,
    isError: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    val focusRequester = FocusRequester()

    LaunchedEffect(isEditing.value) {
        if (isEditing.value && !isSaving) {
            focusRequester.requestFocus()
        }
    }

    Surface(
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp)
            .clickable(enabled = !isSaving && !isEditing.value) {
                isEditing.value = true
            }
    ) {
        TextField(
            value = value,
            onValueChange = {
                if (isEditing.value && !isSaving) onValueChange(it)
            },
            label = { Text(label) },
            enabled = isEditing.value && !isSaving,
            isError = isError,
            supportingText = {
                if (isError && errorMessage != null) {
                    Text(text = errorMessage, color = Color.Red)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            colors = transparentTextFieldColors(),
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(
                        onClick = { onValueChange("") },
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

/**
 * Autocompletado de ubicación con menú desplegable de sugerencias.
 * - Filtra sobre la lista local de municipios (repositorio local).
 * - Usa un pequeño debounce (120ms) para mejorar la UX al escribir.
 */



/**
 * Selector de fecha (dd/MM/yyyy) usando Material3 DatePicker.
 * - Campo de solo lectura con icono de calendario.
 * - Diálogo con fecha inicial sensata (18 años por defecto si no hay valor).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthDateField(
    value: String,
    label: String = "Fecha de nacimiento",
    onDateSelected: (String) -> Unit,
    isSaving: Boolean,
    isError: Boolean,
    errorMessage: String?,
    isEditing: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    // ---- Utilidades de fecha (dd/MM/yyyy)
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    fun parseToEpochMillis(dateStr: String): Long? = try {
        val ld = LocalDate.parse(dateStr, dateFormatter)
        ld.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    } catch (_: Exception) { null }
    fun formatFromEpoch(millis: Long): String {
        val ld = Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return ld.format(dateFormatter)
    }

    var showPicker by remember { mutableStateOf(false) }
    val today = remember { LocalDate.now() }
    val fallback = remember { today.minusYears(18) }
    val initialMillis = remember(value) {
        parseToEpochMillis(value)
            ?: fallback.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
    val yearRange = 1900..today.year
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        initialDisplayedMonthMillis = initialMillis,
        yearRange = yearRange
    )

    // Surface clickable: entra en modo edición como los demás campos
    Surface(
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp)
            .clickable(enabled = !isSaving && !isEditing.value) { isEditing.value = true }
    ) {
        Column {
            TextField(
                value = value,
                onValueChange = { /* readOnly */ },
                label = { Text(label) },
                readOnly = true,
                enabled = isEditing.value && !isSaving,     // ← gris cuando no editas
                isError = isError,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = "Elegir fecha",
                        modifier = Modifier
                            .clickable(enabled = isEditing.value && !isSaving) { showPicker = true }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = transparentTextFieldColors()
            )

            if (isError && !errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                )
            }
        }
    }

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = {
                showPicker = false
                isEditing.value = false             // ← salir de edición al cancelar
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onDateSelected(formatFromEpoch(it))
                        }
                        showPicker = false
                        isEditing.value = false         // ← salir de edición al confirmar
                    },
                    enabled = datePickerState.selectedDateMillis != null
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPicker = false
                    isEditing.value = false
                }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * Selector de género con 3 opciones:
 * - Resalta la opción activa con borde y color.
 * - Deshabilita interacción cuando `isEnabled = false`.
 */
@Composable
fun GenderSelector(
    selectedGender: String,
    onGenderSelected: (String) -> Unit,
    isEnabled: Boolean,
    isError: Boolean = false
) {
    val options = listOf(
        "Hombre" to R.drawable.hombre,
        "Mujer" to R.drawable.mujer,
        "No binario" to R.drawable.nobinario
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        options.forEach { (genderKey, drawableRes) ->
            val isSelected = genderKey == selectedGender

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable(enabled = isEnabled && !isSelected) {
                        onGenderSelected(genderKey)
                    }
            ) {
                Image(
                    painter = painterResource(id = drawableRes),
                    contentDescription = genderKey,
                    modifier = Modifier
                        .size(45.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = genderKey.replaceFirstChar { it.uppercase() },
                    fontSize = 12.sp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
        }
    }

    if (isError) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Campo obligatorio",
            color = Color.Red,
            fontSize = 12.sp
        )
    }
}

/**
 PREVIEW PARA PODER DISEÑAR Y EDITAR LA PANTALLA SIN TENER QUE EJECUTAR LA APP
 */
@Preview(showBackground = true)
@Composable
fun UserProfileContentPreview() {
    val fakeProfile = UserProfileDto(
        firstName = "Javier",
        lastName = "Serrano",
        email = "javier@example.com",
        phone = "600123456",
        location = "Madrid",
        gender = "Hombre",          // ⇦ unifica con GenderSelector
        birthDate = "10/05/1995",   // ⇦ unifica con BirthDateField (dd/MM/yyyy)
        photoUrl = ""
    )
    val fakeNavController = rememberNavController()
    val fakeImageChanged = false
    val fakeFieldErrors = FieldErrors()
    val fakeIsEditingLocation = remember { mutableStateOf(false) }

    UserProfileContent(
        profile = fakeProfile,
        localImageUri = null,
        onImageClick = {},
        onUpdateField = {},
        onSaveClick = {},
        navController = fakeNavController,
        isImageChanged = fakeImageChanged,
        onRequestLeave = {},
        isSaving = false,
        hasChanges = true,
        fieldErrors = fakeFieldErrors,
        errorMessage = null,
        onDismissError = {},
        emailErrorMessage = null,
        isEditingLocation = fakeIsEditingLocation,
        isUploadingPhoto = false,
        onRemoveImage={}
    )
}
