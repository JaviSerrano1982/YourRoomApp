package com.example.yourroom.ui.theme.screens.publish


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun PublishPhotosScreen(
    navController: NavController,
    spaceId: Long) {
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
                    // Volvemos a home
                    navController.navigate("home") {
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PublishPhotosScreenPreview() {
    PublishPhotosScreen(
        navController = rememberNavController(),
        spaceId = 0L
    )
}