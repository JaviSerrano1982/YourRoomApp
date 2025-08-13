package com.example.yourroom.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.yourroom.ui.theme.screens.*
import com.example.yourroom.ui.theme.screens.UserProfileScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    isLoggedIn: Boolean,
    userId: Long
) {
    val showBottomBar = remember { mutableStateOf(false) }
    LaunchedEffect(userId) {
        println("USER ID EN PROFILE: $userId")
    }


    Scaffold(
        bottomBar = {
            if (showBottomBar.value) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            //COMENTAR ESTE CÓDIGO PARA HACER BYPASS. (solo pruebas)
           startDestination = if (isLoggedIn && userId != 0L) "home" else "splash",

            //Descomentar la siguiente línea para hacer Bypass (solo pruebas
             //startDestination="home",
            modifier = Modifier.padding(innerPadding)
        ) {
            // Pantallas públicas
            composable("splash") { SplashScreen(navController) }
            composable("login") { showBottomBar.value = false
                                            LoginScreen(navController) }
            composable("register") { RegisterScreen(navController) }
            composable("success") {
                SuccessScreen(
                    onNavigateToLogin = {
                        navController.navigate("login") {
                            popUpTo("success") { inclusive = true }
                        }
                    }
                )
            }

            // Pantallas privadas con BottomBar
            composable("home") {
                showBottomBar.value = true
                HomeScreen(navController)
            }
            composable("search") {
                showBottomBar.value = true
                SearchScreen()
            }
            composable("publish") {
                showBottomBar.value = true
                PublishScreen()
            }
            composable("favorites") {
                showBottomBar.value = true
                FavoritesScreen()
            }
            composable("profile") {
                showBottomBar.value = false
                UserProfileScreen(navController)
            }

        }

    }
}


