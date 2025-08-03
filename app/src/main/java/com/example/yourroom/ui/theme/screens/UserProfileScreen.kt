package com.example.yourroom.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.yourroom.R
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun UserProfileScreen(
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
        profile = profile,
        localImageUri = localImageUri,
        onImageClick = { imageLauncher.launch("image/*") },
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
        }
    )
}

@Composable
fun UserProfileContent(
    profile: com.example.yourroom.model.UserProfileDto,
    localImageUri: Uri?,
    onImageClick: () -> Unit,
    onUpdateField: (com.example.yourroom.model.UserProfileDto.() -> com.example.yourroom.model.UserProfileDto) -> Unit,
    onSaveClick: () -> Unit
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

            // Cabecera con degradado que ocupa todo el alto disponible
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
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
                                .border(2.dp, Color.White, CircleShape)
                        )

                        IconButton(
                            onClick = onImageClick,
                            modifier = Modifier
                                .offset(x = 4.dp, y = 4.dp)
                                .size(36.dp)
                                .background(Color(0xFFFF9800), shape = CircleShape)
                                .border(2.dp, Color.White, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Cambiar foto",
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


            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = profile.firstName,
                onValueChange = { onUpdateField { copy(firstName = it) } },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    disabledIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = profile.lastName,
                onValueChange = { onUpdateField { copy(lastName = it) } },
                label = { Text("Apellidos") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = profile.birthDate,
                onValueChange = { onUpdateField { copy(birthDate = it) } },
                label = { Text("Fecha de nacimiento (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = profile.gender,
                onValueChange = { onUpdateField { copy(gender = it) } },
                label = { Text("Género") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = profile.email,
                onValueChange = { onUpdateField { copy(email = it) } },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = profile.phone,
                onValueChange = { onUpdateField { copy(phone = it) } },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = profile.location,
                onValueChange = { onUpdateField { copy(location = it) } },
                label = { Text("Ubicación") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = onSaveClick) {
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

    UserProfileContent(
        profile = fakeProfile,
        localImageUri = null,
        onImageClick = {},
        onUpdateField = {},
        onSaveClick = {}
    )
}
