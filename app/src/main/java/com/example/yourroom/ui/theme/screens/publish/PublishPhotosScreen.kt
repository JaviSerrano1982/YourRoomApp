package com.example.yourroom.ui.theme.screens.publish


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun PublishPhotosScreen(navController: NavController) {
    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = { navController.popBackStack() }) {
                    Text("Atrás")
                }
                Button(onClick = {
                    // Aquí de momento volvemos al perfil al terminar
                    navController.navigate("profile") {
                        popUpTo(PublishRoutes.Root) { inclusive = true }
                    }
                }) {
                    Text("Finalizar")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text("Pantalla Fotos")
        }
    }
}