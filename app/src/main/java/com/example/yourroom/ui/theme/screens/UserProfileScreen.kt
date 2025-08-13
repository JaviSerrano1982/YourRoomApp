package com.example.yourroom.ui.theme.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.yourroom.R
import com.example.yourroom.viewmodel.UserProfileViewModel
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yourroom.model.UserProfileDto
import com.example.yourroom.viewmodel.FieldErrors

@Composable
fun UserProfileScreen(
    navController: NavHostController,
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val hasChanges by viewModel.hasChanges.collectAsState()
    val userId by viewModel.userId.collectAsState()
    val localImageUri by viewModel.localImageUri.collectAsState()
    val isImageChanged by viewModel.isImageChanged.collectAsState()
    val fieldErrors by viewModel.fieldErrors.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val emailErrorMessage by viewModel.emailErrorMessage.collectAsState()

    var showLeaveDialog by remember { mutableStateOf(false) }



    val context = LocalContext.current

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.setLocalImage(uri)
    }

    LaunchedEffect(Unit) {
        viewModel.initProfile(context)
    }

    BackHandler {
        if (hasChanges && !isSaving) {
            showLeaveDialog = true
        } else {
            navController.popBackStack()
        }
    }
    // AlertDialog de confirmación para salir
    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Cambios sin guardar") },
            text = { Text("Si sales ahora, los cambios no guardados se perderán.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLeaveDialog = false
                        navController.popBackStack()
                    }
                ) { Text("Salir") }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) { Text("Cancelar") }
            }
        )
    }

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
        isImageChanged = remember { mutableStateOf(isImageChanged) }, // Para Preview
        isSaving = isSaving,
        hasChanges = hasChanges,
        fieldErrors = fieldErrors,
        errorMessage = errorMessage,
        onRequestLeave = { showLeaveDialog = true },
        onDismissError = { viewModel.clearError() },
        emailErrorMessage = emailErrorMessage
    )
}

