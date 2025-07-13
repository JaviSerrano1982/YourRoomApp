package com.example.yourroom.ui.theme.screens

import android.R.color.white
import android.annotation.SuppressLint
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.yourroom.R
import com.example.yourroom.datastore.UserPreferences
import kotlinx.coroutines.delay
import com.airbnb.lottie.compose.*

@Composable
fun SplashScreen(navController: NavHostController) {
    val context = LocalContext.current


    val gradient = Brush.verticalGradient(
        listOf(Color(0xFF0A1D37), Color(0xFF2FE2EC))
    )

    LaunchedEffect(Unit) {
        delay(2500)
        val isLoggedIn = UserPreferences(context).isUserLoggedIn()
        if (isLoggedIn) {
            navController.navigate("home") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

// Aquí reutilizas todo tu contenido animado del SplashScreenContent
    SplashScreenContent()
}

@SuppressLint("ResourceAsColor", "UnusedBoxWithConstraintsScope")

@Composable
fun SplashScreenContent() {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Desplazamiento vertical del centro del gradiente radial
        val offsetY = -maxHeight.value * 0.1f

        val radialGradient = Brush.radialGradient(
            colorStops = arrayOf(
                0.55f to Color.White,
                1.0f to Color(0xFF90ECF3)
            ),
            center = Offset(
                x = maxWidth.value / 0.73f,
                y = maxHeight.value / 0.72f + offsetY
            ),
            radius = Float.POSITIVE_INFINITY
        )

        // Capa de fondo con el gradiente radial
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = radialGradient)
        ) {
            // Capa superior con un gradiente vertical azul oscuro difuminado
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFBDF8FB).copy(alpha = 0f), // parte superior
                                Color.Transparent,                     // centro
                                Color(0xFF0A2942).copy(alpha = 1f)  // parte inferior
                            )
                        )
                    )
            )
            // 🎬 Animaciones
            val logoAlpha by animateFloatAsState(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                label = "LogoFade"
            )

            val progressAlpha by animateFloatAsState(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 1000,
                    delayMillis = 1000,
                    easing = FastOutSlowInEasing
                ),
                label = "ProgressFade"
            )

            // Contenido centrado: logo y cargador
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.your_room_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(260.dp)
                        .offset(y = 30.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))


                Text(
                    text = "Alquila. Usa. Libera.",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(top = 8.dp)
                        .offset(y = 20.dp)
                )


                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(48.dp)
                        .offset(y = 80.dp)
                )


            }

            }

        }
    }



@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SplashScreenContent()
}



