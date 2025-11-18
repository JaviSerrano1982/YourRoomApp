package com.example.yourroom.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.yourroom.ui.screens.profile.UserProfileScreen
import com.example.yourroom.ui.screens.auth.LoginScreen
import com.example.yourroom.ui.screens.auth.RegisterScreen
import com.example.yourroom.ui.screens.favorites.FavoritesScreen
import com.example.yourroom.ui.screens.home.HomeScreen
import com.example.yourroom.ui.screens.publish.PublishBasicsScreen
import com.example.yourroom.ui.screens.publish.PublishDetailsScreen
import com.example.yourroom.ui.screens.publish.PublishPhotosScreen
import com.example.yourroom.ui.screens.succes.SuccessScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.yourroom.ui.screens.edit.EditRoomRoutes
import com.example.yourroom.ui.screens.edit.EditRoomScreen
import com.example.yourroom.ui.screens.edit.EditSecondaryPhotosScreen
import com.example.yourroom.ui.screens.home.SearchSpacesScreen
import com.example.yourroom.ui.screens.myRooms.MyRoomsScreen
import com.example.yourroom.ui.screens.splash.SplashScreen
import com.example.yourroom.ui.screens.publish.PublishRoutes
import com.example.yourroom.viewmodel.UserProfileViewModel


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

    val userProfileVM: UserProfileViewModel = hiltViewModel()


    // Carga de perfil en cuanto tenemos userId
    LaunchedEffect(isLoggedIn, userId) {
        if (isLoggedIn && userId > 0L) {
            userProfileVM.loadProfile(userId)
        }
    }


    // Estructura principal de la app
    Scaffold(
        bottomBar = {
            if (showBottomBar.value) {
                BottomNavigationBar(navController, userProfileVM)
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
            startDestination = if (isLoggedIn && userId != 0L) "home" else "splash",

            //startDestination = "home", // <- Bypass (solo pruebas)

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
            // Éxito tras REGISTRO
            composable(route = "success_register") {
                SuccessScreen(
                    title = "¡Registro exitoso!",
                    primaryText = "Iniciar sesión",
                    onPrimaryClick = {
                        navController.navigate("login") {
                            popUpTo("success_register") { inclusive = true }
                        }
                    }
                )
            }

            // Éxito tras PUBLICAR SALA
            composable(route = "success_publish") {
                SuccessScreen(
                    title = "¡Sala publicada con éxito!",
                    primaryText = "Ir al inicio",
                    onPrimaryClick = {
                        navController.navigate("home") {
                            popUpTo("success_publish") { inclusive = true } // limpia la pantalla de éxito
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(
                route = "edit_secondary_photos/{spaceId}",
                arguments = listOf(navArgument("spaceId") { type = NavType.LongType })
            ) { backStackEntry ->
                val spaceId = backStackEntry.arguments!!.getLong("spaceId")
                EditSecondaryPhotosScreen(
                    navController = navController,
                    spaceId = spaceId
                )
            }



            // -----------------------------
            // RUTAS PRIVADAS
            // -----------------------------
            composable("home") {
                showBottomBar.value = true
                HomeScreen(navController)
            }
            composable("myRooms") {
                showBottomBar.value = false
                MyRoomsScreen(navController)
            }
            composable("favorites") {
                showBottomBar.value = true
                FavoritesScreen()
            }
            composable("profile") {
                showBottomBar.value = false
                UserProfileScreen(navController,userProfileVM)
            }
            composable("search_spaces") {
                SearchSpacesScreen(navController)
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

            // RUTA PARA EDITAR SALA PUBLICADA
            composable(
                route = EditRoomRoutes.Edit,
                arguments = listOf(navArgument("spaceId") { type = NavType.LongType })
            ) { backStackEntry ->
                showBottomBar.value = false
                val spaceId = backStackEntry.arguments?.getLong("spaceId") ?: 0L
                EditRoomScreen(navController, spaceId)
            }


        }
    }
}
