package com.example.yourroom.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.yourroom.viewmodel.UserProfileViewModel
import java.util.*

@Composable
fun UserProfileScreen(
    userId: Long,
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val context = LocalContext.current

    var localImageUri by remember { mutableStateOf<Uri?>(null) }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        localImageUri = uri
        uri?.let {
            viewModel.updateField { copy(photoUrl = it.toString()) }
        }
    }

    LaunchedEffect(userId) {
        if (userId > 0) {
            println("🔍Cargando perfil con userId = $userId")
            viewModel.loadProfile(userId)
        } else {
            println(" ID de usuario inválido: $userId")
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = rememberAsyncImagePainter(localImageUri ?: profile.photoUrl),
            contentDescription = "Foto de perfil",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
        )

        TextButton(onClick = { imageLauncher.launch("image/*") }) {
            Text("Cambiar foto")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = profile.firstName,
            onValueChange = { viewModel.updateField { copy(firstName = it) } },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = profile.lastName,
            onValueChange = { viewModel.updateField { copy(lastName = it) } },
            label = { Text("Apellidos") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = profile.email,
            onValueChange = { viewModel.updateField { copy(email = it) } },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = profile.phone,
            onValueChange = { viewModel.updateField { copy(phone = it) } },
            label = { Text("Teléfono") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = profile.location,
            onValueChange = { viewModel.updateField { copy(location = it) } },
            label = { Text("Ubicación") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = profile.gender,
            onValueChange = { viewModel.updateField { copy(gender = it) } },
            label = { Text("Género") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = profile.birthDate,
            onValueChange = { viewModel.updateField { copy(birthDate = it) } },
            label = { Text("Fecha de nacimiento (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { viewModel.updateProfile(userId) }) {
            Text("Guardar cambios")
        }
    }

}
