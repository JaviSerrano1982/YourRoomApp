package com.example.yourroom.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.yourroom.ui.theme.screens.*
import com.example.yourroom.ui.theme.screens.profile.UserProfileScreen
import com.example.yourroom.ui.theme.screens.auth.LoginScreen
import com.example.yourroom.ui.theme.screens.auth.RegisterScreen
import com.example.yourroom.ui.theme.screens.favorites.FavoritesScreen
import com.example.yourroom.ui.theme.screens.home.HomeScreen
import com.example.yourroom.ui.theme.screens.publish.PublishBasicsScreen
import com.example.yourroom.ui.theme.screens.publish.PublishDetailsScreen
import com.example.yourroom.ui.theme.screens.publish.PublishPhotosScreen
import com.example.yourroom.ui.theme.screens.succes.SuccessScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.yourroom.ui.theme.screens.publish.PublishRoutes
import com.google.firebase.BuildConfig

// ---------------------------------------------------------------------
// NAVEGACIÓN PRINCIPAL DE LA APP (NavGraph)
// ---------------------------------------------------------------------

/**
 * Define la navegación de toda la aplicación YourRoom.
 *
 * - Usa [NavHostController] para gestionar las rutas.
 * - Define qué pantalla se muestra según el estado de login.
 * - Controla la visibilidad de la BottomNavigationBar.
 *
 * @param navController controlador de navegación.
 * @param isLoggedIn indica si el usuario ya está autenticado.
 * @param userId identificador del usuario (0L si aún no existe).
 */


@Composable
fun AppNavGraph(
    navController: NavHostController,
    isLoggedIn: Boolean,
    userId: Long
) {
    // Estado para mostrar u ocultar la BottomNavigationBar
    val showBottomBar = remember { mutableStateOf(false) }

    // Debug temporal: mostrar userId cargado en consola
    LaunchedEffect(userId) {
        println("USER ID EN PROFILE: $userId")
    }


    // Estructura principal de la app
    Scaffold(
        bottomBar = {
            if (showBottomBar.value) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,

            // -----------------------------
            // Pantalla inicial
            // -----------------------------
            // Por defecto: splash → login → home
            // Bypass para pruebas: cambiar a "home"
            //startDestination = if (isLoggedIn && userId != 0L) "home" else "splash",

            startDestination = "home", // <- Bypass (solo pruebas)

            modifier = Modifier.padding(innerPadding)
        ) {
            // -----------------------------
            // RUTAS PÚBLICAS (sin BottomBar)
            // -----------------------------
            composable("splash") {
                SplashScreen(navController)
            }
            composable("login") {
                showBottomBar.value = false
                LoginScreen(navController)
            }
            composable("register") {
                RegisterScreen(navController)
            }
            composable("success") {
                SuccessScreen(
                    onNavigateToLogin = {
                        navController.navigate("login") {
                            popUpTo("success") { inclusive = true }
                        }
                    }
                )
            }

            // -----------------------------
            // RUTAS PRIVADAS (con BottomBar)
            // -----------------------------
            composable("home") {
                showBottomBar.value = true
                HomeScreen(navController)
            }
            composable("search") {
                showBottomBar.value = true
                SearchScreen()
            }
            composable("favorites") {
                showBottomBar.value = true
                FavoritesScreen()
            }
            composable("profile") {
                showBottomBar.value = false
                UserProfileScreen(navController)
            }

            // navigation(...)
            navigation(
                startDestination = "publish/basics",
                route = PublishRoutes.Root
            ) {
                composable("publish/basics") {
                    showBottomBar.value = false
                    PublishBasicsScreen(navController)
                }

                composable(
                    route = PublishRoutes.Details,        // "publish/{spaceId}/details"
                    arguments = listOf(navArgument("spaceId") { type = NavType.LongType })
                ) { backStackEntry ->
                    showBottomBar.value = false
                    val spaceId = backStackEntry.arguments?.getLong("spaceId") ?: 0L
                    PublishDetailsScreen(navController, spaceId)
                }

                composable(
                    route = PublishRoutes.Photos,         // "publish/{spaceId}/photos"
                    arguments = listOf(navArgument("spaceId") { type = NavType.LongType })
                ) { backStackEntry ->
                    showBottomBar.value = false
                    val spaceId = backStackEntry.arguments?.getLong("spaceId") ?: 0L
                    PublishPhotosScreen(navController, spaceId)
                }
            }


        }
    }
}
