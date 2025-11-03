package com.example.yourroom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.yourroom.datastore.UserPreferences
import com.example.yourroom.navigation.AppNavGraph


import com.example.yourroom.ui.theme.YourRoomTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            YourRoomTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                val userPreferences = UserPreferences(context)
                val userId by userPreferences.userIdFlow.collectAsState(initial = 0L)

                //Comentar este codigo para pasar directamente al login.(Solo pruebas)
                val isLoggedIn by userPreferences.isLoggedInFlow.collectAsState(initial = false)

                //Descomentar este código para pasar directamente al login. (Solo pruebas)
               //val isLoggedIn = true


                    AppNavGraph(navController, isLoggedIn, userId)

            }
        }
    }
}

