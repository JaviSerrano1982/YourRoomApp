package com.example.yourroom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.example.yourroom.datastore.UserPreferences
import com.example.yourroom.navigation.AppNavGraph


import com.example.yourroom.ui.theme.YourRoomTheme
import com.example.yourroom.ui.theme.screens.SplashScreen
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

