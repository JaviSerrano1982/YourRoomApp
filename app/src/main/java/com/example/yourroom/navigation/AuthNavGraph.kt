package com.example.yourroom.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.yourroom.ui.theme.screens.HomeScreen
import com.example.yourroom.ui.theme.screens.LoginScreen
import com.example.yourroom.ui.theme.screens.RegisterScreen
import com.example.yourroom.ui.theme.screens.SplashScreen
import com.example.yourroom.ui.theme.screens.SuccessScreen


@Composable
fun AuthNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("home") { HomeScreen(navController = navController) }
        composable("success") {
            SuccessScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("success") { inclusive = true }
                    }
                }
            )
        }


    }
}