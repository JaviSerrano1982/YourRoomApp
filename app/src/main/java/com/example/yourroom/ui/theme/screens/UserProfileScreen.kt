package com.example.yourroom.screens

import android.net.Uri
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
import androidx.compose.material.Button

import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.yourroom.datastore.UserPreferences
import com.example.yourroom.viewmodel.UserProfileViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.yourroom.R
import androidx.compose.material.icons.outlined.Male
import androidx.compose.material.icons.outlined.Female
import androidx.compose.material.icons.Icons


@Composable
fun UserProfileScreen(
    navController: NavHostController,
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var userId by remember { mutableStateOf(0L) }
    var localImageUri by remember { mutableStateOf<Uri?>(null) }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        localImageUri = uri
        uri?.let {
            viewModel.updateField { copy(photoUrl = it.toString()) }
        }
    }
    val isImageChanged = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val prefs = UserPreferences(context)
        val storedId = prefs.userIdFlow.first()
        println("\u2705 userId cargado al entrar: $storedId")
        userId = storedId

        if (storedId > 0) {
            viewModel.loadProfile(storedId)
        } else {
            println("\u26A0\uFE0F userId inv\u00e1lido al cargar perfil")
        }
    }

    UserProfileContent(
        navController = navController,
        profile = profile,
        localImageUri = localImageUri,
        onImageClick = {
            imageLauncher.launch("image/*")
            isImageChanged.value = true
        },
        onUpdateField = { viewModel.updateField(it) },

        onSaveClick = {
            scope.launch {
                println("\uD83E\uDDE0 userId recuperado en bot\u00f3n: $userId")
                if (userId > 0) {
                    viewModel.updateProfile(userId)
                } else {
                    println("\u26A0\uFE0F No se puede guardar, userId inv\u00e1lido")
                }
            }
        },
        isImageChanged = isImageChanged

    )
}

@Composable
fun UserProfileContent(
    profile: com.example.yourroom.model.UserProfileDto,
    localImageUri: Uri?,
    onImageClick: () -> Unit,
    onUpdateField: (com.example.yourroom.model.UserProfileDto.() -> com.example.yourroom.model.UserProfileDto) -> Unit,
    onSaveClick: () -> Unit,
    navController: NavController,
    isImageChanged: MutableState<Boolean>

) {
    val photoUrl = profile.photoUrl.takeIf { it.isNotBlank() }
    var initialProfile by remember { mutableStateOf(profile) }
    val isInitialProfileSet = remember { mutableStateOf(false) }
    val hasChanges = remember { mutableStateOf(false) }






    LaunchedEffect(Unit) {
        if (!isInitialProfileSet.value && profile.firstName.isNotBlank()) {
            initialProfile = profile
            isInitialProfileSet.value = true
        }
    }



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
    val isGenderChanged = remember { mutableStateOf(false) }


    fun resetEditingStates() {
        isEditingFirstName.value = false
        isEditingLastName.value = false
        isEditingBirthDate.value = false
        isEditingGender.value = false
        isEditingEmail.value = false
        isEditingPhone.value = false
        isEditingLocation.value = false
    }




    Box(modifier = Modifier
        .fillMaxSize()
        .systemBarsPadding()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                ,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF7F00FF), Color(0xFF00BFFF))
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
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


            // Cabecera con degradado que ocupa todo el alto disponible
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 250.dp)

                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF7F00FF), Color(0xFF00BFFF))
                        )
                    )
                   ,
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                            onClick = onImageClick,
                            modifier = Modifier
                                .offset(x = -12.dp, y = -12.dp)
                                .size(24.dp)
                                .background(Color(0xFF2196F3), shape = CircleShape) // Azul cámara
                                .border(2.dp, Color.White, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
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

            TextField(
                value = profile.firstName,
                onValueChange = {
                    onUpdateField { copy(email = it) }
                    hasChanges.value = true
                }
                ,
                label = { Text("Nombre") },
                enabled = isEditingFirstName.value,
                trailingIcon = {
                    IconButton(onClick = {
                        isEditingFirstName.value = true
                        hasChanges.value = true},
                        ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            modifier = Modifier.size(18.dp)
                            )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                colors = textFieldColors()
            )

            TextField(
                value = profile.lastName,
                onValueChange = { onUpdateField { copy(lastName = it) } },
                label = { Text("Apellidos") },
                enabled = isEditingLastName.value,
                trailingIcon = {
                    IconButton(onClick = { isEditingLastName.value = true }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                colors = textFieldColors()
            )

            TextField(
                value = profile.birthDate,
                onValueChange = { onUpdateField { copy(birthDate = it) } },
                label = { Text("Fecha de nacimiento (YYYY-MM-DD)") },
                enabled = isEditingBirthDate.value,
                trailingIcon = {
                    IconButton(onClick = { isEditingBirthDate.value = true }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                colors = textFieldColors()
            )

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
                onGenderSelected = { gender ->
                    onUpdateField { copy(gender = gender) }
                    hasChanges.value = true
                }

            )




            TextField(
                value = profile.email,
                onValueChange = { onUpdateField { copy(email = it) } },
                label = { Text("Email") },
                enabled = isEditingEmail.value,
                trailingIcon = {
                    IconButton(onClick = { isEditingEmail.value = true }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                colors = textFieldColors()
            )

            TextField(
                value = profile.phone,
                onValueChange = { onUpdateField { copy(phone = it) } },
                label = { Text("Teléfono") },
                enabled = isEditingPhone.value,
                trailingIcon = {
                    IconButton(onClick = { isEditingPhone.value = true }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                colors = textFieldColors()
            )

            TextField(
                value = profile.location,
                onValueChange = { onUpdateField { copy(location = it) } },
                label = { Text("Ubicación") },
                enabled = isEditingLocation.value,
                trailingIcon = {
                    IconButton(onClick = { isEditingLocation.value = true }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                colors = textFieldColors()
            )

            Button(
                onClick = {
                    onSaveClick()
                    isImageChanged.value = false
                    hasChanges.value = false
                },
                enabled = hasChanges.value,

                colors = ButtonDefaults.buttonColors(
                    containerColor = if ( !hasChanges.value) Color(0xFF4CAF50) else Color.LightGray,
                    contentColor = Color.White
                ),
                modifier = Modifier.padding(24.dp)
            ) {
                Text("Guardar cambios")
            }


        }
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

    UserProfileContent(
        profile = fakeProfile,
        localImageUri = null,
        onImageClick = {},
        onUpdateField = {},
        onSaveClick = {},
        navController = fakeNavController,
        isImageChanged = fakeImageChanged

    )
}


// Utilidad para evitar repetir los mismos colores
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

) {
    val options = listOf("Mujer", "Hombre")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        options.forEach { gender ->
            val isSelected = gender == selectedGender
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable {
                        onGenderSelected(gender)

                    }
            ) {
                Icon(
                    imageVector = if (gender == "Mujer") Icons.Outlined.Female else Icons.Outlined.Male,
                    contentDescription = gender,
                    modifier = Modifier.size(48.dp),
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = gender,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
        }
    }
}
