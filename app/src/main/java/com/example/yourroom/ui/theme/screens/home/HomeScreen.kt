package com.example.yourroom.ui.theme.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.yourroom.datastore.UserPreferences
import com.example.yourroom.viewmodel.UserProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val scope = rememberCoroutineScope()
    val profileVM: UserProfileViewModel = hiltViewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Bienvenido a la Home")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            scope.launch {
                // 1) Limpia estado local del VM
                profileVM.clearSessionState()
                // 2) Limpia DataStore (userId, tokens, etc.)
                userPreferences.clearSession()
                // 3) Cierra sesión de Firebase
                FirebaseAuth.getInstance().signOut()
                // 4) Navega a login limpiando todo el stack
                navController.navigate("login") {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }) {
            Text("Cerrar sesión")
        }
    }
}