@Composable
fun UserProfileContent(
    profile: UserProfileDto,
    localImageUri: Uri?,
    onImageClick: () -> Unit,
    onUpdateField: (UserProfileDto.() -> UserProfileDto) -> Unit,
    onSaveClick: () -> Unit,
    navController: NavController,
    isImageChanged: MutableState<Boolean>,
    isSaving: Boolean,
    onRequestLeave: () -> Unit,
    hasChanges: Boolean,
    fieldErrors: FieldErrors,
    errorMessage: String?,
    onDismissError: () -> Unit,
    emailErrorMessage: String?
) {
    val photoUrl = profile.photoUrl.takeIf { it.isNotBlank() }
    val hasProfilePhoto = localImageUri != null || photoUrl != null

    val imagePainter = if (hasProfilePhoto) {
        rememberAsyncImagePainter(
            model = localImageUri ?: photoUrl,
            placeholder = painterResource(R.drawable.avatar_default),
            error = painterResource(R.drawable.avatar_default)
        )
    } else {
        painterResource(R.drawable.avatar_default)
    }

    val isEditingFirstName = remember { mutableStateOf(false) }
    val isEditingLastName = remember { mutableStateOf(false) }
    val isEditingBirthDate = remember { mutableStateOf(false) }
    val isEditingGender = remember { mutableStateOf(false) }
    val isEditingEmail = remember { mutableStateOf(false) }
    val isEditingPhone = remember { mutableStateOf(false) }
    val isEditingLocation = remember { mutableStateOf(false) }


    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = onDismissError,
            title = { Text("Datos incompletos") },
            text  = { Text(errorMessage ?: "") },
            confirmButton = {
                TextButton(onClick = onDismissError) { Text("OK") }
            }
        )
    }



    fun resetEditingStates() {
        isEditingFirstName.value = false
        isEditingLastName.value = false
        isEditingBirthDate.value = false
        isEditingGender.value = false
        isEditingEmail.value = false
        isEditingPhone.value = false
        isEditingLocation.value = false
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Topbar fija
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
                    if (hasChanges && !isSaving) {
                        // delega al Screen mostrando el diálogo
                        // opción A: expón un callback onRequestLeave() desde Content
                        // opción B (rápida): pásale un lambda para setear showLeaveDialog
                        onRequestLeave()

                    } else {
                        navController.popBackStack()
                    }
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .background(Color.White)
                .padding(top = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cabecera
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
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "${profile.firstName} ${profile.lastName}",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // CAMPOS

            Text(
                text = "Información básica",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp)
            )

            EditableTextField(
                value = profile.firstName,
                label = "Nombre",
                onValueChange = { onUpdateField { copy(firstName = it) } },
                isEditing = isEditingFirstName,
                isSaving = isSaving,
                isError = fieldErrors.firstName,
                errorMessage = "Campo obligatorio"

            )

            EditableTextField(
                value = profile.lastName,
                label = "Apellidos",
                onValueChange = { onUpdateField { copy(lastName = it) } },
                isEditing = isEditingLastName,
                isSaving = isSaving,
                isError = fieldErrors.lastName,
                errorMessage = "Campo obligatorio"
            )

            EditableTextField(
                value = profile.birthDate,
                label = "Fecha de nacimiento (YYYY-MM-DD)",
                onValueChange = { onUpdateField { copy(birthDate = it) } },
                isEditing = isEditingBirthDate,
                isSaving = isSaving,
                isError = fieldErrors.birthDate,
                errorMessage = "Campo obligatorio"
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Género",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp)
            )

            GenderSelector(
                selectedGender = profile.gender,
                onGenderSelected = { gender -> onUpdateField { copy(gender = gender) } },
                isEnabled = !isSaving,
                isError = fieldErrors.gender
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Información privada",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp)
            )

            EditableTextField(
                value = profile.email,
                label = "Email",
                onValueChange = { onUpdateField { copy(email = it) } },
                isEditing = isEditingEmail,
                isSaving = isSaving,
                isError = fieldErrors.email,
                errorMessage = emailErrorMessage ?: "Campo obligatorio"
            )

            EditableTextField(
                value = profile.phone,
                label = "Teléfono",
                onValueChange = { onUpdateField { copy(phone = it) } },
                isEditing = isEditingPhone,
                isSaving = isSaving,
                isError = fieldErrors.phone,
                errorMessage = "Campo obligatorio"
            )

            EditableTextField(
                value = profile.location,
                label = "Ubicación",
                onValueChange = { onUpdateField { copy(location = it) } },
                isEditing = isEditingLocation,
                isSaving = isSaving,
                isError = fieldErrors.location,
                errorMessage = "Campo obligatorio"
            )

            Spacer(modifier = Modifier.height(70.dp))
        }

        // Botón fijo
        Button(
            onClick = {
                resetEditingStates()
                onSaveClick()
            },
            enabled = hasChanges && !isSaving,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (hasChanges) Color(0xFF4CAF50) else Color.Gray,
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
                if (isEditing.value && !isSaving) {
                    onValueChange(it)
                }
            },
            label = { Text(label) },
            enabled = isEditing.value && !isSaving,
            isError = isError,  // activa borde rojo
            supportingText = {
                if (isError && errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = Color.Red
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            colors = textFieldColors()
        )
    }
}

@Composable
fun textFieldColors() = TextFieldDefaults.colors(
    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
    disabledIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    disabledContainerColor = Color.White
)

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
                    .clickable(enabled = isEnabled) {
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

@Preview(showBackground = true)
@Composable
fun UserProfileContentPreview() {
    val fakeProfile = com.example.yourroom.model.UserProfileDto(
        firstName = "Javier",
        lastName = "Serrano",
        email = "javier@example.com",
        phone = "600123456",
        location = "Madrid",
        gender = "Masculino",
        birthDate = "1995-05-10",
        photoUrl = ""
    )
    val fakeNavController = rememberNavController()
    val fakeImageChanged = remember { mutableStateOf(false) }

    // Importa FieldErrors:
    // import com.example.yourroom.viewmodel.FieldErrors
    val fakeFieldErrors = com.example.yourroom.viewmodel.FieldErrors()

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
        emailErrorMessage = null
    )
}
